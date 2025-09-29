document.addEventListener('DOMContentLoaded', () => {
    updateAuthButtons();

    (async () => {
        try {
            const status = await appUtils.requireLogin();
            initializeChallenges(status.username);
        } catch (error) {
            console.error(error);
        }
    })();
});

function initializeChallenges(username) {
    const createChallengeForm = document.getElementById('create-challenge-form');
    const ongoingChallengesContainer = document.getElementById('ongoing-challenges');

    createChallengeForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const newChallenge = {
            title: document.getElementById('challenge-title').value,
            type: document.getElementById('challenge-type').value,
            target: parseFloat(document.getElementById('target-value').value),
            duration: parseInt(document.getElementById('duration').value, 10),
            description: document.getElementById('challenge-description').value,
            startDate: new Date().toISOString().split('T')[0]
        };
        try {
            await appUtils.apiFetch('/api/challenges', {
                method: 'POST',
                body: newChallenge
            });
            alert('챌린지가 성공적으로 생성되었습니다.');
            createChallengeForm.reset();
            loadAndDisplayChallenges();
        } catch (error) {
            alert('챌린지 생성 실패: ' + error.message);
        }
    });

    async function loadAndDisplayChallenges() {
        try {
            const challenges = await appUtils.apiFetch('/api/challenges', { method: 'GET', headers: { 'Content-Type': 'application/json' } });
            const dietData = await appUtils.apiFetch('/api/diet', { method: 'GET', headers: { 'Content-Type': 'application/json' } });
            const participantsData = await appUtils.apiFetch('/api/challenges/participants', { method: 'GET', headers: { 'Content-Type': 'application/json' } });

            ongoingChallengesContainer.innerHTML = '';
            if (!challenges.length) {
                ongoingChallengesContainer.innerHTML = '<p class="text-center text-muted">진행 중인 챌린지가 없습니다. 새로운 챌린지를 만들어 보세요!</p>';
                return;
            }

            challenges.forEach(challenge => {
                const totalAchieved = calculateAchievement(challenge, dietData);
                const daysPassed = calculateDaysPassed(challenge);
                const totalTarget = challenge.target * daysPassed;
                let progress = totalTarget === 0 ? 0 : (totalAchieved / totalTarget) * 100;
                if (progress > 100) progress = 100;

                const participantList = participantsData[challenge.id] || [];
                const participantCount = participantList.length;
                const isParticipating = participantList.includes(username);
                const isCreator = username === challenge.creator;

                const deleteBtnHtml = isCreator ? `<button class="btn btn-outline-danger delete-challenge-btn ms-2" data-id="${challenge.id}">삭제</button>` : '';

                const challengeEl = document.createElement('div');
                challengeEl.className = 'card mb-3';
                challengeEl.dataset.id = challenge.id;
                challengeEl.innerHTML = `
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <h5 class="card-title">${challenge.title} (${challenge.type})</h5>
                            <div>
                                <span class="badge bg-secondary participant-count">참여 인원: ${participantCount}</span>
                            </div>
                        </div>
                        <p class="card-subtitle text-muted mb-2">${challenge.description || ''}</p>
                        <div class="d-flex justify-content-between">
                            <span>${Math.round(totalAchieved)} / ${Math.round(totalTarget)} ${challenge.type === '칼로리' ? 'kcal' : 'g'}</span>
                            <span>${formatDate(challenge.startDate)} ~ ${formatDate(addDays(challenge.startDate, challenge.duration))}</span>
                        </div>
                        <div class="progress mt-2">
                            <div class="progress-bar" role="progressbar" style="width: ${progress}%;" aria-valuenow="${progress}" aria-valuemin="0" aria-valuemax="100">
                                ${progress.toFixed(1)}%
                            </div>
                        </div>
                        <div class="mt-3 text-end">
                            ${!isParticipating ? `<button class="btn btn-primary join-challenge-btn" data-id="${challenge.id}">참여하기</button>` : `<button class="btn btn-secondary" disabled>참여 완료</button>`}
                            ${deleteBtnHtml}
                        </div>
                    </div>
                `;
                ongoingChallengesContainer.appendChild(challengeEl);
            });

            document.querySelectorAll('.join-challenge-btn').forEach(button => {
                button.addEventListener('click', (e) => {
                    const challengeId = parseInt(e.target.dataset.id, 10);
                    joinChallenge(challengeId);
                });
            });

            document.querySelectorAll('.delete-challenge-btn').forEach(button => {
                button.addEventListener('click', (e) => {
                    const challengeId = parseInt(e.target.dataset.id, 10);
                    if (confirm('정말 이 챌린지를 삭제하시겠습니까?')) {
                        deleteChallenge(challengeId);
                    }
                });
            });
        } catch (error) {
            console.error('챌린지 데이터를 불러오는 중 오류 발생:', error);
            ongoingChallengesContainer.innerHTML = '<p class="text-danger">챌린지 데이터를 불러오는 데 실패했습니다.</p>';
        }
    }

    function calculateAchievement(challenge, dietData) {
        const startDate = new Date(challenge.startDate);
        const today = new Date();
        let totalAchieved = 0;
        dietData.forEach(record => {
            const recordDate = new Date(record.date);
            if (recordDate >= startDate && recordDate <= today) {
                record.foods.forEach(food => {
                    switch (challenge.type) {
                        case '칼로리':
                            totalAchieved += food.energy;
                            break;
                        case '단백질':
                            totalAchieved += food.protein;
                            break;
                        case '탄수화물':
                            totalAchieved += food.carbohydrate;
                            break;
                        case '지방':
                            totalAchieved += food.fat;
                            break;
                        default:
                            break;
                    }
                });
            }
        });
        return totalAchieved;
    }

    function calculateDaysPassed(challenge) {
        const today = new Date();
        const startDate = new Date(challenge.startDate);
        const diff = Math.floor((today - startDate) / (1000 * 60 * 60 * 24)) + 1;
        return Math.min(diff, challenge.duration);
    }

    function joinChallenge(challengeId) {
        appUtils.apiFetch(`/api/challenges/${challengeId}/join`, {
            method: 'POST'
        }).then(() => {
            alert('챌린지 참여 성공!');
            loadAndDisplayChallenges();
        }).catch(error => {
            alert('챌린지 참여 중 오류 발생: ' + error.message);
        });
    }

    function deleteChallenge(id) {
        appUtils.apiFetch(`/api/challenges/${id}`, {
            method: 'DELETE'
        }).then(() => {
            alert('챌린지가 성공적으로 삭제되었습니다.');
            loadAndDisplayChallenges();
        }).catch(error => {
            alert('챌린지 삭제 실패: ' + error.message);
        });
    }

    function formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR');
    }

    function addDays(dateString, days) {
        const date = new Date(dateString);
        date.setDate(date.getDate() + days);
        return date.toISOString().split('T')[0];
    }

    loadAndDisplayChallenges();
}
