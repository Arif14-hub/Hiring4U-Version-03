package com.hiring4u.service;
import com.hiring4u.Enums.Role;
import com.hiring4u.dto.CanRegDTO;
import com.hiring4u.entity.CanEntity;
import com.hiring4u.repository.RegistrationRepository;
import com.hiring4u.repository.RecruitorsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

@Service
public class CanRegSer {
    private static final long MAX_RESUME_SIZE_BYTES = 5 * 1024 * 1024;

    private final RegistrationRepository registrationRepository;
    private final RecruitorsRepository recruiterRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public CanRegSer(RegistrationRepository registrationRepository,
                     RecruitorsRepository recruiterRepository,
                     BCryptPasswordEncoder passwordEncoder) {
        this.registrationRepository = registrationRepository;
        this.recruiterRepository = recruiterRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public ResponseEntity<String> processRegistration(CanRegDTO dto) {
       return processRegistration(dto, null, false);
    }

    @Transactional
    public ResponseEntity<String> processRegistration(CanRegDTO dto, MultipartFile resume) {
       return processRegistration(dto, resume, true);
    }

    private ResponseEntity<String> processRegistration(CanRegDTO dto, MultipartFile resume, boolean resumeRequired) {
       if (resumeRequired) {
           String resumeError = resumeValidationError(resume);
           if (resumeError != null) {
               return ResponseEntity.badRequest().body(resumeError);
           }
       }
       if (isBlank(dto.getFullName()) || isBlank(dto.getEmail()) || isBlank(dto.getPassword())) {
           return ResponseEntity.badRequest().body("Name, email and password are required");
       }
       String email = normalizeEmail(dto.getEmail());
       if (registrationRepository.findByEmail(email).isPresent() || recruiterRepository.findByEmail(email).isPresent()) {
           return ResponseEntity.status(HttpStatus.CONFLICT).body("An account already exists for this email");
       }
       CanEntity entity = new CanEntity();
       entity.setRole(Role.CANDIDATE);
       entity.setEmail(email);
       entity.setFullName(dto.getFullName().trim());
       entity.setPassword(passwordEncoder.encode(dto.getPassword()));
       entity.setPhoneNumber(cleanOptional(dto.getPhoneNumber()));
       entity.setDob(cleanOptional(dto.getDob()));
       entity.setLocation(cleanOptional(dto.getLocation()));
       if (resumeRequired) {
           try {
               entity.setResumeData(resume.getBytes());
           } catch (IOException exception) {
               return ResponseEntity.badRequest().body("We could not read the resume file");
           }
           entity.setResumeFileName(resume.getOriginalFilename().trim());
           entity.setResumeContentType(cleanOptional(resume.getContentType()));
       }

       registrationRepository.save(entity);

       return ResponseEntity.status(HttpStatus.CREATED).body("Candidate account created successfully");
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

    private String resumeValidationError(MultipartFile resume) {
        if (resume == null || resume.isEmpty()) {
            return "Choose a PDF, DOC, or DOCX resume to upload";
        }
        if (resume.getSize() > MAX_RESUME_SIZE_BYTES) {
            return "Resume files must be 5 MB or smaller";
        }
        String fileName = resume.getOriginalFilename();
        String normalizedFileName = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (!(normalizedFileName.endsWith(".pdf") || normalizedFileName.endsWith(".doc") || normalizedFileName.endsWith(".docx"))) {
            return "Resume files must be PDF, DOC, or DOCX";
        }
        return null;
    }
}
