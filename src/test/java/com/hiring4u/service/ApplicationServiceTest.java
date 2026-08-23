package com.hiring4u.service;

import com.hiring4u.dto.ApplicationDTO;
import com.hiring4u.entity.ApplicationEntity;
import com.hiring4u.repository.ApplicationRepository;
import com.hiring4u.repository.JobsRepository;
import com.hiring4u.repository.RegistrationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationServiceTest {

    private final ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    private final RegistrationRepository registrationRepository = mock(RegistrationRepository.class);
    private final JobsRepository jobsRepository = mock(JobsRepository.class);
    private final ApplicationService service = createService();

    @Test
    void rejectsMissingIds() {
        ApplicationDTO dto = new ApplicationDTO();

        var response = service.apply(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void rejectsUnknownCandidate() {
        ApplicationDTO dto = applicationDto(1L, 2L);

        when(registrationRepository.existsById(1L)).thenReturn(false);

        var response = service.apply(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Candidate not found");
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void rejectsUnknownJob() {
        ApplicationDTO dto = applicationDto(1L, 2L);

        when(registrationRepository.existsById(1L)).thenReturn(true);
        when(jobsRepository.existsById(2L)).thenReturn(false);

        var response = service.apply(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Job not found");
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateApplication() {
        ApplicationDTO dto = applicationDto(1L, 2L);

        when(registrationRepository.existsById(1L)).thenReturn(true);
        when(jobsRepository.existsById(2L)).thenReturn(true);
        when(applicationRepository.existsByCandidateIdAndJobId(1L, 2L)).thenReturn(true);

        var response = service.apply(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("Candidate already applied for this job");
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void savesValidApplication() {
        ApplicationDTO dto = applicationDto(1L, 2L);

        when(registrationRepository.existsById(1L)).thenReturn(true);
        when(jobsRepository.existsById(2L)).thenReturn(true);
        when(applicationRepository.existsByCandidateIdAndJobId(1L, 2L)).thenReturn(false);

        var response = service.apply(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Job Applied Successfully");
        verify(applicationRepository).save(any(ApplicationEntity.class));
    }

    private ApplicationService createService() {
        ApplicationService applicationService = new ApplicationService();
        ReflectionTestUtils.setField(applicationService, "Repository", applicationRepository);
        ReflectionTestUtils.setField(applicationService, "registrationRepository", registrationRepository);
        ReflectionTestUtils.setField(applicationService, "jobsRepository", jobsRepository);
        return applicationService;
    }

    private ApplicationDTO applicationDto(Long candidateId, Long jobId) {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setCandidateId(candidateId);
        dto.setJobId(jobId);
        return dto;
    }
}
