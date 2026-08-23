package com.hiring4u.dto;

public record ProfileDTO(
        String role,
        String email,
        String displayName,
        String fullName,
        String phoneNumber,
        String dob,
        String location,
        String professionalTitle,
        String skills,
        Integer experienceYears,
        String education,
        String bio,
        String linkedInUrl,
        String portfolioUrl,
        boolean resumeAvailable,
        String resumeFileName,
        String companyName,
        String hrName,
        String hrPhone,
        String hrLocation,
        String weblink,
        String industry,
        String companySize,
        String companyAddress,
        String companyDescription
) {
}
