package com.example.leadgen_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String token;

    private String type; // "EMAIL" or "PHONE"

    private Instant expiresAt;

    private Boolean used = false;

    private Instant createdAt = Instant.now();
}
