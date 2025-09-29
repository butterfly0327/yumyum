document.addEventListener('DOMContentLoaded', () => {
    updateAuthButtons();

    const GEMINI_API_KEY = 'YOUR_API_KEY';

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

    // Gemini API 호출을 위한 기본 URL 및 모델
    const API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;

    // marked.js 줄바꿈 옵션 활성화
    marked.setOptions({
        breaks: true, // 단일 줄바꿈을 <br>로 변환
    });

    // 메시지를 화면에 추가하는 함수
    function addMessage(text, sender) {
        const messageDiv = document.createElement('div');
        messageDiv.classList.add('message-bubble');
        if (sender === 'user') {
            messageDiv.classList.add('user-message');
        } else {
            messageDiv.classList.add('ai-message');
        }
        
        // 마크다운 렌더링을 위해 innerHTML 사용
        if (typeof marked !== 'undefined') {
            messageDiv.innerHTML = marked.parse(text);
        } else {
            messageDiv.textContent = text;
        }

        chatContainer.appendChild(messageDiv);
        chatContainer.scrollTop = chatContainer.scrollHeight;
    }

    // AI 응답을 가져오는 함수 (Gemini API 직접 호출)
    async function getAIResponse(userPrompt) {
        addMessage('... 응답을 생성하는 중 ...', 'ai');
        sendBtn.disabled = true;
        sendBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';
        
        // 가독성 향상 프롬프트 (최적화된 버전)
        const coachingPrompt = `당신은 사용자에게 명확하고 이해하기 쉬운 답변을 제공하는 전문 AI 코치입니다.
답변의 가독성을 높이기 위해, 핵심 내용은 **굵은 글씨**로, 목록은 글머리 기호(*)를 사용하여 깔끔하게 정리해 주세요.
모든 답변은 한국어로 작성합니다.
사용자의 질문은 다음과 같습니다: ${userPrompt}`;

        try {
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    "contents": [{
                        "parts": [{
                            "text": coachingPrompt
                        }]
                    }]
                }),
            });
            
            const data = await response.json();
            
            // 기존 '응답 생성 중' 메시지 제거
            const loadingMessage = chatContainer.querySelector('.ai-message:last-child');
            if (loadingMessage) {
                chatContainer.removeChild(loadingMessage);
            }

            // 응답에서 텍스트 추출 및 에러 처리
            if (data.candidates && data.candidates[0] && data.candidates[0].content && data.candidates[0].content.parts[0]) {
                const aiText = data.candidates[0].content.parts[0].text;
                addMessage(aiText, 'ai');
            } else if (data.error) {
                console.error('API Error:', data.error.message);
                addMessage(`죄송합니다. 오류가 발생했습니다: ${data.error.message}`, 'ai');
            } else {
                addMessage('죄송합니다. 예상치 못한 오류가 발생하여 응답을 받을 수 없습니다.', 'ai');
            }
        } catch (error) {
            console.error('Error:', error);
            const loadingMessage = chatContainer.querySelector('.ai-message:last-child');
            if (loadingMessage) {
                loadingMessage.textContent = '죄송합니다. 오류가 발생하여 응답을 받을 수 없습니다.';
            } else {
                addMessage('죄송합니다. 오류가 발생하여 응답을 받을 수 없습니다.', 'ai');
            }
        } finally {
            sendBtn.disabled = false;
            sendBtn.innerHTML = '전송';
        }
    }

    // 오늘의 AI 추천을 가져오는 함수
    function getDailyRecommendation() {
        // 추천 영역은 간결한 한 문장으로 표시되도록 프롬프트 수정
        const dailyPrompt = "오늘의 식단 추천, 칼로리 계산 방법, 단백질 섭취 팁, 식단 계획 세우기 중에서 랜덤으로 하나를 선택하여 '오늘의 AI 추천' 제목에 어울리게 한 문장으로 간결하게 요약해줘.";
        fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                "contents": [{
                    "parts": [{
                        "text": dailyPrompt
                    }]
                }]
            }),
        })
        .then(response => response.json())
        .then(data => {
            if (data.candidates && data.candidates[0] && data.candidates[0].content && data.candidates[0].content.parts[0]) {
                const aiText = data.candidates[0].content.parts[0].text;
                recommendationList.innerHTML = `<li class="list-group-item">${aiText}</li>`;
            } else {
                recommendationList.innerHTML = `<li class="list-group-item text-danger">추천을 불러오는 데 실패했습니다.</li>`;
            }
        })
        .catch(error => {
            console.error('Error fetching daily recommendation:', error);
            recommendationList.innerHTML = `<li class="list-group-item text-danger">추천을 불러오는 데 실패했습니다.</li>`;
        });
    }

    // 초기 메시지 및 추천 로드
    addMessage('안녕하세요! 무엇을 도와드릴까요?', 'ai');
    getDailyRecommendation();

    // 폼 제출 이벤트 리스너
    chatForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const question = userInput.value.trim();
        if (question) {
            addMessage(question, 'user');
            getAIResponse(question);
            userInput.value = ''; // 입력창 초기화
        }
    });

    // 빠른 질문 버튼 이벤트 리스너
    quickQuestionBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const question = btn.dataset.question;
            addMessage(question, 'user');
            getAIResponse(question);
        });
    });
    }
});