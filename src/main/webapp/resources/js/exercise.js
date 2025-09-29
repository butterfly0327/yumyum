document.addEventListener('DOMContentLoaded', () => {
    (async () => {
        try {
            const status = await appUtils.requireLogin();
            await updateAuthButtons();
            initializeExercise(status.username);
        } catch (error) {
            console.error(error);
        }
    })();

    function initializeExercise(username) {
        const exerciseForm = document.getElementById('exercise-form');
        const aiResponseEl = document.getElementById('ai-response');
        const saveSectionEl = document.getElementById('save-section');
        const caloriesToSaveEl = document.getElementById('calories-to-save');
        const saveExerciseBtn = document.getElementById('save-exercise-btn');
        const totalWeeklyCaloriesEl = document.getElementById('total-weekly-calories');
        const exerciseDateInput = document.getElementById('exercise-date');
        const apiKeyForm = document.getElementById('api-key-form');
        const apiKeyInput = document.getElementById('api-key-input');
        const apiKeyStatus = document.getElementById('api-key-status');

        if (exerciseDateInput) {
            exerciseDateInput.valueAsDate = new Date();
        }

        let geminiApiKey = '';
        let chartInstance;
        let extractedCalories = 0;

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

        exerciseForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            if (!ensureApiKey()) {
                return;
            }

            const prompt = document.getElementById('exercise-prompt').value;
            aiResponseEl.innerHTML = '<div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div>';
            saveSectionEl.classList.add('d-none');

            const coachingPrompt = `너는 사용자의 운동 칼로리를 계산해주는 전문 AI 코치야. 다음 운동 내용에 대해 소모 칼로리 값을 오직 숫자만으로 알려줘. 다른 설명이나 단위(kcal)는 절대 포함하지 마. 만약 계산이 불가능하거나 알 수 없다면 0을 반환해. 운동 내용: ${prompt}`;

            try {
                const data = await appUtils.apiFetch('/api/ai/exercise', {
                    method: 'POST',
                    body: {
                        apiKey: geminiApiKey,
                        prompt: coachingPrompt
                    }
                });

                if (data && data.success && typeof data.calories === 'number') {
                    extractedCalories = data.calories;
                    aiResponseEl.innerHTML = `<p>예상 소모 칼로리는 <strong>${extractedCalories} kcal</strong> 입니다!</p>`;
                    caloriesToSaveEl.textContent = extractedCalories;
                    saveSectionEl.classList.remove('d-none');
                } else {
                    const message = data && data.message ? data.message : 'AI가 유효한 칼로리 값을 반환하지 못했습니다.';
                    aiResponseEl.innerHTML = '';
                    const errorParagraph = document.createElement('p');
                    errorParagraph.classList.add('text-danger');
                    errorParagraph.textContent = message;
                    aiResponseEl.appendChild(errorParagraph);
                    if (data && data.rawResponse) {
                        const rawInfo = document.createElement('small');
                        rawInfo.classList.add('text-muted');
                        rawInfo.textContent = `AI 응답: ${data.rawResponse}`;
                        aiResponseEl.appendChild(rawInfo);
                    }
                    saveSectionEl.classList.add('d-none');
                }
            } catch (error) {
                aiResponseEl.innerHTML = '';
                const errorParagraph = document.createElement('p');
                errorParagraph.classList.add('text-danger');
                errorParagraph.textContent = `AI 요청 중 오류가 발생했습니다. ${error.message}`;
                aiResponseEl.appendChild(errorParagraph);
                saveSectionEl.classList.add('d-none');
            }
        });

        saveExerciseBtn.addEventListener('click', async () => {
            const exerciseDate = exerciseDateInput.value;
            const newRecord = {
                date: exerciseDate,
                calories: extractedCalories
            };

            try {
                await appUtils.apiFetch('/api/exercise-records', {
                    method: 'POST',
                    body: newRecord
                });
                alert('운동 기록이 성공적으로 저장되었습니다!');
                loadAndDisplayWeeklyCalories();
                saveSectionEl.classList.add('d-none');
                document.getElementById('exercise-prompt').value = '';
                aiResponseEl.innerHTML = '<p class="text-muted" id="ai-placeholder">여기에 AI의 답변이 표시됩니다.</p>';
                extractedCalories = 0;
            } catch (error) {
                alert('운동 기록 저장 실패: ' + error.message);
            }
        });

        async function loadAndDisplayWeeklyCalories() {
            try {
                const records = await appUtils.apiFetch(`/api/exercise-records?username=${encodeURIComponent(username)}`, {
                    method: 'GET',
                    headers: { 'Content-Type': 'application/json' }
                });
                const weeklyData = getWeeklyData(records);
                const totalCalories = weeklyData.reduce((sum, day) => sum + day, 0);
                totalWeeklyCaloriesEl.textContent = `${totalCalories} kcal`;
                renderChart(weeklyData);
            } catch (error) {
                console.error('주간 칼로리 데이터를 불러오는 중 오류 발생:', error);
            }
        }

        function getWeeklyData(records) {
            const now = new Date();
            const startOfWeek = new Date(now);
            startOfWeek.setDate(now.getDate() - (now.getDay() === 0 ? 6 : now.getDay() - 1));
            startOfWeek.setHours(0, 0, 0, 0);

            const endOfWeek = new Date(startOfWeek);
            endOfWeek.setDate(startOfWeek.getDate() + 6);
            endOfWeek.setHours(23, 59, 59, 999);

            const weeklyCalories = new Array(7).fill(0);
            records.forEach((record) => {
                const recordDate = new Date(record.date);
                if (recordDate >= startOfWeek && recordDate <= endOfWeek && record.username === username) {
                    const dayOfWeek = recordDate.getDay();
                    const index = dayOfWeek === 0 ? 6 : dayOfWeek - 1;
                    weeklyCalories[index] += record.calories;
                }
            });
            return weeklyCalories;
        }

        function renderChart(data) {
            const ctx = document.getElementById('weekly-calories-chart').getContext('2d');
            if (chartInstance) {
                chartInstance.destroy();
            }
            chartInstance = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: ['월', '화', '수', '목', '금', '토', '일'],
                    datasets: [{
                        label: '소모 칼로리 (kcal)',
                        data,
                        backgroundColor: 'rgba(54, 162, 235, 0.5)',
                        borderColor: 'rgba(54, 162, 235, 1)',
                        borderWidth: 1
                    }]
                },
                options: {
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                }
            });
        }

        loadAndDisplayWeeklyCalories();
    }
});
