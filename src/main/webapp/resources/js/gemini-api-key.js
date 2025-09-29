(function () {
    document.addEventListener('DOMContentLoaded', () => {
        const form = document.getElementById('gemini-api-key-form');
        if (!form || !window.appUtils) {
            return;
        }

        const input = document.getElementById('gemini-api-key-input');
        const status = document.getElementById('gemini-api-key-status');
        const clearBtn = document.getElementById('gemini-api-key-clear');

        function updateStatusMessage(message, variant = 'muted') {
            if (!status) {
                return;
            }
            status.className = `form-text text-${variant}`;
            status.textContent = message;
        }

        function applyCurrentKey() {
            const newKey = (input?.value || '').trim();
            window.appUtils.setGeminiApiKey(newKey);
            if (newKey) {
                updateStatusMessage('Gemini API 키가 적용되었습니다. (페이지를 새로고침하면 초기화됩니다.)', 'success');
            } else {
                updateStatusMessage('Gemini API 키를 입력하면 AI 기능을 사용할 수 있습니다.');
            }
        }

        form.addEventListener('submit', (event) => {
            event.preventDefault();
            if (!input) {
                return;
            }
            if (!input.value.trim()) {
                updateStatusMessage('API 키를 입력해 주세요.', 'danger');
                window.appUtils.clearGeminiApiKey();
                return;
            }
            applyCurrentKey();
        });

        if (input) {
            input.addEventListener('input', () => {
                applyCurrentKey();
            });
        }

        if (clearBtn) {
            clearBtn.addEventListener('click', () => {
                window.appUtils.clearGeminiApiKey();
                if (input) {
                    input.value = '';
                }
                updateStatusMessage('Gemini API 키가 초기화되었습니다.', 'warning');
            });
        }

        applyCurrentKey();
    });
})();
