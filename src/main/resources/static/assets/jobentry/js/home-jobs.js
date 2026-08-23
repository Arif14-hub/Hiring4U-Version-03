(() => {
    const categoryHeading = Array.from(document.querySelectorAll('h1')).find((heading) => heading.textContent.trim() === 'Explore By Category');
    categoryHeading?.closest('.container-fliud')?.remove();

    const jobHeading = Array.from(document.querySelectorAll('h1')).find((heading) => heading.textContent.trim() === 'Job Listing');
    const jobSection = jobHeading?.closest('.container-fliud');
    if (!jobSection) return;

    jobSection.id = 'jobs';
    jobHeading.textContent = 'Live opportunities';
    const jobContainer = jobSection.querySelector('.container');
    const tabContent = jobSection.querySelector('.tab-class');
    tabContent.replaceChildren();

    const status = document.createElement('p');
    status.className = 'live-jobs-status text-center mb-4';
    const jobsList = document.createElement('div');
    jobsList.id = 'live-jobs-list';
    const feedback = document.createElement('p');
    feedback.className = 'live-job-feedback';
    tabContent.append(status, jobsList, feedback);

    const searchInput = document.getElementById('job-search');
    const roleInput = document.getElementById('job-role');
    const locationInput = document.getElementById('job-location');
    const minimumCtcInput = document.getElementById('job-min-ctc');
    const postedWithinInput = document.getElementById('job-posted-within');
    const searchButton = document.getElementById('job-search-button');
    let jobs = [];
    let searchTimer;
    let latestRequest = 0;
    let selectedSort = 'recent';
    let viewerCoordinates = null;

    const element = (tag, className, text) => {
        const node = document.createElement(tag);
        if (className) node.className = className;
        if (text !== undefined) node.textContent = text;
        return node;
    };

    const sortOptions = [
        { value: 'recent', label: 'Most recently posted' },
        { value: 'ctc_desc', label: 'CTC: high to low' },
        { value: 'ctc_asc', label: 'CTC: low to high' },
        { value: 'distance', label: 'Nearest to me' },
        { value: 'skills', label: 'Best skill match' }
    ];
    const sortControls = element('div', 'live-jobs-sort dropdown d-none');
    const sortButton = element('button', 'btn btn-outline-primary dropdown-toggle', 'Sort results');
    sortButton.type = 'button';
    sortButton.setAttribute('data-bs-toggle', 'dropdown');
    sortButton.setAttribute('aria-expanded', 'false');
    const sortMenu = element('div', 'dropdown-menu dropdown-menu-end shadow-sm');
    sortOptions.forEach((option) => {
        const optionButton = element('button', 'dropdown-item', option.label);
        optionButton.type = 'button';
        optionButton.dataset.sort = option.value;
        optionButton.addEventListener('click', () => selectSort(option.value));
        sortMenu.append(optionButton);
    });
    sortControls.append(sortButton, sortMenu);
    tabContent.insertBefore(sortControls, jobsList);

    const requestedSkills = () => `${searchInput.value} ${roleInput.value}`
        .toLowerCase()
        .split(/[\s,;/|]+/)
        .map((term) => term.trim())
        .filter((term) => term.length > 1);

    const skillMatchScore = (job) => {
        const skills = requestedSkills();
        if (!skills.length) return null;
        const jobText = `${job.title} ${job.requireskills}`.toLowerCase();
        const matches = skills.filter((skill) => jobText.includes(skill));
        return Math.round((matches.length / skills.length) * 100);
    };

    const distanceInKilometres = (latitude, longitude, targetLatitude, targetLongitude) => {
        const earthRadiusKm = 6371;
        const toRadians = (value) => value * Math.PI / 180;
        const latitudeDifference = toRadians(targetLatitude - latitude);
        const longitudeDifference = toRadians(targetLongitude - longitude);
        const distanceFormula = Math.sin(latitudeDifference / 2) ** 2
            + Math.cos(toRadians(latitude)) * Math.cos(toRadians(targetLatitude)) * Math.sin(longitudeDifference / 2) ** 2;
        return earthRadiusKm * 2 * Math.atan2(Math.sqrt(distanceFormula), Math.sqrt(1 - distanceFormula));
    };

    const hasCoordinates = (job) => Number.isFinite(job.latitude) && Number.isFinite(job.longitude);

    const sortCurrentJobs = () => {
        const sortedJobs = [...jobs];
        if (selectedSort === 'ctc_desc') return sortedJobs.sort((firstJob, secondJob) => secondJob.salary - firstJob.salary);
        if (selectedSort === 'ctc_asc') return sortedJobs.sort((firstJob, secondJob) => firstJob.salary - secondJob.salary);
        if (selectedSort === 'distance') return sortedJobs.sort((firstJob, secondJob) => (firstJob.distanceKm ?? Infinity) - (secondJob.distanceKm ?? Infinity));
        if (selectedSort === 'skills') return sortedJobs.sort((firstJob, secondJob) => (skillMatchScore(secondJob) ?? 0) - (skillMatchScore(firstJob) ?? 0));
        return sortedJobs;
    };

    const setDistanceForJobs = () => {
        jobs.forEach((job) => {
            job.distanceKm = hasCoordinates(job) && viewerCoordinates
                ? distanceInKilometres(viewerCoordinates.latitude, viewerCoordinates.longitude, job.latitude, job.longitude)
                : null;
        });
    };

    const requestViewerCoordinates = () => new Promise((resolve, reject) => {
        if (!navigator.geolocation) {
            reject(new Error('Location services are not available in this browser.'));
            return;
        }
        navigator.geolocation.getCurrentPosition(
            (position) => resolve({ latitude: position.coords.latitude, longitude: position.coords.longitude }),
            () => reject(new Error('Allow location access to sort jobs by distance.')),
            { maximumAge: 300000, timeout: 10000 }
        );
    });

    const selectSort = async (sort) => {
        if (sort === 'skills' && !requestedSkills().length) {
            feedback.textContent = 'Search by a skill or role first to rank the matching jobs.';
            return;
        }
        if (sort === 'distance') {
            try {
                viewerCoordinates ??= await requestViewerCoordinates();
                setDistanceForJobs();
                if (!jobs.some((job) => job.distanceKm !== null)) {
                    feedback.textContent = 'Distance is not available for these jobs because their recruiters did not share a job location.';
                    return;
                }
            } catch (error) {
                feedback.textContent = error.message;
                return;
            }
        }
        selectedSort = sort;
        sortButton.textContent = `Sort: ${sortOptions.find((option) => option.value === sort).label}`;
        renderJobs();
    };

    const applyForJob = async (jobId, button) => {
        const profileResponse = await fetch('/api/auth/me', { headers: { Accept: 'application/json' } });
        if (!profileResponse.ok) {
            window.location.href = '/login';
            return;
        }
        const profile = await profileResponse.json();
        if (profile.role !== 'CANDIDATE') {
            feedback.textContent = 'Only candidate accounts can apply for jobs.';
            return;
        }
        button.disabled = true;
        button.textContent = 'Applying…';
        const response = await fetch('/candidate/apply', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', Accept: 'text/plain' },
            body: JSON.stringify({ jobId })
        });
        const message = await response.text();
        feedback.textContent = message;
        if (response.ok) button.textContent = 'Applied';
        else { button.disabled = false; button.textContent = 'Apply now'; }
    };

    const createJobCard = (job) => {
        const card = element('div', 'job-item live-job-card p-4 mb-4');
        const row = element('div', 'row g-4');
        const main = element('div', 'col-sm-12 col-md-8 d-flex align-items-center');
        const initials = (job.company || 'H').split(/\s+/).map((word) => word[0]).join('').slice(0, 2).toUpperCase();
        main.append(element('div', 'live-job-mark', initials));
        const details = element('div', 'text-start ps-4');
        details.append(element('h5', 'mb-2', job.title));
        details.append(element('p', 'mb-2 fw-bold', job.company));
        const meta = element('div', 'live-job-meta');
        meta.append(element('span', 'text-truncate me-3', `📍 ${job.location}`));
        meta.append(element('span', 'text-truncate', `₹ ${Number(job.salary).toLocaleString('en-IN')} yearly`));
        if (selectedSort === 'distance' && Number.isFinite(job.distanceKm)) {
            meta.append(element('span', 'text-truncate ms-3', `• ${job.distanceKm.toFixed(1)} km away`));
        }
        details.append(meta, element('p', 'live-job-skills', job.requireskills), element('p', 'live-job-description', job.description.length > 210 ? `${job.description.slice(0, 210)}…` : job.description));
        if (selectedSort === 'skills') {
            details.append(element('p', 'live-job-score', `Skill match: ${skillMatchScore(job)}%`));
        }
        main.append(details);
        const actions = element('div', 'col-sm-12 col-md-4 d-flex flex-column align-items-start align-items-md-end justify-content-center');
        const applyButton = element('button', 'btn btn-primary', 'Apply now');
        applyButton.type = 'button';
        applyButton.addEventListener('click', () => applyForJob(job.id, applyButton).catch(() => { feedback.textContent = 'We could not submit your application. Please try again.'; applyButton.disabled = false; applyButton.textContent = 'Apply now'; }));
        actions.append(applyButton, element('small', 'text-truncate mt-3', `Posted ${new Date(`${job.postedDate}T00:00:00`).toLocaleDateString()}`));
        row.append(main, actions);
        card.append(row);
        return card;
    };

    const renderJobs = () => {
        const sortedJobs = sortCurrentJobs();
        sortControls.classList.toggle('d-none', !sortedJobs.length);
        status.replaceChildren(element('strong', null, String(sortedJobs.length)), document.createTextNode(sortedJobs.length === 1 ? ' opportunity available from our database' : ' opportunities available from our database'));
        jobsList.replaceChildren();
        if (!sortedJobs.length) {
            const empty = element('div', 'live-jobs-empty');
            empty.append(element('i', 'fa fa-search'), element('h5', 'mb-2', 'No matching jobs yet'), element('p', 'mb-0', 'Try a different search, or check back when recruiters publish new roles.'));
            jobsList.append(empty);
            return;
        }
        sortedJobs.forEach((job) => jobsList.append(createJobCard(job)));
    };

    const loadJobs = async () => {
        const requestNumber = ++latestRequest;
        status.textContent = 'Searching live jobs…';
        try {
            const parameters = new URLSearchParams();
            if (searchInput.value.trim()) parameters.set('search', searchInput.value.trim());
            if (roleInput.value) parameters.set('role', roleInput.value);
            if (locationInput.value) parameters.set('location', locationInput.value);
            if (minimumCtcInput.value) parameters.set('minCtc', minimumCtcInput.value);
            if (postedWithinInput.value) parameters.set('postedWithinDays', postedWithinInput.value);
            const query = parameters.toString();
            const response = await fetch(`/candidate/jobs${query ? `?${query}` : ''}`, {
                headers: { Accept: 'application/json' },
                cache: 'no-store'
            });
            if (!response.ok) throw new Error();
            const result = await response.json();
            if (requestNumber !== latestRequest) return;
            jobs = result;
            if (selectedSort === 'distance' && viewerCoordinates) setDistanceForJobs();
            renderJobs();
        } catch {
            if (requestNumber !== latestRequest) return;
            status.textContent = 'Live jobs are temporarily unavailable.';
        }
    };

    searchButton.addEventListener('click', (event) => { event.preventDefault(); loadJobs(); window.location.hash = 'jobs'; });
    const scheduleSearch = () => {
        window.clearTimeout(searchTimer);
        searchTimer = window.setTimeout(loadJobs, 300);
    };

    [searchInput, roleInput, locationInput, minimumCtcInput].forEach((input) => input.addEventListener('input', scheduleSearch));
    postedWithinInput.addEventListener('change', loadJobs);
    searchInput.addEventListener('keydown', (event) => { if (event.key === 'Enter') { event.preventDefault(); window.clearTimeout(searchTimer); loadJobs(); } });
    loadJobs();
    window.setInterval(loadJobs, 10000);
})();
