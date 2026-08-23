package com.hiring4u.dto;

public record ProfileUpdateDTO(
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
