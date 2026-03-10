package com.example.leadgen_backend.service;

import com.example.leadgen_backend.model.User;
import com.example.leadgen_backend.model.VerificationToken;
import com.example.leadgen_backend.repository.UserRepository;
import com.example.leadgen_backend.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public String generateEmailVerificationToken(Long userId) {
        // Invalidate old tokens
        tokenRepository.findByUserIdAndType(userId, "EMAIL")
                .forEach(token -> token.setUsed(true));

        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken.builder()
                .userId(userId)
                .token(token)
                .type("EMAIL")
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .used(false)
                .build();

        tokenRepository.save(verificationToken);
        log.info("Generated email verification token for user: {}", userId);

        return token;
    }

    @Transactional
    public String generatePhoneVerificationToken(Long userId) {
        // Invalidate old tokens
        tokenRepository.findByUserIdAndType(userId, "PHONE")
                .forEach(token -> token.setUsed(true));

        // Generate 6-digit code
        String code = String.format("%06d", (int) (Math.random() * 1000000));

        VerificationToken verificationToken = VerificationToken.builder()
                .userId(userId)
                .token(code)
                .type("PHONE")
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .used(false)
                .build();

        tokenRepository.save(verificationToken);
        log.info("Generated phone verification code for user: {}", userId);

        return code;
    }

    @Transactional
    public boolean verifyEmail(String token) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            log.warn("Invalid email verification token");
            return false;
        }

        VerificationToken verificationToken = tokenOpt.get();

        if (verificationToken.getUsed() || verificationToken.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Email verification token expired or already used");
            return false;
        }

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerified(true);
        userRepository.save(user);

        log.info("Email verified for user: {}", user.getId());
        return true;
    }

    @Transactional
    public boolean verifyPhone(Long userId, String code) {
        Optional<VerificationToken> tokenOpt = tokenRepository
                .findByUserIdAndType(userId, "PHONE")
                .stream()
                .filter(t -> !t.getUsed() && t.getExpiresAt().isAfter(Instant.now()))
                .findFirst();

        if (tokenOpt.isEmpty()) {
            log.warn("Invalid phone verification code for user: {}", userId);
            return false;
        }

        VerificationToken verificationToken = tokenOpt.get();

        if (!verificationToken.getToken().equals(code)) {
            log.warn("Phone verification code mismatch for user: {}", userId);
            return false;
        }

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        log.info("Phone verified for user: {}", userId);
        return true;
    }

    public boolean isEmailVerified(Long userId) {
        return tokenRepository.findByUserIdAndType(userId, "EMAIL")
                .stream()
                .anyMatch(t -> t.getUsed() && t.getExpiresAt().isAfter(Instant.now().minus(30, ChronoUnit.DAYS)));
    }

    public boolean isPhoneVerified(Long userId) {
        return tokenRepository.findByUserIdAndType(userId, "PHONE")
                .stream()
                .anyMatch(t -> t.getUsed() && t.getExpiresAt().isAfter(Instant.now().minus(30, ChronoUnit.DAYS)));
    }
}
