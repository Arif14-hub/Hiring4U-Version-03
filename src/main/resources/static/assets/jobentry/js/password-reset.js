(() => {
    const feedback = document.getElementById('reset-feedback');
    const showFeedback = (message, type, resetUrl) => {
        feedback.className = `auth-feedback show ${type}`;
        feedback.replaceChildren(document.createTextNode(message));
        if (resetUrl) {
            const link = document.createElement('a');
            link.href = resetUrl;
            link.textContent = ' Open the reset page.';
            link.className = 'auth-small-link';
            feedback.append(link);
        }
    };

    const requestForm = document.getElementById('reset-request-form');
    if (requestForm) {
        requestForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            if (!requestForm.checkValidity()) {
                requestForm.reportValidity();
                return;
            }
            const button = document.getElementById('reset-request-button');
            button.disabled = true;
            button.textContent = 'Creating link…';
            try {
                const response = await fetch('/api/auth/password/reset-request', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
                    body: JSON.stringify({ email: document.getElementById('email').value.trim() })
                });
                const data = await response.json();
                if (!response.ok) throw new Error(data.message || 'We could not start your password reset.');
                showFeedback(data.message, 'success', data.resetUrl);
            } catch (error) {
                showFeedback(error.message, 'error');
            } finally {
                button.disabled = false;
                button.innerHTML = 'Get reset link <i class="fa fa-arrow-right ms-2"></i>';
            }
        });
        return;
    }

    const resetForm = document.getElementById('reset-password-form');
    if (!resetForm) return;
    const token = new URLSearchParams(window.location.search).get('token');
    if (!token) showFeedback('This reset link is incomplete. Request a new one to continue.', 'error');
    resetForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (!token) return;
        if (!resetForm.checkValidity()) {
            resetForm.reportValidity();
            return;
        }
        const password = document.getElementById('new-password').value;
        if (password !== document.getElementById('confirm-password').value) {
            showFeedback('Passwords do not match.', 'error');
            return;
        }
        const button = document.getElementById('reset-password-button');
        button.disabled = true;
        button.textContent = 'Updating password…';
        try {
            const response = await fetch('/api/auth/password/reset', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', Accept: 'text/plain' },
                body: JSON.stringify({ token, password })
            });
            const message = await response.text();
            if (!response.ok) throw new Error(message || 'We could not reset your password.');
            showFeedback(message, 'success');
            window.setTimeout(() => { window.location.href = '/login?password=reset'; }, 1200);
        } catch (error) {
            showFeedback(error.message, 'error');
        } finally {
            button.disabled = false;
            button.innerHTML = 'Update password <i class="fa fa-arrow-right ms-2"></i>';
        }
    });
})();
