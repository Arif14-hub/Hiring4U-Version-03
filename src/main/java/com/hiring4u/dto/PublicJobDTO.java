package com.hiring4u.dto;

import com.hiring4u.entity.JobsEntity;

import java.time.LocalDate;

public record PublicJobDTO(
        Long id,
        String title,
        String description,
        String requireskills,
        double salary,
        String location,
        Double latitude,
        Double longitude,
        LocalDate postedDate,
        String company
) {
    public static PublicJobDTO from(JobsEntity job) {
        return new PublicJobDTO(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getRequireskills(),
                job.getSalary(),
                job.getLocation(),
                job.getLatitude(),
                job.getLongitude(),
                job.getPostedDate(),
                job.getRecruiter().getCompanyName()
        );
    }
}
