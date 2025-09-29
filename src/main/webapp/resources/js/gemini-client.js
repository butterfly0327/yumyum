(function (global) {
    const DEFAULT_MODEL = 'gemini-2.5-flash';
    const API_BASE = 'https://generativelanguage.googleapis.com/v1beta/models';

    function resolveKeyProvider() {
        if (global.appUtils && typeof global.appUtils.getGeminiApiKey === 'function') {
            return {
                get: () => (global.appUtils.getGeminiApiKey() || '').trim(),
            };
        }

        let inMemoryKey = '';
        return {
            get: () => inMemoryKey,
            set: (value) => {
                inMemoryKey = (value || '').trim();
                global.document?.dispatchEvent?.(new CustomEvent('geminiApiKeyChanged', {
                    detail: { hasKey: Boolean(inMemoryKey) }
                }));
            },
            clear: () => {
                inMemoryKey = '';
                global.document?.dispatchEvent?.(new CustomEvent('geminiApiKeyChanged', {
                    detail: { hasKey: false }
                }));
            }
        };
    }

    const keyProvider = resolveKeyProvider();

    function getApiKey() {
        if (keyProvider.get) {
            return keyProvider.get();
        }
        return '';
    }

    function hasApiKey() {
        return Boolean(getApiKey());
    }

    function resolveModel(model) {
        if (!model) {
            return DEFAULT_MODEL;
        }
        const trimmed = model.trim();
        return trimmed.startsWith('models/') ? trimmed.substring('models/'.length) : trimmed;
    }

    function normalizeParts(parts) {
        if (!Array.isArray(parts)) {
            return [];
        }
        return parts.map((part) => {
            if (!part) {
                return null;
            }
            if (typeof part === 'string') {
                return { text: part };
            }
            if (typeof part.text === 'string') {
                return { text: part.text };
            }
            return null;
        }).filter(Boolean);
    }

    function normalizeMessage(message) {
        if (!message) {
            return null;
        }
        const role = message.role || 'user';
        const parts = normalizeParts(message.parts || (typeof message.text === 'string' ? [message.text] : []));
        if (!parts.length) {
            return null;
        }
        return { role, parts };
    }

    function buildBody(options) {
        const { messages = [], systemInstruction, generationConfig } = options || {};
        const normalizedMessages = messages.map(normalizeMessage).filter(Boolean);
        if (!normalizedMessages.length) {
            throw new Error('유효한 메시지가 없어 AI 요청을 보낼 수 없습니다.');
        }

        const body = {
            contents: normalizedMessages,
        };

        if (systemInstruction) {
            body.system_instruction = normalizeMessage({ role: 'system', parts: [systemInstruction] });
        }

        if (generationConfig && typeof generationConfig === 'object') {
            body.generationConfig = generationConfig;
        }

        return body;
    }

    function extractCandidateText(candidate) {
        if (!candidate) {
            return '';
        }
        const parts = candidate.content?.parts || [];
        return parts
            .map((part) => (typeof part.text === 'string' ? part.text : ''))
            .join('')
            .trim();
    }

    function extractFinishReason(candidate) {
        if (!candidate) {
            return null;
        }
        return candidate.finishReason || candidate.finish_reason || null;
    }

    async function generateContent(options) {
        const apiKey = getApiKey();
        if (!apiKey) {
            const error = new Error('API_KEY_MISSING');
            error.code = 'API_KEY_MISSING';
            throw error;
        }

        const model = resolveModel(options?.model);
        const body = buildBody(options);
        const url = `${API_BASE}/${encodeURIComponent(model)}:generateContent?key=${encodeURIComponent(apiKey)}`;

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'x-goog-api-key': apiKey,
            },
            body: JSON.stringify(body)
        });

        let data = null;
        try {
            data = await response.json();
        } catch (error) {
            const parseError = new Error('AI 응답을 해석할 수 없습니다. 잠시 후 다시 시도해 주세요.');
            parseError.cause = error;
            throw parseError;
        }

        if (!response.ok) {
            const message = data?.error?.message || `API 요청 실패: ${response.status} ${response.statusText}`;
            const apiError = new Error(message);
            apiError.code = data?.error?.status || response.status;
            apiError.data = data;
            throw apiError;
        }

        const candidate = data?.candidates?.[0];
        const text = extractCandidateText(candidate);
        const finishReason = extractFinishReason(candidate);

        if (!text) {
            const message = finishReason === 'SAFETY'
                ? 'Google AI 안전성 정책에 따라 답변이 차단되었습니다. 질문을 조금 다르게 표현해 보세요.'
                : 'AI 응답을 해석할 수 없습니다. 잠시 후 다시 시도해 주세요.';
            const emptyError = new Error(message);
            emptyError.finishReason = finishReason;
            emptyError.data = data;
            throw emptyError;
        }

        return {
            text,
            finishReason,
            data,
        };
    }

    global.GeminiClient = {
        generateContent,
        hasApiKey,
        getApiKey,
    };
})(window);
