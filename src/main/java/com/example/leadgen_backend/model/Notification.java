package com.example.leadgen_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String type; // "LEAD_ASSIGNED", "LEAD_CLAIMED", "LEAD_EXPIRED", etc.

    private String title;

    @Column(length = 2000)
    private String message;

    private Long relatedLeadId;
    private Long relatedAssignmentId;

    private Boolean isRead = false;

    private String channel; // "IN_APP", "EMAIL", "SMS"

    private Instant sentAt = Instant.now();
    private Instant readAt;
}
