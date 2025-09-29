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

        const SYSTEM_PROMPT = [
            '당신은 식단과 운동을 도와주는 전문 AI 코치입니다.',
            '모든 답변은 한국어로 작성합니다.',
            '핵심 키워드는 **굵게**, 단계나 목록은 글머리 기호(*)로 보기 좋게 정리해 주세요.',
            '건강에 유해하거나 극단적인 조언은 피하고, 균형 잡힌 식습관과 안전한 운동을 권장하세요.'
        ].join('\n');

        const conversationHistory = [];

        marked.setOptions({
            breaks: true,
        });

        function hasApiKey() {
            return GeminiClient.hasApiKey();
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
            notice.textContent = 'Gemini API 키를 설정하면 AI 코치 기능을 이용할 수 있습니다.';
            chatContainer.appendChild(notice);
            chatContainer.scrollTop = chatContainer.scrollHeight;
        }

        function clearApiKeyNotice() {
            const existingNotice = chatContainer.querySelector('.api-key-required');
            if (existingNotice) {
                chatContainer.removeChild(existingNotice);
            }
        }

        function buildUserMessage(question) {
            return {
                role: 'user',
                parts: [{ text: question }]
            };
        }

        async function getAIResponse(userPrompt) {
            let loadingMessage;
            const historyEntry = buildUserMessage(userPrompt);
            conversationHistory.push(historyEntry);
            if (conversationHistory.length > 12) {
                const overflow = conversationHistory.length - 12;
                const removeCount = overflow % 2 === 0 ? overflow : overflow + 1;
                conversationHistory.splice(0, Math.min(removeCount, conversationHistory.length - 1));
            }

            try {
                loadingMessage = addLoadingMessage();
                const result = await GeminiClient.generateContent({
                    systemInstruction: SYSTEM_PROMPT,
                    messages: conversationHistory,
                });

                removeLoadingMessage(loadingMessage);

                const aiText = result.text;
                addMessage(aiText, 'ai');
                conversationHistory.push({
                    role: 'model',
                    parts: [{ text: aiText }]
                });
            } catch (error) {
                conversationHistory.pop();
                removeLoadingMessage(loadingMessage);

                if (error.code === 'API_KEY_MISSING' || error.message === 'API_KEY_MISSING') {
                    addMessage('Gemini API 키가 설정되지 않았습니다. 키를 입력한 뒤 다시 시도해 주세요.', 'ai');
                    updateUiByApiKey();
                } else if (error.finishReason === 'SAFETY') {
                    addMessage(error.message, 'ai');
                } else {
                    console.error('Error fetching AI response:', error);
                    const friendlyMessage = error.code === 429 || error.code === 'RESOURCE_EXHAUSTED'
                        ? 'API 사용량 한도를 초과했습니다. 잠시 후 다시 시도해 주세요.'
                        : `죄송합니다. 오류가 발생했습니다: ${error.message}`;
                    addMessage(friendlyMessage, 'ai');
                }
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
