(() => {
    const profileMenu = document.getElementById('profile-menu');
    const loginLink = document.getElementById('login-link');
    const signupLink = document.getElementById('signup-link');
    const logoutButton = document.getElementById('logout-button');

    if (!profileMenu || !loginLink || !signupLink) return;

    logoutButton?.addEventListener('click', async () => {
        logoutButton.disabled = true;
        try {
            await fetch('/logout', { method: 'POST', credentials: 'same-origin', cache: 'no-store' });
        } finally {
            window.location.replace('/?logout=success');
        }
    });

    fetch('/api/auth/me', { headers: { Accept: 'application/json' }, cache: 'no-store' })
        .then((response) => response.ok ? response.json() : null)
        .then((profile) => {
            if (!profile) return;
            const name = profile.displayName || profile.email;
            const initials = name.split(/\s+/).map((word) => word[0]).join('').slice(0, 2).toUpperCase();

            document.getElementById('profile-name').textContent = name;
            document.getElementById('profile-email').textContent = profile.email;
            document.getElementById('profile-role').textContent = profile.role === 'RECRUITER' ? 'Recruiter account' : 'Candidate account';
            document.getElementById('profile-initials').textContent = initials;
            loginLink.classList.add('d-none');
            signupLink.classList.add('d-none');
            profileMenu.classList.remove('d-none');
        })
        .catch(() => undefined);
})();
