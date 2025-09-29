document.addEventListener('DOMContentLoaded', () => {
    updateAuthButtons();

    (async () => {
        try {
            const status = await appUtils.requireLogin();
            initializeMypage(status.username);
        } catch (error) {
            console.error(error);
        }
    })();
});

function initializeMypage(username) {
    async function loadUserInfo() {
        try {
            const profile = await appUtils.apiFetch('/api/profile', { method: 'GET', headers: { 'Content-Type': 'application/json' } });
            document.getElementById('view-name').textContent = profile.name || '미입력';
            document.getElementById('view-height').textContent = profile.height ? `${profile.height}` : '미입력';
            document.getElementById('view-weight').textContent = profile.weight ? `${profile.weight}` : '미입력';
            document.getElementById('view-disease').textContent = profile.disease || '미입력';
        } catch (error) {
            console.error('프로필 정보를 불러오는 데 실패했습니다:', error);
        }
    }

    async function loadFollowingList() {
        try {
            const followData = await appUtils.apiFetch('/api/follows', { method: 'GET', headers: { 'Content-Type': 'application/json' } });
            const following = followData[username]?.following || [];
            const followingListEl = document.getElementById('following-list');
            followingListEl.innerHTML = '';
            if (!following.length) {
                followingListEl.innerHTML = '<p class="text-muted text-center">아직 팔로우하는 사용자가 없습니다.</p>';
                return;
            }
            following.forEach(followee => {
                const listItem = document.createElement('div');
                listItem.className = 'list-group-item d-flex justify-content-between align-items-center';
                listItem.innerHTML = `
                    <span>${followee}</span>
                    <button class="btn btn-sm btn-outline-danger unfollow-btn" data-followee="${followee}">언팔로우</button>
                `;
                followingListEl.appendChild(listItem);
            });
            document.querySelectorAll('.unfollow-btn').forEach(button => {
                button.addEventListener('click', (e) => {
                    const followee = e.target.dataset.followee;
                    toggleFollow(followee, false);
                });
            });
        } catch (error) {
            console.error('팔로잉 목록을 불러오는 데 실패했습니다:', error);
            document.getElementById('following-list').innerHTML = '<p class="text-danger text-center">팔로잉 목록을 불러오는 데 실패했습니다.</p>';
        }
    }

    function toggleFollow(followee, follow) {
        appUtils.apiFetch('/api/follows', {
            method: 'POST',
            body: { followee, action: follow ? 'follow' : 'unfollow' }
        }).then(() => {
            alert(follow ? `${followee}님을 팔로우했습니다.` : `${followee}님을 팔로잉 취소했습니다.`);
            loadFollowingList();
        }).catch(error => {
            alert('작업 실패: ' + error.message);
        });
    }

    loadUserInfo();
    loadFollowingList();
}
