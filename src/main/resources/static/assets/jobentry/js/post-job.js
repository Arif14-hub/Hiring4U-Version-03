(() => {
    const form = document.getElementById('post-job-form');
    const feedback = document.getElementById('post-job-feedback');
    const submitButton = document.getElementById('post-job-button');
    const postedDate = document.getElementById('posted-date');
    const input = (id) => document.getElementById(id);
    let jobCoordinates = null;

    const showFeedback = (message, type) => {
        feedback.textContent = message;
        feedback.className = `post-job-feedback show ${type}`;
    };

    const setToday = () => {
        postedDate.value = new Date().toISOString().slice(0, 10);
    };

    setToday();

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                jobCoordinates = {
                    latitude: position.coords.latitude,
                    longitude: position.coords.longitude
                };
            },
            () => undefined,
            { maximumAge: 300000, timeout: 8000 }
        );
    }

    fetch('/api/auth/me', { headers: { Accept: 'application/json' } })
        .then((response) => response.ok ? response.json() : null)
        .then((profile) => {
            if (!profile || profile.role !== 'RECRUITER') {
                window.location.replace('/?error=recruiter-required');
                return;
            }
            const displayName = profile.displayName || profile.email;
            document.getElementById('recruiter-name').textContent = `Posting as ${displayName}`;
            document.getElementById('recruiter-avatar').textContent = displayName.split(/\s+/).map((word) => word[0]).join('').slice(0, 2).toUpperCase();
        })
        .catch(() => window.location.replace('/login'));

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }

        const payload = {
            title: input('title').value.trim(),
            description: input('description').value.trim(),
            requireskills: input('requireskills').value.trim(),
            salary: Number(input('salary').value),
            location: input('location').value.trim(),
            postedDate: postedDate.value,
            ...(jobCoordinates || {})
        };

        submitButton.disabled = true;
        submitButton.innerHTML = 'Publishing… <i class="fa fa-spinner fa-spin ms-2"></i>';

        try {
            const response = await fetch('/recruiter/posted', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', Accept: 'text/plain' },
                body: JSON.stringify(payload)
            });
            const message = await response.text();
            if (!response.ok) throw new Error(message || 'We could not publish this job post.');
            form.reset();
            setToday();
            showFeedback(`${message}. Your listing is now available to candidates.`, 'success');
        } catch (error) {
            showFeedback(error.message, 'error');
        } finally {
            submitButton.disabled = false;
            submitButton.innerHTML = 'Publish job post <i class="fa fa-arrow-right ms-2"></i>';
        }
    });
})();
