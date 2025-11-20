package com.example.leadgen_backend.model;

import com.example.leadgen_backend.enums.LeadStatus;
import com.example.leadgen_backend.enums.LeadUrgency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String customerPhone;
    private String customerEmail;

    private Long categoryId;
    private String description;

    private Integer budgetFrom;
    private Integer budgetTo;

    private String locationText;
    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "lead_urgency_enum")
    private LeadUrgency urgency;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "lead_status_enum")
    private LeadStatus status = LeadStatus.NEW;

    private Instant createdAt = Instant.now();
}
