package com.example.leadgen_backend.service.impl;

import com.example.leadgen_backend.enums.AssignmentStatus;
import com.example.leadgen_backend.enums.LeadStatus;
import com.example.leadgen_backend.model.BusinessProfile;
import com.example.leadgen_backend.model.Lead;
import com.example.leadgen_backend.model.LeadAssignment;
import com.example.leadgen_backend.repository.BusinessProfileRepository;
import com.example.leadgen_backend.repository.LeadAssignmentRepository;
import com.example.leadgen_backend.repository.LeadRepository;
import com.example.leadgen_backend.repository.UserRepository;
import com.example.leadgen_backend.service.LeadService;
import com.example.leadgen_backend.service.ScoringService;
import com.example.leadgen_backend.util.GeoUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final LeadAssignmentRepository assignmentRepository;

    private final UserRepository userRepository;
    private final ScoringService scoringService;

    public LeadServiceImpl(LeadRepository leadRepository,
                           BusinessProfileRepository businessProfileRepository,
                           LeadAssignmentRepository assignmentRepository,
                           ScoringService scoringService,
                           UserRepository userRepository) {
        this.leadRepository = leadRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.assignmentRepository = assignmentRepository;
        this.scoringService = scoringService;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Lead createLead(Lead lead) {
        Lead saved = leadRepository.save(lead);
        // In a real system this could be async, but here we do it synchronously
        computeMatchesAndCreateAssignments(saved);
        return saved;
    }

    @Override
    @Transactional
    public void computeMatchesAndCreateAssignments(Lead lead) {
        // 1) Load all active business profiles
        List<BusinessProfile> candidates = businessProfileRepository.findAll()
                .stream()
                .filter(b -> Boolean.TRUE.equals(b.getActive()))
                .filter(b -> b.getServiceRadiusKm() == null || b.getServiceRadiusKm() > 0)
                .toList();

        if (candidates.isEmpty()) {
            return; // nothing to match against
        }

        Double leadLat = lead.getLatitude();
        Double leadLon = lead.getLongitude();

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(30 * 60); // 30-minute claim window
        int maxAssignments = 5; // configurable later

        // 2) Filter by distance + compute scores
        List<LeadAssignment> assignments = candidates.stream()
                .filter(business -> {
                    // If we don't have coordinates for either side, skip distance filtering
                    if (leadLat == null || leadLon == null
                            || business.getUser() == null
                            || business.getUser().getLatitude() == null
                            || business.getUser().getLongitude() == null) {
                        return true;
                    }
                    double distance = GeoUtil.distanceKm(
                            leadLat,
                            leadLon,
                            business.getUser().getLatitude(),
                            business.getUser().getLongitude()
                    );
                    Integer radius = business.getServiceRadiusKm();
                    return radius == null || distance <= radius;
                })
                .map(business -> {
                    int score = scoringService.computeScore(lead, business);
                    return LeadAssignment.builder()
                            .leadId(lead.getId())
                            .businessId(business.getId())
                            .score(score)
                            .isClaimed(false)
                            .expiresAt(expiresAt)
                            .status(AssignmentStatus.NOTIFIED)
                            .build();
                })
                .sorted(Comparator.comparingInt(LeadAssignment::getScore).reversed())
                .limit(maxAssignments)
                .toList();

        if (assignments.isEmpty()) {
            return; // no suitable matches
        }

        // 3) Assign rank based on sorted order
        int rank = 1;
        for (LeadAssignment assignment : assignments) {
            assignment.setRank(rank++);
        }

        // 4) Persist assignments
        assignmentRepository.saveAll(assignments);

        // 5) Update lead status to ASSIGNED
        lead.setStatus(LeadStatus.ASSIGNED);
        leadRepository.save(lead);
    }
}

