package com.hiring4u.controller;

import com.hiring4u.dto.UserProfileDTO;
import com.hiring4u.repository.RecruitorsRepository;
import com.hiring4u.repository.RegistrationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationRepository candidateRepository;
    private final RecruitorsRepository recruiterRepository;

    public AuthController(RegistrationRepository candidateRepository, RecruitorsRepository recruiterRepository) {
        this.candidateRepository = candidateRepository;
        this.recruiterRepository = recruiterRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> currentUser(Authentication authentication) {
        String email = authentication.getName();

        return candidateRepository.findByEmail(email)
                .map(candidate -> ResponseEntity.ok(new UserProfileDTO(
                        candidate.getFullName(), candidate.getEmail(), candidate.getRole().name())))
                .or(() -> recruiterRepository.findByEmail(email)
                        .map(recruiter -> ResponseEntity.ok(new UserProfileDTO(
                                recruiter.getCompanyName(), recruiter.getEmail(), recruiter.getRole().name()))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
