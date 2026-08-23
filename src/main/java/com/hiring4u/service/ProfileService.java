package com.hiring4u.service;

import com.hiring4u.dto.ProfileDTO;
import com.hiring4u.dto.ProfileUpdateDTO;
import com.hiring4u.dto.ResumeDownload;
import com.hiring4u.entity.CanEntity;
import com.hiring4u.entity.RecEntity;
import com.hiring4u.repository.RecruitorsRepository;
import com.hiring4u.repository.RegistrationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Locale;

@Service
public class ProfileService {

    private static final long MAX_RESUME_SIZE_BYTES = 5 * 1024 * 1024;

    private final RegistrationRepository candidateRepository;
    private final RecruitorsRepository recruiterRepository;

    public ProfileService(RegistrationRepository candidateRepository, RecruitorsRepository recruiterRepository) {
        this.candidateRepository = candidateRepository;
        this.recruiterRepository = recruiterRepository;
    }

    @Transactional(readOnly = true)
    public ProfileDTO getProfile(String email) {
        return candidateRepository.findByEmail(email)
                .map(this::candidateProfile)
                .or(() -> recruiterRepository.findByEmail(email).map(this::recruiterProfile))
                .orElseThrow(() -> profileNotFound());
    }

    @Transactional
    public ProfileDTO updateProfile(String email, ProfileUpdateDTO update) {
        return candidateRepository.findByEmail(email)
                .map(candidate -> {
                    candidate.setFullName(required(update.fullName(), "Full name"));
                    candidate.setPhoneNumber(optional(update.phoneNumber()));
                    candidate.setDob(optional(update.dob()));
                    candidate.setLocation(optional(update.location()));
                    candidate.setProfessionalTitle(optional(update.professionalTitle()));
                    candidate.setSkills(optional(update.skills()));
                    candidate.setExperienceYears(nonNegative(update.experienceYears(), "Experience years"));
                    candidate.setEducation(optional(update.education()));
                    candidate.setBio(optional(update.bio()));
                    candidate.setLinkedInUrl(optional(update.linkedInUrl()));
                    candidate.setPortfolioUrl(optional(update.portfolioUrl()));
                    return candidateProfile(candidateRepository.save(candidate));
                })
                .orElseGet(() -> recruiterRepository.findByEmail(email)
                        .map(recruiter -> {
                            recruiter.setCompanyName(required(update.companyName(), "Company name"));
                            recruiter.setHrName(optional(update.hrName()));
                            recruiter.setHrPhone(optional(update.hrPhone()));
                            recruiter.setHrLocation(optional(update.hrLocation()));
                            recruiter.setWeblink(optional(update.weblink()));
                            recruiter.setIndustry(optional(update.industry()));
                            recruiter.setCompanySize(optional(update.companySize()));
                            recruiter.setCompanyAddress(optional(update.companyAddress()));
                            recruiter.setCompanyDescription(optional(update.companyDescription()));
                            recruiter.setLinkedInUrl(optional(update.linkedInUrl()));
                            return recruiterProfile(recruiterRepository.save(recruiter));
                        })
                        .orElseThrow(this::profileNotFound));
    }

    @Transactional
    public ProfileDTO uploadResume(String email, MultipartFile file) {
        CanEntity candidate = candidateRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate accounts can upload a resume"));
        validateResume(file);
        try {
            candidate.setResumeData(file.getBytes());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "We could not read the resume file", exception);
        }
        candidate.setResumeFileName(file.getOriginalFilename().trim());
        candidate.setResumeContentType(optional(file.getContentType()));
        return candidateProfile(candidateRepository.save(candidate));
    }

    @Transactional(readOnly = true)
    public ResumeDownload downloadResume(String email) {
        CanEntity candidate = candidateRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate accounts have a resume"));
        if (candidate.getResumeData() == null || candidate.getResumeFileName() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No resume has been uploaded yet");
        }
        return new ResumeDownload(candidate.getResumeData(), candidate.getResumeFileName(), candidate.getResumeContentType());
    }

    private ProfileDTO candidateProfile(CanEntity candidate) {
        return new ProfileDTO(
                candidate.getRole().name(), candidate.getEmail(), candidate.getFullName(), candidate.getFullName(),
                candidate.getPhoneNumber(), candidate.getDob(), candidate.getLocation(), candidate.getProfessionalTitle(),
                candidate.getSkills(), candidate.getExperienceYears(), candidate.getEducation(), candidate.getBio(),
                candidate.getLinkedInUrl(), candidate.getPortfolioUrl(), candidate.getResumeData() != null,
                candidate.getResumeFileName(), null, null, null, null, null, null, null, null, null
        );
    }

    private ProfileDTO recruiterProfile(RecEntity recruiter) {
        return new ProfileDTO(
                recruiter.getRole().name(), recruiter.getEmail(), recruiter.getCompanyName(), null,
                null, null, null, null, null, null, null, null, recruiter.getLinkedInUrl(), null,
                false, null, recruiter.getCompanyName(), recruiter.getHrName(), recruiter.getHrPhone(),
                recruiter.getHrLocation(), recruiter.getWeblink(), recruiter.getIndustry(), recruiter.getCompanySize(),
                recruiter.getCompanyAddress(), recruiter.getCompanyDescription()
        );
    }

    private void validateResume(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a PDF, DOC, or DOCX resume to upload");
        }
        if (file.getSize() > MAX_RESUME_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resume files must be 5 MB or smaller");
        }
        String fileName = file.getOriginalFilename();
        String normalizedFileName = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (!(normalizedFileName.endsWith(".pdf") || normalizedFileName.endsWith(".doc") || normalizedFileName.endsWith(".docx"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resume files must be PDF, DOC, or DOCX");
        }
    }

    private String required(String value, String label) {
        String normalized = optional(value);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " is required");
        }
        return normalized;
    }

    private String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Integer nonNegative(Integer value, String label) {
        if (value != null && value < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " cannot be negative");
        }
        return value;
    }

    private ResponseStatusException profileNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");
    }
}
