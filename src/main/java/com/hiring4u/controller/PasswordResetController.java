package com.hiring4u.controller;

import com.hiring4u.dto.PasswordResetConfirmDTO;
import com.hiring4u.dto.PasswordResetRequestDTO;
import com.hiring4u.dto.PasswordResetRequestResponse;
import com.hiring4u.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/reset-request")
    public PasswordResetRequestResponse requestReset(@Valid @RequestBody PasswordResetRequestDTO dto) {
        return passwordResetService.requestReset(dto);
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetConfirmDTO dto) {
        passwordResetService.resetPassword(dto);
        return ResponseEntity.ok("Password updated successfully. You can now log in.");
    }
}
