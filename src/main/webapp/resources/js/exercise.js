document.addEventListener('DOMContentLoaded', () => {
    (async () => {
        try {
            const status = await appUtils.requireLogin();
            updateAuthButtons();
            initializeExercise(status.username);
        } catch (error) {
            console.error(error);
        }
    })();

    function initializeExercise(username) {
        const exerciseForm = document.getElementById('exercise-form');
        const exercisePromptInput = document.getElementById('exercise-prompt');
        const exerciseDateInput = document.getElementById('exercise-date');
        const submitButton = exerciseForm.querySelector('button[type="submit"]');
        const aiResponseEl = document.getElementById('ai-response');
        const saveSectionEl = document.getElementById('save-section');
        const caloriesToSaveEl = document.getElementById('calories-to-save');
        const saveExerciseBtn = document.getElementById('save-exercise-btn');
        const totalWeeklyCaloriesEl = document.getElementById('total-weekly-calories');

        exerciseDateInput.valueAsDate = new Date();
        let chartInstance;
        let extractedCalories = 0;

        function hasApiKey() {
            return Boolean(window.GeminiClient?.hasApiKey?.());
        }

        function showMissingKeyMessage() {
            aiResponseEl.innerHTML = '<p class="text-muted api-key-alert">Gemini API 키를 설정하면 AI 응답을 받을 수 있습니다.</p>';
            saveSectionEl.classList.add('d-none');
        }

        function updateUiByApiKey() {
            const available = hasApiKey();
            submitButton.disabled = !available;

            if (available) {
                if (!aiResponseEl.querySelector('#ai-placeholder') && !aiResponseEl.querySelector('.text-danger') && !aiResponseEl.querySelector('.spinner-border')) {
                    aiResponseEl.innerHTML = '<p class="text-muted" id="ai-placeholder">여기에 AI의 답변이 표시됩니다.</p>';
                }
            } else {
                showMissingKeyMessage();
            }
        }

        exerciseForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            const prompt = exercisePromptInput.value.trim();
            if (!prompt) {
                return;
            }

            if (!hasApiKey()) {
                alert('AI 기능을 사용하려면 Gemini API 키를 입력해 주세요.');
                updateUiByApiKey();
                return;
            }

            aiResponseEl.innerHTML = '<div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div>';
            saveSectionEl.classList.add('d-none');
            submitButton.disabled = true;

            try {
                if (!window.GeminiClient || typeof window.GeminiClient.generateContent !== 'function') {
                    const error = new Error('AI 클라이언트를 불러오지 못했습니다. 페이지를 새로고침한 뒤 다시 시도해 주세요.');
                    error.code = 'CLIENT_MISSING';
                    throw error;
                }
                const response = await window.GeminiClient.generateContent({
                    systemInstruction: [
                        '너는 사용자의 운동 기록을 보고 소모 칼로리를 계산하는 전문 AI 코치야.',
                        '반드시 숫자만으로 결과를 알려 주고, 단위(kcal)나 다른 설명은 절대 포함하지 마.',
                        '정확한 계산이 어렵다면 0을 반환해.'
                    ].join(' '),
                    messages: [
                        {
                            role: 'user',
                            parts: [prompt]
                        }
                    ]
                });
                const aiText = response?.text || '';
                const numericValue = Number(aiText?.match(/\d+/g)?.join(''));

                if (!isNaN(numericValue) && numericValue >= 0) {
                    extractedCalories = numericValue;
                    aiResponseEl.innerHTML = `<p>예상 소모 칼로리는 <strong>${extractedCalories} kcal</strong> 입니다!</p>`;
                    caloriesToSaveEl.textContent = extractedCalories;
                    saveSectionEl.classList.remove('d-none');
                } else {
                    aiResponseEl.innerHTML = `<p class="text-danger">AI가 유효한 칼로리 값을 계산하지 못했습니다.<br>AI 응답: ${aiText}</p>`;
                    saveSectionEl.classList.add('d-none');
                }
            } catch (error) {
                console.error('Error:', error);
                if (error.code === 'API_KEY_MISSING') {
                    alert('Gemini API 키가 설정되지 않았습니다. 키를 입력해 주세요.');
                    showMissingKeyMessage();
                } else if (error.code === 429 || error.code === 'RESOURCE_EXHAUSTED') {
                    aiResponseEl.innerHTML = '<p class="text-danger">API 사용량 한도가 초과되었습니다. 잠시 후 다시 시도해 주세요.</p>';
                } else if (error.code === 403 || error.code === 'PERMISSION_DENIED') {
                    aiResponseEl.innerHTML = '<p class="text-danger">API 키 권한이 부족합니다. 키 제한 설정을 확인한 뒤 다시 시도해 주세요.</p>';
                } else if (error.code === 'CLIENT_MISSING') {
                    aiResponseEl.innerHTML = `<p class="text-danger">${error.message}</p>`;
                } else {
                    const message = error.message || 'AI 요청 중 오류가 발생했습니다.';
                    aiResponseEl.innerHTML = `<p class="text-danger">AI 요청 중 오류가 발생했습니다.<br>오류 상세: ${message}</p>`;
                }
                saveSectionEl.classList.add('d-none');
            } finally {
                submitButton.disabled = !hasApiKey();
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
                exercisePromptInput.value = '';
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
            records.forEach(record => {
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
        updateUiByApiKey();
        document.addEventListener('geminiApiKeyChanged', updateUiByApiKey);

        if (!hasApiKey()) {
            showMissingKeyMessage();
        }
    }
});
