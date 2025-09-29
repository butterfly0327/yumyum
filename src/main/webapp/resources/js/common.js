const appConfig = window.appConfig || {};
const appRoot = appConfig.contextPath || '';
let cachedAuthStatus = null;

async function apiFetch(path, options = {}) {
    const url = path.startsWith('http') ? path : `${appRoot}${path}`;
    const fetchOptions = Object.assign({
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json' }
    }, options);

    if (fetchOptions.body && typeof fetchOptions.body !== 'string') {
        fetchOptions.body = JSON.stringify(fetchOptions.body);
    }

    const response = await fetch(url, fetchOptions);
    if (!response.ok) {
        let message = response.statusText;
        try {
            const data = await response.json();
            if (data && data.message) {
                message = data.message;
            }
        } catch (e) {
            // ignore
        }
        throw new Error(message || '요청 중 오류가 발생했습니다.');
    }

    const text = await response.text();
    if (!text) {
        return null;
    }
    try {
        return JSON.parse(text);
    } catch (e) {
        return text;
    }
}

async function getAuthStatus(force = false) {
    if (!force && cachedAuthStatus) {
        return cachedAuthStatus;
    }
    const status = await apiFetch('/api/auth/status', { method: 'GET', headers: { 'Content-Type': 'application/json' } });
    cachedAuthStatus = status;
    return status;
}

async function requireLogin() {
    const status = await getAuthStatus(true);
    if (!status || !status.authenticated) {
        alert('로그인이 필요한 페이지입니다.');
        window.location.href = `${appRoot}/main?action=login`;
        throw new Error('NOT_AUTHENTICATED');
    }
    return status;
}

async function updateAuthButtons() {
    const container = document.getElementById('auth-buttons');
    if (!container) {
        return;
    }
    try {
        const status = await getAuthStatus(true);
        if (status.authenticated) {
            container.innerHTML = `
                <a href="${appRoot}/main?action=mypage" class="btn btn-outline-light me-2">마이페이지</a>
                <button type="button" class="btn btn-outline-light" id="logout-btn">로그아웃</button>
            `;
            const logoutBtn = document.getElementById('logout-btn');
            if (logoutBtn) {
                logoutBtn.addEventListener('click', async () => {
                    try {
                        await apiFetch('/api/auth/logout', { method: 'POST' });
                        cachedAuthStatus = null;
                        window.location.href = `${appRoot}/main?action=landing`;
                    } catch (error) {
                        alert(error.message);
                    }
                });
            }
        } else {
            container.innerHTML = `
                <a href="${appRoot}/main?action=login" class="btn btn-outline-light me-2">로그인</a>
                <a href="${appRoot}/main?action=register" class="btn btn-primary">회원가입</a>
            `;
        }
    } catch (error) {
        console.error(error);
    }
}

window.appUtils = {
    apiFetch,
    getAuthStatus,
    requireLogin,
    updateAuthButtons,
    appRoot
};
