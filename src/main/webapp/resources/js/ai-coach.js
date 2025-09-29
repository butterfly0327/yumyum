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
        const recommendationList = document.getElementById('recommendation-list');

        const GEMINI_ENDPOINT = 'https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent';

        marked.setOptions({
            breaks: true,
        });

        function hasApiKey() {
            return Boolean(appUtils.getGeminiApiKey());
        }

        function buildGeminiUrl() {
            const apiKey = appUtils.getGeminiApiKey();
            if (!apiKey) {
                throw new Error('API_KEY_MISSING');
            }
            return `${GEMINI_ENDPOINT}?key=${encodeURIComponent(apiKey)}`;
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

        function buildCoachingPrompt(userPrompt) {
            return `당신은 사용자에게 명확하고 이해하기 쉬운 답변을 제공하는 전문 AI 코치입니다.\n답변의 가독성을 높이기 위해, 핵심 내용은 **굵은 글씨**로, 목록은 글머리 기호(*)를 사용하여 깔끔하게 정리해 주세요.\n모든 답변은 한국어로 작성합니다.\n사용자의 질문은 다음과 같습니다: ${userPrompt}`;
        }

        function showRecommendationPlaceholder() {
            if (recommendationList) {
                recommendationList.innerHTML = '<li class="list-group-item text-muted">API 키를 설정하면 추천을 받을 수 있습니다.</li>';
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

        async function getAIResponse(userPrompt) {
            let loadingMessage;
            try {
                const apiUrl = buildGeminiUrl();
                loadingMessage = addLoadingMessage();

                const response = await fetch(apiUrl, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        contents: [{
                            parts: [{ text: buildCoachingPrompt(userPrompt) }]
                        }]
                    })
                });

                const data = await response.json();
                removeLoadingMessage(loadingMessage);

                if (data?.candidates?.[0]?.content?.parts?.[0]?.text) {
                    addMessage(data.candidates[0].content.parts[0].text, 'ai');
                } else if (data?.error?.message) {
                    console.error('API Error:', data.error.message);
                    addMessage(`죄송합니다. 오류가 발생했습니다: ${data.error.message}`, 'ai');
                } else {
                    addMessage('죄송합니다. 예상치 못한 오류로 응답을 받을 수 없습니다.', 'ai');
                }
            } catch (error) {
                removeLoadingMessage(loadingMessage);
                if (error.message === 'API_KEY_MISSING') {
                    addMessage('Gemini API 키가 설정되지 않았습니다. 키를 입력한 뒤 다시 시도해 주세요.', 'ai');
                    updateUiByApiKey();
                } else {
                    console.error('Error:', error);
                    addMessage('죄송합니다. 오류가 발생하여 응답을 받을 수 없습니다.', 'ai');
                }
            } finally {
                sendBtn.disabled = !hasApiKey();
                sendBtn.innerHTML = '전송';
            }
        }

        function getDailyRecommendation() {
            if (!hasApiKey()) {
                showRecommendationPlaceholder();
                return;
            }

            fetch(buildGeminiUrl(), {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    contents: [{
                        parts: [{ text: "오늘의 식단 추천, 칼로리 계산 방법, 단백질 섭취 팁, 식단 계획 세우기 중에서 랜덤으로 하나를 선택하여 '오늘의 AI 추천' 제목에 어울리게 한 문장으로 간결하게 요약해줘." }]
                    }]
                })
            })
            .then(response => response.json())
            .then(data => {
                if (data?.candidates?.[0]?.content?.parts?.[0]?.text) {
                    recommendationList.innerHTML = `<li class="list-group-item">${data.candidates[0].content.parts[0].text}</li>`;
                } else {
                    recommendationList.innerHTML = `<li class="list-group-item text-danger">추천을 불러오는 데 실패했습니다.</li>`;
                }
            })
            .catch(error => {
                console.error('Error fetching daily recommendation:', error);
                recommendationList.innerHTML = `<li class="list-group-item text-danger">추천을 불러오는 데 실패했습니다.</li>`;
            });
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
                getDailyRecommendation();
            } else {
                showApiKeyNotice();
                showRecommendationPlaceholder();
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
