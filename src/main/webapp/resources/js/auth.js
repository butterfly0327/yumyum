document.addEventListener('DOMContentLoaded', () => {
    updateAuthButtons();

    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;
            const email = document.getElementById('email').value.trim();
            const messageEl = document.getElementById('register-message');
            try {
                await appUtils.apiFetch('/api/auth/register', {
                    method: 'POST',
                    body: { username, password, email }
                });
                messageEl.className = 'mt-3 text-center text-success';
                messageEl.textContent = '회원가입 성공! 로그인 페이지로 이동합니다.';
                setTimeout(() => {
                    window.location.href = `${appUtils.appRoot}/main?action=login`;
                }, 1500);
            } catch (error) {
                messageEl.className = 'mt-3 text-center text-danger';
                messageEl.textContent = error.message;
            }
        });
    }

    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;
            const messageEl = document.getElementById('login-message');
            try {
                await appUtils.apiFetch('/api/auth/login', {
                    method: 'POST',
                    body: { username, password }
                });
                messageEl.className = 'mt-3 text-center text-success';
                messageEl.textContent = '로그인 성공! 홈으로 이동합니다.';
                setTimeout(() => {
                    window.location.href = `${appUtils.appRoot}/main?action=landing`;
                }, 1000);
            } catch (error) {
                messageEl.className = 'mt-3 text-center text-danger';
                messageEl.textContent = error.message;
            }
        });
    }
});
