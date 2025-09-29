document.addEventListener('DOMContentLoaded', () => {
    updateAuthButtons();

    (async () => {
        try {
            await appUtils.requireLogin();
            initializePage();
        } catch (error) {
            console.error(error);
        }
    })();

    function initializePage() {
        const form = document.getElementById('diet-record-form');
        const recordsContainer = document.getElementById('diet-records-container');
        const today = new Date().toISOString().split('T')[0];
        document.getElementById('date').value = today;

        async function loadDietRecords() {
            try {
                const data = await appUtils.apiFetch('/api/diet', { method: 'GET', headers: { 'Content-Type': 'application/json' } });
                data.sort((a, b) => new Date(b.date) - new Date(a.date));
                const latestRecords = data.slice(0, 10);

                recordsContainer.innerHTML = '';
                const groupedData = latestRecords.reduce((acc, current) => {
                    const date = current.date;
                    if (!acc[date]) {
                        acc[date] = [];
                    }
                    acc[date].push(current);
                    return acc;
                }, {});

                Object.keys(groupedData).forEach((date) => {
                    const dateRecords = groupedData[date];
                    const dateHeader = document.createElement('h4');
                    dateHeader.textContent = date;
                    recordsContainer.appendChild(dateHeader);

                    dateRecords.forEach((meal) => {
                        const mealHeader = document.createElement('h5');
                        mealHeader.textContent = meal.mealType;
                        recordsContainer.appendChild(mealHeader);

                        const table = document.createElement('table');
                        table.className = 'table table-bordered mb-4';
                        table.innerHTML = `
                            <thead class="table-light">
                                <tr>
                                    <th>음식</th>
                                    <th>칼로리</th>
                                    <th>단백질(g)</th>
                                    <th>탄수화물(g)</th>
                                    <th>지방(g)</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                ${meal.foods.map((food, index) => `
                                    <tr>
                                        <td>${food.name}</td>
                                        <td>${food.energy.toFixed(1)} kcal</td>
                                        <td>${food.protein.toFixed(1)} g</td>
                                        <td>${food.carbohydrate.toFixed(1)} g</td>
                                        <td>${food.fat.toFixed(1)} g</td>
                                        <td><button class="btn btn-danger btn-sm delete-food-btn" data-meal-id="${meal.id}" data-food-index="${index}">삭제</button></td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        `;
                        recordsContainer.appendChild(table);
                    });
                });

                document.querySelectorAll('.delete-food-btn').forEach(button => {
                    button.addEventListener('click', async (event) => {
                        const mealId = event.target.dataset.mealId;
                        const foodIndex = event.target.dataset.foodIndex;
                        if (confirm('정말로 이 음식 항목을 삭제하시겠습니까?')) {
                            await deleteFoodItem(mealId, foodIndex);
                            loadDietRecords();
                        }
                    });
                });
            } catch (error) {
                console.error('데이터를 불러오는 중 오류 발생:', error);
                recordsContainer.innerHTML = '<p class="text-danger">식단 데이터를 불러오는 데 실패했습니다.</p>';
            }
        }

        async function deleteFoodItem(mealId, foodIndex) {
            try {
                await appUtils.apiFetch(`/api/diet/${mealId}/food/${foodIndex}`, {
                    method: 'DELETE'
                });
                alert('음식 항목이 성공적으로 삭제되었습니다.');
            } catch (error) {
                alert('음식 항목 삭제 실패: ' + error.message);
            }
        }

        loadDietRecords();

        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            const newRecord = {
                date: document.getElementById('date').value,
                mealType: document.getElementById('meal-type').value,
                foods: [
                    {
                        code: 'manual',
                        name: document.getElementById('food-name').value,
                        energy: parseFloat(document.getElementById('calories').value),
                        protein: parseFloat(document.getElementById('protein').value),
                        carbohydrate: parseFloat(document.getElementById('carbohydrate').value),
                        fat: parseFloat(document.getElementById('fat').value),
                        weight: 'manual'
                    }
                ]
            };
            try {
                await appUtils.apiFetch('/api/diet', {
                    method: 'POST',
                    body: newRecord
                });
                alert('식단 기록이 성공적으로 추가되었습니다.');
                form.reset();
                document.getElementById('date').value = today;
                loadDietRecords();
            } catch (error) {
                alert('식단 기록 추가 실패: ' + error.message);
            }
        });
    }
});
