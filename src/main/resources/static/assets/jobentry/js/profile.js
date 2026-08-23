(() => {
    const byId = (id) => document.getElementById(id);
    const form = byId('profile-form');
    const feedback = byId('profile-feedback');
    const saveButton = byId('profile-save-button');
    const uploadButton = byId('upload-resume-button');
    const resumeFile = byId('resume-file');
    let profile;

    const value = (field) => field ?? '';
    const showFeedback = (message, type) => { feedback.textContent = message; feedback.className = `profile-feedback show ${type}`; };
    const responseMessage = async (response, fallback) => {
        const text = await response.text();
        if (!text) return fallback;
        try { return JSON.parse(text).detail || JSON.parse(text).message || fallback; } catch { return text; }
    };
    const setField = (id, field) => { byId(id).value = value(field); };

    const populateProfile = (data) => {
        profile = data;
        const isCandidate = profile.role === 'CANDIDATE';
        document.querySelectorAll('[data-profile-role]').forEach((section) => {
            const isActiveSection = section.dataset.profileRole === profile.role;
            section.hidden = !isActiveSection;
            section.querySelectorAll('input, select, textarea, button').forEach((field) => { field.disabled = !isActiveSection; });
        });
        byId('profile-eyebrow').textContent = isCandidate ? 'Candidate account' : 'Recruiter account';
        byId('profile-heading').textContent = isCandidate ? 'Build a profile employers notice.' : 'Keep your company profile hiring-ready.';
        byId('profile-subheading').textContent = isCandidate ? 'Your profile and resume are saved securely in Hiring4U.' : 'Your company information is available when you create job posts.';
        byId('profile-display-name').textContent = profile.displayName;
        byId('profile-email').textContent = profile.email;
        byId('profile-role-badge').textContent = isCandidate ? 'Candidate' : 'Recruiter';
        byId('profile-avatar').textContent = profile.displayName.split(/\s+/).map((word) => word[0]).join('').slice(0, 2).toUpperCase();

        if (isCandidate) {
            setField('full-name', profile.fullName); setField('professional-title', profile.professionalTitle); setField('phone-number', profile.phoneNumber); setField('candidate-location', profile.location); setField('candidate-dob', profile.dob); setField('experience-years', profile.experienceYears); setField('skills', profile.skills); setField('education', profile.education); setField('bio', profile.bio); setField('candidate-linkedin', profile.linkedInUrl); setField('portfolio-url', profile.portfolioUrl);
            byId('resume-status').textContent = profile.resumeAvailable ? `${profile.resumeFileName} is saved in your profile.` : 'PDF, DOC, or DOCX — up to 5 MB.';
            byId('download-resume-link').classList.toggle('d-none', !profile.resumeAvailable);
        } else {
            setField('company-name', profile.companyName); setField('industry', profile.industry); setField('hr-name', profile.hrName); setField('hr-phone', profile.hrPhone); setField('hr-location', profile.hrLocation); setField('company-address', profile.companyAddress); setField('company-size', profile.companySize); setField('company-website', profile.weblink); setField('company-linkedin', profile.linkedInUrl); setField('company-description', profile.companyDescription);
        }
    };

    const profilePayload = () => profile.role === 'CANDIDATE' ? {
        fullName: byId('full-name').value.trim(), phoneNumber: byId('phone-number').value.trim(), dob: byId('candidate-dob').value, location: byId('candidate-location').value.trim(), professionalTitle: byId('professional-title').value.trim(), skills: byId('skills').value.trim(), experienceYears: byId('experience-years').value === '' ? null : Number(byId('experience-years').value), education: byId('education').value.trim(), bio: byId('bio').value.trim(), linkedInUrl: byId('candidate-linkedin').value.trim(), portfolioUrl: byId('portfolio-url').value.trim()
    } : {
        companyName: byId('company-name').value.trim(), hrName: byId('hr-name').value.trim(), hrPhone: byId('hr-phone').value.trim(), hrLocation: byId('hr-location').value.trim(), weblink: byId('company-website').value.trim(), industry: byId('industry').value.trim(), companySize: byId('company-size').value, companyAddress: byId('company-address').value.trim(), companyDescription: byId('company-description').value.trim(), linkedInUrl: byId('company-linkedin').value.trim()
    };

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (!form.checkValidity()) { form.reportValidity(); return; }
        saveButton.disabled = true; saveButton.textContent = 'Saving…';
        try {
            const response = await fetch('/api/profile', { method: 'PUT', headers: { 'Content-Type': 'application/json', Accept: 'application/json' }, body: JSON.stringify(profilePayload()), cache: 'no-store' });
            if (!response.ok) throw new Error(await responseMessage(response, 'We could not save your profile.'));
            populateProfile(await response.json());
            showFeedback('Your profile changes were saved to the database.', 'success');
        } catch (error) { showFeedback(error.message, 'error'); }
        finally { saveButton.disabled = false; saveButton.innerHTML = 'Save profile changes <i class="fa fa-check ms-2"></i>'; }
    });

    uploadButton.addEventListener('click', async () => {
        const file = resumeFile.files[0];
        if (!file) { showFeedback('Choose a resume file before uploading.', 'error'); return; }
        uploadButton.disabled = true; uploadButton.textContent = 'Uploading…';
        try {
            const data = new FormData(); data.append('file', file);
            const response = await fetch('/api/profile/resume', { method: 'POST', body: data, cache: 'no-store' });
            if (!response.ok) throw new Error(await responseMessage(response, 'We could not upload your resume.'));
            populateProfile(await response.json()); resumeFile.value = '';
            showFeedback('Your resume was uploaded and saved to the database.', 'success');
        } catch (error) { showFeedback(error.message, 'error'); }
        finally { uploadButton.disabled = false; uploadButton.textContent = 'Upload resume'; }
    });

    fetch('/api/profile', { headers: { Accept: 'application/json' }, cache: 'no-store' })
        .then(async (response) => { if (!response.ok) throw new Error(await responseMessage(response, 'Please sign in to view your profile.')); return response.json(); })
        .then(populateProfile)
        .catch((error) => { showFeedback(error.message, 'error'); window.setTimeout(() => { window.location.href = '/login'; }, 900); });
})();
