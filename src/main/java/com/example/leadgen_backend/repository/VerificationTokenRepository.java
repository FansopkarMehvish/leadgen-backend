package com.example.leadgen_backend.repository;

import com.example.leadgen_backend.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    List<VerificationToken> findByUserIdAndType(Long userId, String type);

    List<VerificationToken> findByUserIdAndUsedFalseAndExpiresAtAfter(Long userId, Instant now);
}
