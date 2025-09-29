const appConfig = window.appConfig || {};
const appRoot = appConfig.contextPath || '';
let cachedAuthStatus = null;

const GEMINI_STORAGE_KEY = 'yumyumcoach.geminiApiKey';
let geminiApiKey = '';

function normalizeGeminiApiKey(key) {
    return (key || '').trim();
}

function getGeminiStorage() {
    try {
        const storage = window.sessionStorage;
        const testKey = '__yumyumcoach_storage_test__';
        storage.setItem(testKey, 'ok');
        storage.removeItem(testKey);
        return storage;
    } catch (error) {
        console.warn('세션 스토리지를 사용할 수 없어 Gemini API 키를 메모리에만 저장합니다.', error);
        return null;
    }
}

const geminiStorage = getGeminiStorage();

function loadStoredGeminiApiKey() {
    if (!geminiStorage) {
        return '';
    }
    try {
        return normalizeGeminiApiKey(geminiStorage.getItem(GEMINI_STORAGE_KEY));
    } catch (error) {
        console.warn('Gemini API 키를 스토리지에서 불러오는 중 문제가 발생했습니다.', error);
        return '';
    }
}

function persistGeminiApiKey(value) {
    if (!geminiStorage) {
        return;
    }
    try {
        if (value) {
            geminiStorage.setItem(GEMINI_STORAGE_KEY, value);
        } else {
            geminiStorage.removeItem(GEMINI_STORAGE_KEY);
        }
    } catch (error) {
        console.warn('Gemini API 키를 스토리지에 저장하는 중 문제가 발생했습니다.', error);
    }
}

geminiApiKey = loadStoredGeminiApiKey();

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

function getGeminiApiKey() {
    if (!geminiApiKey) {
        const stored = loadStoredGeminiApiKey();
        if (stored) {
            geminiApiKey = stored;
        }
    }
    return geminiApiKey;
}

function setGeminiApiKey(key) {
    const normalizedKey = normalizeGeminiApiKey(key);
    const previousKey = geminiApiKey;
    geminiApiKey = normalizedKey;

    if (normalizedKey === previousKey) {
        return;
    }

    persistGeminiApiKey(geminiApiKey);

    document.dispatchEvent(new CustomEvent('geminiApiKeyChanged', {
        detail: { hasKey: Boolean(geminiApiKey) }
    }));
}

function clearGeminiApiKey() {
    setGeminiApiKey('');
}

window.appUtils = {
    apiFetch,
    getAuthStatus,
    requireLogin,
    updateAuthButtons,
    appRoot,
    getGeminiApiKey,
    setGeminiApiKey,
    clearGeminiApiKey
};
