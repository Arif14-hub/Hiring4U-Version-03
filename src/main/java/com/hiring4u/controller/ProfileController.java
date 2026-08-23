package com.hiring4u.controller;

import com.hiring4u.dto.ProfileDTO;
import com.hiring4u.dto.ProfileUpdateDTO;
import com.hiring4u.dto.ResumeDownload;
import com.hiring4u.service.ProfileService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ProfileDTO getProfile(Authentication authentication) {
        return profileService.getProfile(authentication.getName());
    }

    @PutMapping
    public ProfileDTO updateProfile(Authentication authentication, @RequestBody ProfileUpdateDTO update) {
        return profileService.updateProfile(authentication.getName(), update);
    }

    @PostMapping(value = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileDTO uploadResume(Authentication authentication, @RequestPart("file") MultipartFile file) {
        return profileService.uploadResume(authentication.getName(), file);
    }

    @GetMapping("/resume")
    public ResponseEntity<byte[]> downloadResume(Authentication authentication) {
        ResumeDownload resume = profileService.downloadResume(authentication.getName());
        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        if (resume.contentType() != null) {
            try {
                contentType = MediaType.parseMediaType(resume.contentType());
            } catch (IllegalArgumentException ignored) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(resume.fileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(resume.data());
    }
}
