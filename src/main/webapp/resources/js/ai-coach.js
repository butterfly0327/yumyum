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
        const apiKeyForm = document.getElementById('api-key-form');
        const apiKeyInput = document.getElementById('api-key-input');
        const apiKeyStatus = document.getElementById('api-key-status');

        let geminiApiKey = '';

        if (typeof marked !== 'undefined') {
            marked.setOptions({ breaks: true });
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

        function updateApiKeyStatus(message, type = 'muted') {
            if (!apiKeyStatus) {
                return;
            }
            apiKeyStatus.textContent = message;
            apiKeyStatus.classList.remove('text-muted', 'text-success', 'text-danger');
            apiKeyStatus.classList.add(`text-${type}`);
        }

        if (apiKeyForm) {
            apiKeyForm.addEventListener('submit', (event) => {
                event.preventDefault();
                const value = apiKeyInput.value.trim();
                if (!value) {
                    updateApiKeyStatus('API 키를 입력해주세요.', 'danger');
                    apiKeyInput.focus();
                    return;
                }
                geminiApiKey = value;
                apiKeyInput.value = '';
                apiKeyInput.blur();
                updateApiKeyStatus('API 키가 설정되었습니다. 질문을 전송하면 해당 요청에만 사용됩니다.', 'success');
            });
        }

        function ensureApiKey() {
            if (!geminiApiKey) {
                updateApiKeyStatus('AI 기능을 사용하려면 먼저 API 키를 설정해주세요.', 'danger');
                if (apiKeyInput) {
                    apiKeyInput.focus();
                }
                return false;
            }
            return true;
        }

        async function getAIResponse(userPrompt) {
            if (!ensureApiKey()) {
                return;
            }

            const coachingPrompt = `당신은 사용자에게 명확하고 이해하기 쉬운 답변을 제공하는 전문 AI 코치입니다.\n답변의 가독성을 높이기 위해, 핵심 내용은 **굵은 글씨**로, 목록은 글머리 기호(*)를 사용하여 깔끔하게 정리해 주세요.\n모든 답변은 한국어로 작성합니다.\n사용자의 질문은 다음과 같습니다: ${userPrompt}`;

            const loadingMessage = addMessage('... 응답을 생성하는 중 ...', 'ai');
            sendBtn.disabled = true;
            sendBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';

            try {
                const data = await appUtils.apiFetch('/api/ai/coach', {
                    method: 'POST',
                    body: {
                        apiKey: geminiApiKey,
                        prompt: coachingPrompt
                    }
                });

                if (data && data.success && data.content) {
                    loadingMessage.remove();
                    addMessage(data.content, 'ai');
                } else {
                    const message = data && data.message ? data.message : '응답을 불러오지 못했습니다.';
                    loadingMessage.classList.add('text-danger');
                    loadingMessage.textContent = `죄송합니다. ${message}`;
                }
            } catch (error) {
                loadingMessage.classList.add('text-danger');
                loadingMessage.textContent = `죄송합니다. 오류가 발생했습니다: ${error.message}`;
            } finally {
                sendBtn.disabled = false;
                sendBtn.innerHTML = '전송';
            }
        }

        addMessage('안녕하세요! Gemini API 키를 설정한 뒤 궁금한 내용을 물어보세요.', 'ai');

        chatForm.addEventListener('submit', (event) => {
            event.preventDefault();
            const question = userInput.value.trim();
            if (!question) {
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
                addMessage(question, 'user');
                getAIResponse(question);
            });
        });
    }
});
