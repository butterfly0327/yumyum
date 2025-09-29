document.addEventListener('DOMContentLoaded', () => {
    updateAuthButtons();

    (async () => {
        try {
            const status = await appUtils.requireLogin();
            initializeMypageEdit(status.username);
        } catch (error) {
            console.error(error);
        }
    })();
});

function initializeMypageEdit(username) {
    const messageEl = document.getElementById('message');

    async function loadUserData() {
        try {
            document.getElementById('username').value = username;
            const profile = await appUtils.apiFetch('/api/profile', { method: 'GET', headers: { 'Content-Type': 'application/json' } });
            document.getElementById('name').value = profile.name || '';
            document.getElementById('height').value = profile.height || '';
            document.getElementById('weight').value = profile.weight || '';
            document.getElementById('disease').value = profile.disease || '';
        } catch (error) {
            console.error('프로필 정보를 불러오는 데 실패했습니다:', error);
        }
    }

    document.getElementById('account-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const currentPassword = document.getElementById('currentPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        try {
            await appUtils.apiFetch('/api/account', {
                method: 'PUT',
                body: { currentPassword, newPassword }
            });
            messageEl.className = 'mt-3 text-center text-success';
            messageEl.textContent = '회원 정보가 성공적으로 수정되었습니다.';
            document.getElementById('currentPassword').value = '';
            document.getElementById('newPassword').value = '';
        } catch (error) {
            messageEl.className = 'mt-3 text-center text-danger';
            messageEl.textContent = error.message;
        }
    });

    document.getElementById('delete-account-btn').addEventListener('click', async () => {
        if (!confirm('정말로 회원 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
            return;
        }
        const currentPassword = document.getElementById('currentPassword').value;
        if (!currentPassword) {
            alert('회원 탈퇴를 위해 현재 비밀번호를 입력해주세요.');
            return;
        }
        try {
            await appUtils.apiFetch('/api/account', {
                method: 'DELETE',
                body: { currentPassword }
            });
            alert('회원 탈퇴가 완료되었습니다.');
            window.location.href = `${appUtils.appRoot}/main?action=landing`;
        } catch (error) {
            messageEl.className = 'mt-3 text-center text-danger';
            messageEl.textContent = error.message;
        }
    });

    document.getElementById('profile-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const profile = {
            name: document.getElementById('name').value,
            height: parseFloat(document.getElementById('height').value) || 0,
            weight: parseFloat(document.getElementById('weight').value) || 0,
            disease: document.getElementById('disease').value
        };
        try {
            await appUtils.apiFetch('/api/profile', {
                method: 'PUT',
                body: profile
            });
            messageEl.className = 'mt-3 text-center text-success';
            messageEl.textContent = '프로필 정보가 성공적으로 저장되었습니다.';
        } catch (error) {
            messageEl.className = 'mt-3 text-center text-danger';
            messageEl.textContent = error.message;
        }
    });

    loadUserData();
}
