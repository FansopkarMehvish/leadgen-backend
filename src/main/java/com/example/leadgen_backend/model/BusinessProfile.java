package com.example.leadgen_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "business_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessProfile {
    @Id
    private Long id; // same as user id, one-to-one

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    private String businessName;
    private String description;
    private Integer minBudget;
    private Integer maxBudget;
    private Integer serviceRadiusKm;
    private Boolean active = true;
}