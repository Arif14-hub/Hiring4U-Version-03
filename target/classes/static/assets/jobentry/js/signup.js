(() => {
    const form = document.getElementById('signup-form');
    const feedback = document.getElementById('signup-feedback');
    const submitButton = document.getElementById('signup-button');
    const roleInput = document.getElementById('role');
    const roleOptions = document.querySelectorAll('.role-option');
    const roleSections = document.querySelectorAll('section[data-role]');
    const byId = (id) => document.getElementById(id);

    const showFeedback = (message, type) => {
        feedback.textContent = message;
        feedback.className = `auth-feedback show ${type}`;
    };

    const selectedRole = () => roleInput.value.toLowerCase();

    const setRole = (role) => {
        role = role === 'recruiter' ? 'recruiter' : 'candidate';
        roleInput.value = role.toUpperCase();

        roleOptions.forEach((option) => {
            const active = option.dataset.role === role;
            option.classList.toggle('active', active);
            option.setAttribute('aria-selected', active ? 'true' : 'false');
        });

        // Only hide/show the actual form sections.
        roleSections.forEach((section) => {
            section.hidden = section.dataset.role !== role;
        });

        byId('full-name').required = role === 'candidate';
        byId('company-name').required = role === 'recruiter';
        byId('candidate-resume').required = role === 'candidate';
        byId('candidate-resume').disabled = role !== 'candidate';
    };

    roleOptions.forEach((option) => {
        option.addEventListener('click', () => setRole(option.dataset.role));
        option.addEventListener('keydown', (event) => {
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                setRole(option.dataset.role);
            }
        });
    });

    setRole('candidate');

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        const password = byId('new-password').value;
        const role = selectedRole();

        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }

        if (password !== byId('confirm-password').value) {
            return showFeedback('Passwords do not match.', 'error');
        }

        const candidate = {
            fullName: byId('full-name').value.trim(),
            phoneNumber: byId('phone-number').value.trim(),
            dob: byId('dob')?.value || '',
            location: byId('candidate-location').value.trim()
        };

        const recruiter = {
            companyName: byId('company-name').value.trim(),
            hrName: byId('hr-name').value.trim(),
            hrPhone: byId('hr-phone').value.trim(),
            hrLocation: byId('hr-location').value.trim(),
            weblink: byId('weblink').value.trim()
        };

        const payload = {
            ...(role === 'candidate' ? candidate : recruiter),
            email: byId('email').value.trim(),
            password
        };

        if (role === 'candidate' && !payload.fullName) {
            return showFeedback('Please enter your full name.', 'error');
        }

        if (role === 'recruiter' && !payload.companyName) {
            return showFeedback('Please enter your company name.', 'error');
        }

        const resume = byId('candidate-resume').files[0];

        if (role === 'candidate' && !resume) {
            return showFeedback('Please upload your resume to create a candidate account.', 'error');
        }

        submitButton.disabled = true;
        submitButton.textContent = 'Creating account…';

        try {
            const request = role === 'candidate'
                ? (() => {
                    const formData = new FormData();
                    formData.append(
                        'candidate',
                        new Blob([JSON.stringify(payload)], { type: 'application/json' })
                    );
                    formData.append('resume', resume);

                    return {
                        url: '/can/registered',
                        options: {
                            method: 'POST',
                            headers: { 'Accept': 'text/plain' },
                            body: formData
                        }
                    };
                })()
                : {
                    url: '/rec/registered',
                    options: {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Accept': 'text/plain'
                        },
                        body: JSON.stringify(payload)
                    }
                };

            const response = await fetch(request.url, request.options);
            const message = await response.text();

            if (!response.ok) {
                throw new Error(message || 'We could not create your account.');
            }

            showFeedback(`${message}. Redirecting to login…`, 'success');

            window.setTimeout(() => {
                window.location.href = '/login.html?registered=success';
            }, 900);
        } catch (error) {
            showFeedback(error.message, 'error');
            submitButton.disabled = false;
            submitButton.textContent = 'Create account →';
        }
    });
})();
