package com.hiring4u.service;
import com.hiring4u.dto.ApplicationDTO;
import com.hiring4u.entity.ApplicationEntity;
import com.hiring4u.entity.CanEntity;
import com.hiring4u.repository.ApplicationRepository;
import com.hiring4u.repository.JobsRepository;
import com.hiring4u.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ApplicationService {
    @Autowired
    private ApplicationRepository Repository;
    @Autowired
    private RegistrationRepository registrationRepository;
    @Autowired
    private JobsRepository jobsRepository;

    public List<ApplicationEntity> getApplicants(Long jobId){
        return Repository.findByJobId(jobId);
    }

    public ResponseEntity<String> apply(ApplicationDTO dto){

        if (dto.getCandidateId() == null || dto.getJobId() == null) {
            return ResponseEntity.badRequest().body("Candidate id and job id are required");
        }
        if (!registrationRepository.existsById(dto.getCandidateId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Candidate not found");
        }
        if (!jobsRepository.existsById(dto.getJobId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job not found");
        }
        if(Repository.existsByCandidateIdAndJobId(dto.getCandidateId(), dto.getJobId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Candidate already applied for this job");
        }
        ApplicationEntity application = new ApplicationEntity();
        application.setCandidateId(dto.getCandidateId());
        application.setJobId(dto.getJobId());
        application.setAppliedDate(LocalDate.now());

        Repository.save(application);

        return ResponseEntity.ok("Job Applied Successfully");
    }

    public ResponseEntity<String> applyForCandidate(ApplicationDTO dto, String email) {
        CanEntity candidate = registrationRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated candidate was not found"));
        dto.setCandidateId(candidate.getId());
        return apply(dto);
    }
}
