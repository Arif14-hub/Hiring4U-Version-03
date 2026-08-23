package com.hiring4u.service;

import com.hiring4u.dto.CanRegDTO;
import com.hiring4u.entity.CanEntity;
import com.hiring4u.entity.RecEntity;
import com.hiring4u.repository.RegistrationRepository;
import com.hiring4u.repository.RecruitorsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CanRegSerTest {

    private final RegistrationRepository candidateRepository = mock(RegistrationRepository.class);
    private final RecruitorsRepository recruiterRepository = mock(RecruitorsRepository.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final CanRegSer service = new CanRegSer(candidateRepository, recruiterRepository, passwordEncoder);

    @Test
    void savesNormalizedCandidateWithEncryptedPassword() {
        CanRegDTO dto = new CanRegDTO();
        dto.setFullName("  Asha Sharma  ");
        dto.setEmail("  ASHA@EXAMPLE.COM ");
        dto.setPassword("secure-password");

        when(candidateRepository.findByEmail("asha@example.com")).thenReturn(Optional.empty());
        when(recruiterRepository.findByEmail("asha@example.com")).thenReturn(Optional.empty());

        var response = service.processRegistration(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(candidateRepository).save(any(CanEntity.class));
    }

    @Test
    void rejectsEmailRegisteredByRecruiter() {
        CanRegDTO dto = new CanRegDTO();
        dto.setFullName("Asha Sharma");
        dto.setEmail("asha@example.com");
        dto.setPassword("secure-password");

        when(candidateRepository.findByEmail("asha@example.com")).thenReturn(Optional.empty());
        when(recruiterRepository.findByEmail("asha@example.com")).thenReturn(Optional.of(new RecEntity()));

        var response = service.processRegistration(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void requiresAndStoresResumeForCandidateSignup() {
        CanRegDTO dto = new CanRegDTO();
        dto.setFullName("Asha Sharma");
        dto.setEmail("asha@example.com");
        dto.setPassword("secure-password");
        MockMultipartFile resume = new MockMultipartFile(
                "resume", "asha-resume.pdf", "application/pdf", "resume content".getBytes()
        );

        when(candidateRepository.findByEmail("asha@example.com")).thenReturn(Optional.empty());
        when(recruiterRepository.findByEmail("asha@example.com")).thenReturn(Optional.empty());

        var response = service.processRegistration(dto, resume);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(candidateRepository).save(argThat(candidate ->
                "asha-resume.pdf".equals(candidate.getResumeFileName())
                        && "application/pdf".equals(candidate.getResumeContentType())
                        && "resume content".equals(new String(candidate.getResumeData()))
        ));
    }

    @Test
    void rejectsCandidateSignupWithoutResume() {
        CanRegDTO dto = new CanRegDTO();
        dto.setFullName("Asha Sharma");
        dto.setEmail("asha@example.com");
        dto.setPassword("secure-password");

        var response = service.processRegistration(dto, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("resume");
    }
}
