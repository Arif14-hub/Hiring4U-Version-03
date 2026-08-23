package com.hiring4u.service;

import com.hiring4u.dto.PasswordResetConfirmDTO;
import com.hiring4u.dto.PasswordResetRequestDTO;
import com.hiring4u.dto.PasswordResetRequestResponse;
import com.hiring4u.entity.PasswordResetToken;
import com.hiring4u.repository.PasswordResetTokenRepository;
import com.hiring4u.repository.RecruitorsRepository;
import com.hiring4u.repository.RegistrationRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;

@Service
public class PasswordResetService {

    private static final String GENERIC_MESSAGE = "If an account exists for this email, reset instructions have been sent.";

    private final RegistrationRepository candidateRepository;
    private final RecruitorsRepository recruiterRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.password-reset.delivery:browser}")
    private String delivery;

    @Value("${app.password-reset.from:no-reply@hiring4u.local}")
    private String fromAddress;

    @Value("${app.password-reset.token-ttl-minutes:30}")
    private long tokenTtlMinutes;

    public PasswordResetService(RegistrationRepository candidateRepository,
                                RecruitorsRepository recruiterRepository,
                                PasswordResetTokenRepository tokenRepository,
                                BCryptPasswordEncoder passwordEncoder,
                                ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.candidateRepository = candidateRepository;
        this.recruiterRepository = recruiterRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSenderProvider = mailSenderProvider;
    }

    @Transactional
    public PasswordResetRequestResponse requestReset(PasswordResetRequestDTO dto) {
        String email = normalizeEmail(dto.getEmail());
        if (!accountExists(email)) {
            return new PasswordResetRequestResponse(GENERIC_MESSAGE, null);
        }

        String rawToken = createRawToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(Instant.now().plus(tokenTtlMinutes, ChronoUnit.MINUTES));
        tokenRepository.deleteByEmail(email);
        tokenRepository.save(token);

        String resetUrl = baseUrl + "/reset-password.html?token=" + rawToken;
        if ("email".equalsIgnoreCase(delivery)) {
            sendResetEmail(email, resetUrl);
            return new PasswordResetRequestResponse(GENERIC_MESSAGE, null);
        }

        return new PasswordResetRequestResponse("Use the secure reset link below. It expires in " + tokenTtlMinutes + " minutes.", resetUrl);
    }

    @Transactional
    public void resetPassword(PasswordResetConfirmDTO dto) {
        PasswordResetToken token = tokenRepository.findByTokenHash(hash(dto.getToken()))
                .orElseThrow(() -> invalidToken());
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw invalidToken();
        }

        String password = passwordEncoder.encode(dto.getPassword());
        boolean updated = candidateRepository.findByEmail(token.getEmail())
                .map(candidate -> {
                    candidate.setPassword(password);
                    candidateRepository.save(candidate);
                    return true;
                })
                .orElseGet(() -> recruiterRepository.findByEmail(token.getEmail())
                        .map(recruiter -> {
                            recruiter.setPassword(password);
                            recruiterRepository.save(recruiter);
                            return true;
                        })
                        .orElse(false));

        if (!updated) {
            throw invalidToken();
        }
        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
    }

    private boolean accountExists(String email) {
        return candidateRepository.findByEmail(email).isPresent() || recruiterRepository.findByEmail(email).isPresent();
    }

    private void sendResetEmail(String email, String resetUrl) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Email delivery is not configured");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Reset your Hiring4U password");
        message.setText("Use this link to reset your Hiring4U password. It expires in " + tokenTtlMinutes + " minutes:\n\n" + resetUrl);
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "We could not send the reset email. Please try again later.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String createRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private ResponseStatusException invalidToken() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "This reset link is invalid or has expired");
    }
}
