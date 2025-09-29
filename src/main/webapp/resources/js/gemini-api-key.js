(function () {
    document.addEventListener('DOMContentLoaded', () => {
        if (!window.appUtils) {
            return;
        }

        const input = document.getElementById('gemini-api-key-input');
        const status = document.getElementById('gemini-api-key-status');

        if (!input) {
            return;
        }

        let lastHasKey = Boolean(window.appUtils.getGeminiApiKey());

        function updateStatus(hasKey, variant) {
            if (!status) {
                return;
            }
            const resolvedVariant = variant || (hasKey ? 'success' : 'muted');
            status.className = `form-text text-${resolvedVariant}`;
            status.textContent = hasKey
                ? '입력한 Gemini API 키가 자동으로 사용됩니다. (페이지를 새로고침하면 초기화됩니다.)'
                : 'Gemini API 키를 입력하면 AI 기능을 사용할 수 있습니다.';
        }

        function applyValue(rawValue) {
            const trimmed = (rawValue || '').trim();
            if (trimmed) {
                lastHasKey = true;
                window.appUtils.setGeminiApiKey(trimmed);
                updateStatus(true);
            } else {
                const hadKeyBefore = lastHasKey;
                lastHasKey = false;
                window.appUtils.clearGeminiApiKey();
                updateStatus(false, hadKeyBefore ? 'warning' : 'muted');
            }
        }

        input.addEventListener('input', () => {
            applyValue(input.value);
        });

        document.addEventListener('geminiApiKeyChanged', (event) => {
            const hasKey = Boolean(event?.detail?.hasKey);
            lastHasKey = hasKey;
            const newValue = hasKey ? window.appUtils.getGeminiApiKey() : '';
            if ((input.value || '').trim() !== newValue) {
                input.value = newValue || '';
            }
            updateStatus(hasKey, hasKey ? 'success' : 'warning');
        });

        const initialInputValue = (input.value || '').trim();
        if (initialInputValue) {
            applyValue(initialInputValue);
        } else {
            const storedValue = window.appUtils.getGeminiApiKey();
            if (storedValue) {
                lastHasKey = true;
                input.value = storedValue;
                updateStatus(true);
            } else {
                updateStatus(false);
            }
        }
    });
})();
