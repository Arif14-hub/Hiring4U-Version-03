package com.hiring4u.service;
import com.hiring4u.dto.JobsListingDTO;
import com.hiring4u.entity.JobsEntity;
import com.hiring4u.entity.RecEntity;
import com.hiring4u.repository.JobsRepository;
import com.hiring4u.repository.RecruitorsRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class JobsListing {

        @Autowired
        private JobsRepository repository;

        @Autowired
        private RecruitorsRepository recruitersRepository;

        public ResponseEntity<String> processjoblisting(JobsListingDTO dto){

            JobsEntity entity = new JobsEntity();

            entity.setSalary(dto.getSalary());
            entity.setLocation(dto.getLocation().trim());
            entity.setLatitude(isValidLatitude(dto.getLatitude()) ? dto.getLatitude() : null);
            entity.setLongitude(isValidLongitude(dto.getLongitude()) ? dto.getLongitude() : null);
            entity.setPostedDate(dto.getPostedDate());
            entity.setDescription(dto.getDescription().trim());
            entity.setRequireskills(dto.getRequireskills().trim());
            entity.setTitle(dto.getTitle().trim());

            // 🔐 Get logged-in recruiter
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            String email = authentication.getName();

            RecEntity recruiter = recruitersRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException("Recruiter not found"));

            entity.setRecruiter(recruiter);

            repository.save(entity);

            return ResponseEntity.ok("Job Posted Successfully");
        }

    public List<JobsEntity> getAllJobs() {
        return repository.findAllByOrderByPostedDateDescIdDesc();
    }

    public List<JobsEntity> searchAvailableJobs(String search,
                                                 String role,
                                                 String location,
                                                 Double minimumCtc,
                                                 Double maximumCtc,
                                                 Integer postedWithinDays,
                                                 String sort) {
        String searchTerm = normalizeSearchTerm(search);
        String roleTerm = normalizeSearchTerm(role);
        String locationTerm = normalizeSearchTerm(location);

        Specification<JobsEntity> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<JobsEntity, RecEntity> recruiter = root.join("recruiter", JoinType.LEFT);

            if (searchTerm != null) {
                String searchPattern = containsPattern(searchTerm);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("requireskills")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(recruiter.get("companyName")), searchPattern)
                ));
            }
            if (roleTerm != null) {
                String rolePattern = containsPattern(roleTerm);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), rolePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("requireskills")), rolePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), rolePattern)
                ));
            }
            if (locationTerm != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), containsPattern(locationTerm)));
            }
            if (minimumCtc != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("salary"), Math.max(0, minimumCtc)));
            }
            if (maximumCtc != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salary"), Math.max(0, maximumCtc)));
            }
            if (postedWithinDays != null && postedWithinDays > 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("postedDate"), LocalDate.now().minusDays(postedWithinDays)));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };

        return repository.findAll(specification, sortJobs(sort));
    }

    private String normalizeSearchTerm(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String containsPattern(String value) {
        return "%" + value.toLowerCase(Locale.ROOT) + "%";
    }

    private boolean isValidLatitude(Double latitude) {
        return latitude != null && latitude >= -90 && latitude <= 90;
    }

    private boolean isValidLongitude(Double longitude) {
        return longitude != null && longitude >= -180 && longitude <= 180;
    }

    private Sort sortJobs(String requestedSort) {
        String sort = requestedSort == null ? "recent" : requestedSort.trim().toLowerCase(Locale.ROOT);
        return switch (sort) {
            case "ctc_asc" -> Sort.by(Sort.Order.asc("salary"), Sort.Order.desc("postedDate"), Sort.Order.desc("id"));
            case "ctc_desc" -> Sort.by(Sort.Order.desc("salary"), Sort.Order.desc("postedDate"), Sort.Order.desc("id"));
            case "title_asc" -> Sort.by(Sort.Order.asc("title").ignoreCase(), Sort.Order.desc("postedDate"), Sort.Order.desc("id"));
            case "location_asc" -> Sort.by(Sort.Order.asc("location").ignoreCase(), Sort.Order.desc("postedDate"), Sort.Order.desc("id"));
            default -> Sort.by(Sort.Order.desc("postedDate"), Sort.Order.desc("id"));
        };
    }
}
