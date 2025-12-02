package com.example.leadgen_backend.model;

import com.example.leadgen_backend.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "lead_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long leadId;
    private Long businessId;

    private Integer score;
    private Integer rank;

    private Boolean isClaimed = false;
    private Instant claimedAt;
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "assignment_status_enum")
    private AssignmentStatus status = AssignmentStatus.NOTIFIED;

    @Version
    private Long version;
}
