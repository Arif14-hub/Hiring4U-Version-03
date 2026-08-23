package com.hiring4u.controller;
import com.hiring4u.dto.CanRegDTO;
import com.hiring4u.service.CanRegSer;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/can")
public class CandidateRegistration {
    private final CanRegSer candidateService;

    public CandidateRegistration(CanRegSer candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping(value = "/registered", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> CanRegister(@Valid @RequestPart("candidate") CanRegDTO dto,
                                              @RequestPart("resume") MultipartFile resume) {
        return candidateService.processRegistration(dto, resume);
    }

    @PostMapping(value = "/registered", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> CanRegisterWithoutResume(@Valid @RequestBody CanRegDTO dto) {
        return ResponseEntity.badRequest().body("A resume is required to create a candidate account");
    }
}
