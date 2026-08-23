package com.hiring4u.service;
import com.hiring4u.Enums.Role;
import com.hiring4u.dto.RecruiterDTO;
import com.hiring4u.entity.RecEntity;
import com.hiring4u.repository.RecruitorsRepository;
import com.hiring4u.repository.RegistrationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;


@Service
public class RecRegSer {
    private final RecruitorsRepository repository;
    private final RegistrationRepository candidateRepository;
    private final BCryptPasswordEncoder encoder;

    public RecRegSer(RecruitorsRepository repository,
                     RegistrationRepository candidateRepository,
                     BCryptPasswordEncoder encoder) {
        this.repository = repository;
        this.candidateRepository = candidateRepository;
        this.encoder = encoder;
    }

    public ResponseEntity<String> processRegistration(RecruiterDTO dto) {
        if (isBlank(dto.getCompanyName()) || isBlank(dto.getEmail()) || isBlank(dto.getPassword())) {
            return ResponseEntity.badRequest().body("Company name, email and password are required");
        }
        String email = normalizeEmail(dto.getEmail());
        if (repository.findByEmail(email).isPresent() || candidateRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("An account already exists for this email");
        }
        RecEntity entity = new RecEntity();

        entity.setRole(Role.RECRUITER);
        entity.setEmail(email);
        entity.setPassword(encoder.encode(dto.getPassword()));
        entity.setCompanyName(dto.getCompanyName().trim());
        entity.setHrLocation(cleanOptional(dto.getHrLocation()));
        entity.setHrName(cleanOptional(dto.getHrName()));
        entity.setWeblink(cleanOptional(dto.getWeblink()));
        entity.setHrPhone(cleanOptional(dto.getHrPhone()));

        repository.save(entity);

        return ResponseEntity.status(HttpStatus.CREATED).body("Recruiter account created successfully");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String cleanOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
