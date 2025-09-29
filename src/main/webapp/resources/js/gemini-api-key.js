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

        let appliedKey = window.appUtils.getGeminiApiKey();
        if (input && appliedKey) {
            input.value = appliedKey;
        }

        function syncInputWithAppliedKey(options = {}) {
            const preserveWarning = Boolean(options.preserveWarning);
            if (!input) {
                return;
            }

            if (preserveWarning && status?.classList.contains('text-warning')) {
                return;
            }

            const currentValue = (input.value || '').trim();

            if (appliedKey && currentValue === appliedKey) {
                updateStatusMessage('Gemini API 키가 적용되었습니다. (페이지를 새로고침하면 초기화됩니다.)', 'success');
                return;
            }

            if (appliedKey && !currentValue) {
                updateStatusMessage('Gemini API 키가 적용되었습니다. (페이지를 새로고침하면 초기화됩니다.)', 'success');
                return;
            }

            if (appliedKey && currentValue !== appliedKey) {
                updateStatusMessage('변경된 내용을 적용하려면 "적용" 버튼을 눌러 주세요.', 'info');
                return;
            }

            if (!appliedKey && currentValue) {
                updateStatusMessage('입력한 키를 사용하려면 "적용" 버튼을 눌러 주세요.', 'info');
                return;
            }

            updateStatusMessage('Gemini API 키를 입력하면 AI 기능을 사용할 수 있습니다.');
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
            const newKey = input.value.trim();
            window.appUtils.setGeminiApiKey(newKey);
            appliedKey = newKey;
            updateStatusMessage('Gemini API 키가 적용되었습니다. (페이지를 새로고침하면 초기화됩니다.)', 'success');
        });

        if (input) {
            input.addEventListener('input', () => {
                const currentValue = input.value.trim();
                if (!currentValue) {
                    appliedKey = '';
                    window.appUtils.clearGeminiApiKey();
                }
                syncInputWithAppliedKey();
            });
        }

        if (clearBtn) {
            clearBtn.addEventListener('click', () => {
                window.appUtils.clearGeminiApiKey();
                if (input) {
                    input.value = '';
                }
                updateStatusMessage('Gemini API 키가 초기화되었습니다.', 'warning');
                appliedKey = '';
                setTimeout(() => syncInputWithAppliedKey(), 2500);
            });
        }

        document.addEventListener('geminiApiKeyChanged', (event) => {
            const hasKey = Boolean(event?.detail?.hasKey);
            appliedKey = hasKey ? window.appUtils.getGeminiApiKey() : '';
            syncInputWithAppliedKey({ preserveWarning: !hasKey });
        });

        syncInputWithAppliedKey();
    });
})();
