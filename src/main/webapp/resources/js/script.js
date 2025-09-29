document.addEventListener('DOMContentLoaded', () => {
    updateAuthButtons();

    (async () => {
        try {
            await appUtils.requireLogin();
            const dietData = await appUtils.apiFetch('/api/diet', { method: 'GET', headers: { 'Content-Type': 'application/json' } });

            const nutritionGoals = {
                calorie: 2000,
                protein: 100,
                carbohydrate: 250,
                fat: 65
            };

            const todayDate = new Date();
            const year = todayDate.getFullYear();
            const month = String(todayDate.getMonth() + 1).padStart(2, '0');
            const day = String(todayDate.getDate()).padStart(2, '0');
            const today = `${year}-${month}-${day}`;

            const todayDayOfWeek = todayDate.getDay();
            const daysToMonday = todayDayOfWeek === 0 ? 6 : todayDayOfWeek - 1;
            const startOfWeek = new Date(todayDate);
            startOfWeek.setDate(todayDate.getDate() - daysToMonday);
            startOfWeek.setHours(0, 0, 0, 0);

            const endOfWeek = new Date(todayDate);
            endOfWeek.setHours(23, 59, 59, 999);

            const weeklyData = {};
            const weeklyTotals = { calorie: 0, protein: 0, carbohydrate: 0, fat: 0, daysCount: 0 };
            const dailyDetails = {};

            dietData.forEach((diet) => {
                const recordDate = new Date(diet.date);
                const inWeek = recordDate >= startOfWeek && recordDate <= endOfWeek;

                if (inWeek) {
                    if (!weeklyData[diet.date]) {
                        weeklyData[diet.date] = { calorie: 0, protein: 0, carbohydrate: 0, fat: 0 };
                        weeklyTotals.daysCount++;
                    }

                    diet.foods.forEach((food) => {
                        weeklyData[diet.date].calorie += food.energy;
                        weeklyData[diet.date].protein += food.protein;
                        weeklyData[diet.date].carbohydrate += food.carbohydrate;
                        weeklyData[diet.date].fat += food.fat;

                        weeklyTotals.calorie += food.energy;
                        weeklyTotals.protein += food.protein;
                        weeklyTotals.carbohydrate += food.carbohydrate;
                        weeklyTotals.fat += food.fat;
                    });
                }

                if (diet.date === today) {
                    if (!dailyDetails[diet.mealType]) {
                        dailyDetails[diet.mealType] = [];
                    }
                    dailyDetails[diet.mealType].push(...diet.foods);
                }
            });

            const todaySummary = weeklyData[today] || { calorie: 0, protein: 0, carbohydrate: 0, fat: 0 };
            updateDailySummary(todaySummary, nutritionGoals);
            updateDailyDetails(dailyDetails);
            updateWeeklyStats(weeklyTotals);
            updateChart(weeklyData);
        } catch (error) {
            console.error('데이터를 불러오는 중 오류 발생:', error);
            document.body.innerHTML = '<h1>데이터를 불러오는 데 실패했습니다.</h1>';
        }
    })();

    function updateDailySummary(summary, goals) {
        const dailySummaryEl = document.getElementById('daily-summary');
        dailySummaryEl.innerHTML = `
            <div class="progress-item mb-2">
                <label>총 칼로리</label>
                <div class="progress" style="height: 25px;">
                    <div class="progress-bar bg-success" role="progressbar"
                        style="width: ${Math.min(100, (summary.calorie / goals.calorie) * 100)}%;">
                        ${summary.calorie.toFixed(0)} / ${goals.calorie} kcal
                    </div>
                </div>
            </div>
            <div class="progress-item mb-2">
                <label>단백질</label>
                <div class="progress" style="height: 25px;">
                    <div class="progress-bar bg-info" role="progressbar"
                        style="width: ${Math.min(100, (summary.protein / goals.protein) * 100)}%;">
                        ${summary.protein.toFixed(1)} / ${goals.protein} g
                    </div>
                </div>
            </div>
            <div class="progress-item mb-2">
                <label>탄수화물</label>
                <div class="progress" style="height: 25px;">
                    <div class="progress-bar bg-warning" role="progressbar"
                        style="width: ${Math.min(100, (summary.carbohydrate / goals.carbohydrate) * 100)}%;">
                        ${summary.carbohydrate.toFixed(1)} / ${goals.carbohydrate} g
                    </div>
                </div>
            </div>
            <div class="progress-item mb-2">
                <label>지방</label>
                <div class="progress" style="height: 25px;">
                    <div class="progress-bar bg-danger" role="progressbar"
                        style="width: ${Math.min(100, (summary.fat / goals.fat) * 100)}%;">
                        ${summary.fat.toFixed(1)} / ${goals.fat} g
                    </div>
                </div>
            </div>
        `;
    }

    function updateDailyDetails(details) {
        const dailyDetailsEl = document.getElementById('daily-details');
        dailyDetailsEl.innerHTML = '';
        const mealOrder = ['아침', '점심', '저녁', '간식'];
        mealOrder.forEach(meal => {
            if (details[meal]) {
                const mealRow = document.createElement('tr');
                mealRow.innerHTML = `<td class="fw-bold">[${meal}]</td><td colspan="4"></td>`;
                dailyDetailsEl.appendChild(mealRow);

                details[meal].forEach(food => {
                    const foodRow = document.createElement('tr');
                    foodRow.innerHTML = `
                        <td>${food.name}</td>
                        <td>${food.energy.toFixed(2)} kcal</td>
                        <td>${food.protein.toFixed(1)} g</td>
                        <td>${food.carbohydrate.toFixed(1)} g</td>
                        <td>${food.fat.toFixed(1)} g</td>
                    `;
                    dailyDetailsEl.appendChild(foodRow);
                });
            }
        });
    }

    function updateWeeklyStats(totals) {
        const statsEl = document.getElementById('weekly-stats');
        statsEl.innerHTML = '';
        const statItems = [
            { label: '총 칼로리', value: totals.calorie.toFixed(0), unit: 'kcal', color: 'primary' },
            { label: '단백질', value: totals.protein.toFixed(1), unit: 'g', color: 'success' },
            { label: '탄수화물', value: totals.carbohydrate.toFixed(1), unit: 'g', color: 'warning' },
            { label: '지방', value: totals.fat.toFixed(1), unit: 'g', color: 'danger' }
        ];
        statItems.forEach(stat => {
            const col = document.createElement('div');
            col.className = 'col-6 mb-3';
            col.innerHTML = `
                <div class="card text-center border-${stat.color}">
                    <div class="card-body">
                        <h5 class="card-title">${stat.label}</h5>
                        <p class="card-text fw-bold">${stat.value} ${stat.unit}</p>
                    </div>
                </div>
            `;
            statsEl.appendChild(col);
        });
    }

    function updateChart(weeklyData) {
        const ctx = document.getElementById('nutritionChart');
        const labels = Object.keys(weeklyData);
        const totals = labels.map(date => weeklyData[date]);
        const data = {
            labels,
            datasets: [
                {
                    label: '칼로리',
                    data: totals.map(item => item.calorie),
                    borderColor: '#28a745',
                    backgroundColor: 'rgba(40, 167, 69, 0.3)'
                }
            ]
        };
        new Chart(ctx, {
            type: 'line',
            data,
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }
});
