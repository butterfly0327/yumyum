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

        function refreshForm() {
            const currentKey = window.appUtils.getGeminiApiKey();
            if (input) {
                input.value = currentKey;
            }
            if (currentKey) {
                updateStatusMessage('Gemini API 키가 저장되었습니다. (브라우저 로컬에만 저장됩니다.)', 'success');
            } else {
                updateStatusMessage('Gemini API 키가 설정되지 않았습니다. 키를 입력하면 AI 기능을 사용할 수 있습니다.');
            }
            document.dispatchEvent(new CustomEvent('geminiApiKeyChanged', {
                detail: { hasKey: Boolean(currentKey) }
            }));
        }

        form.addEventListener('submit', (event) => {
            event.preventDefault();
            const newKey = (input?.value || '').trim();
            if (!newKey) {
                updateStatusMessage('API 키를 입력해 주세요.', 'danger');
                return;
            }
            window.appUtils.setGeminiApiKey(newKey);
            if (input) {
                input.value = newKey;
            }
            updateStatusMessage('Gemini API 키가 저장되었습니다. (브라우저 로컬에만 저장됩니다.)', 'success');
        });

        if (clearBtn) {
            clearBtn.addEventListener('click', () => {
                window.appUtils.clearGeminiApiKey();
                if (input) {
                    input.value = '';
                }
                updateStatusMessage('Gemini API 키가 삭제되었습니다.', 'warning');
            });
        }

        refreshForm();
    });
})();
