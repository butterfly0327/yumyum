document.addEventListener('DOMContentLoaded', () => {
    updateAuthButtons();

    (async () => {
        try {
            await appUtils.requireLogin();
            initializeCoach();
        } catch (error) {
            console.error(error);
        }
    })();

    function initializeCoach() {
        const chatContainer = document.getElementById('chat-container');
        const chatForm = document.getElementById('chat-form');
        const userInput = document.getElementById('user-input');
        const sendBtn = document.getElementById('send-btn');
        const quickQuestionBtns = document.querySelectorAll('.quick-question-btn');

        marked.setOptions({
            breaks: true,
        });

        function hasApiKey() {
            return Boolean(window.GeminiClient?.hasApiKey?.());
        }

        function addMessage(text, sender) {
            const messageDiv = document.createElement('div');
            messageDiv.classList.add('message-bubble');
            messageDiv.classList.add(sender === 'user' ? 'user-message' : 'ai-message');

            if (typeof marked !== 'undefined') {
                messageDiv.innerHTML = marked.parse(text);
            } else {
                messageDiv.textContent = text;
            }

            chatContainer.appendChild(messageDiv);
            chatContainer.scrollTop = chatContainer.scrollHeight;
            return messageDiv;
        }

        function addLoadingMessage() {
            const loadingMessage = document.createElement('div');
            loadingMessage.classList.add('message-bubble', 'ai-message', 'loading-message');
            loadingMessage.textContent = '... 응답을 생성하는 중 ...';
            chatContainer.appendChild(loadingMessage);
            chatContainer.scrollTop = chatContainer.scrollHeight;
            sendBtn.disabled = true;
            sendBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';
            return loadingMessage;
        }

        function removeLoadingMessage(messageEl) {
            if (messageEl && chatContainer.contains(messageEl)) {
                chatContainer.removeChild(messageEl);
            }
        }

        function showApiKeyNotice() {
            if (chatContainer.querySelector('.api-key-required')) {
                return;
            }
            const notice = document.createElement('div');
            notice.classList.add('message-bubble', 'ai-message', 'api-key-required');
            notice.textContent = 'Gemini API 키를 입력하면 AI 코치 기능을 사용할 수 있습니다.';
            chatContainer.appendChild(notice);
            chatContainer.scrollTop = chatContainer.scrollHeight;
        }

        function clearApiKeyNotice() {
            const existingNotice = chatContainer.querySelector('.api-key-required');
            if (existingNotice) {
                chatContainer.removeChild(existingNotice);
            }
        }

        async function getAIResponse(userPrompt) {
            let loadingMessage;

            try {
                loadingMessage = addLoadingMessage();
                if (!window.GeminiClient || typeof window.GeminiClient.generateContent !== 'function') {
                    const error = new Error('AI 클라이언트를 불러오지 못했습니다. 페이지를 새로고침한 뒤 다시 시도해 주세요.');
                    error.code = 'CLIENT_MISSING';
                    throw error;
                }
                const response = await window.GeminiClient.generateContent({
                    systemInstruction: [
                        '당신은 사용자에게 명확하고 이해하기 쉬운 답변을 제공하는 전문 AI 코치입니다.',
                        '핵심 내용은 **굵게**, 목록은 글머리 기호(*)로 정리해 주세요.',
                        '건강에 해롭거나 극단적인 조언은 피하고, 균형 잡힌 식습관과 안전한 운동을 권장하세요.',
                        '모든 답변은 한국어로 작성합니다.'
                    ].join('\n'),
                    messages: [
                        {
                            role: 'user',
                            parts: [userPrompt]
                        }
                    ]
                });
                const aiText = response?.text || '';
                removeLoadingMessage(loadingMessage);
                addMessage(aiText, 'ai');
            } catch (error) {
                if (loadingMessage) {
                    removeLoadingMessage(loadingMessage);
                }

                let friendlyMessage;
                if (error.code === 'API_KEY_MISSING') {
                    friendlyMessage = 'Gemini API 키가 설정되지 않았습니다. 키를 입력한 뒤 다시 시도해 주세요.';
                    updateUiByApiKey();
                } else if (error.code === 429 || error.code === 'RESOURCE_EXHAUSTED') {
                    friendlyMessage = 'API 사용량 한도를 초과했습니다. 잠시 후 다시 시도해 주세요.';
                } else if (error.code === 403 || error.code === 'PERMISSION_DENIED') {
                    friendlyMessage = 'API 키 권한이 부족합니다. 키 제한 설정을 확인한 뒤 다시 시도해 주세요.';
                } else if (error.code === 'CLIENT_MISSING') {
                    friendlyMessage = error.message;
                } else {
                    console.error('Error fetching AI response:', error);
                    friendlyMessage = `죄송합니다. 오류가 발생했습니다: ${error.message}`;
                }
                addMessage(friendlyMessage, 'ai');
            } finally {
                sendBtn.disabled = !hasApiKey();
                sendBtn.innerHTML = '전송';
            }
        }

        function updateUiByApiKey() {
            const available = hasApiKey();
            userInput.disabled = !available;
            sendBtn.disabled = !available;
            quickQuestionBtns.forEach(btn => {
                btn.disabled = !available;
            });

            if (available) {
                clearApiKeyNotice();
            } else {
                showApiKeyNotice();
            }
        }

        addMessage('안녕하세요! 무엇을 도와드릴까요?', 'ai');
        updateUiByApiKey();

        document.addEventListener('geminiApiKeyChanged', updateUiByApiKey);

        chatForm.addEventListener('submit', (event) => {
            event.preventDefault();
            const question = userInput.value.trim();
            if (!question) {
                return;
            }

            if (!hasApiKey()) {
                alert('AI 코치 기능을 사용하려면 Gemini API 키를 입력해 주세요.');
                updateUiByApiKey();
                return;
            }

            addMessage(question, 'user');
            getAIResponse(question);
            userInput.value = '';
        });

        quickQuestionBtns.forEach((btn) => {
            btn.addEventListener('click', () => {
                const question = btn.dataset.question;
                if (!question) {
                    return;
                }

                if (!hasApiKey()) {
                    alert('AI 코치 기능을 사용하려면 Gemini API 키를 입력해 주세요.');
                    updateUiByApiKey();
                    return;
                }

                addMessage(question, 'user');
                getAIResponse(question);
            });
        });
    }
});
