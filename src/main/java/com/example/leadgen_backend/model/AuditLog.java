package com.example.leadgen_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entityType; // e.g., "Lead", "User", "BusinessProfile"
    private Long entityId;

    private String action; // e.g., "CREATE", "UPDATE", "DELETE", "CLAIM"

    private String performedBy; // User email or system
    private Long performedByUserId;

    @Column(length = 4000)
    private String oldValues; // JSON string of old values

    @Column(length = 4000)
    private String newValues; // JSON string of new values

    private String ipAddress;

    private Instant createdAt = Instant.now();
}
