package com.hiring4u.controller;
import com.hiring4u.dto.ApplicationDTO;
import com.hiring4u.dto.PublicJobDTO;
import com.hiring4u.service.ApplicationService;
import com.hiring4u.service.JobsListing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("candidate")
public class CandidateController {
    @Autowired
    JobsListing List;
    @Autowired
    ApplicationService applicationService;


    @GetMapping("/jobs")
    public List<PublicJobDTO> getJobs(@RequestParam(required = false) String search,
                                      @RequestParam(required = false) String role,
                                      @RequestParam(required = false) String location,
                                      @RequestParam(required = false) Double minCtc,
                                      @RequestParam(required = false) Double maxCtc,
                                      @RequestParam(required = false) Integer postedWithinDays,
                                      @RequestParam(defaultValue = "recent") String sort) {
        return List.searchAvailableJobs(search, role, location, minCtc, maxCtc, postedWithinDays, sort)
                .stream()
                .map(PublicJobDTO::from)
                .toList();
    }

    @PostMapping("/apply")
    public ResponseEntity<String> apply(@RequestBody ApplicationDTO dto, Authentication authentication) {
        return applicationService.applyForCandidate(dto, authentication.getName());

    }

}
