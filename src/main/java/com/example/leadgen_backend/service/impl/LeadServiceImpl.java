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
import com.example.leadgen_backend.service.NotificationService;
import com.example.leadgen_backend.service.ScoringService;
import com.example.leadgen_backend.util.GeoUtil;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final NotificationService notificationService;

    public LeadServiceImpl(LeadRepository leadRepository,
                           BusinessProfileRepository businessProfileRepository,
                           LeadAssignmentRepository assignmentRepository,
                           ScoringService scoringService,
                           UserRepository userRepository,
                           NotificationService notificationService) {
        this.leadRepository = leadRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.assignmentRepository = assignmentRepository;
        this.scoringService = scoringService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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

        // 6) Send notifications to matched businesses
        for (LeadAssignment assignment : assignments) {
            notificationService.createNotification(
                    assignment.getBusinessId(),
                    "LEAD_ASSIGNED",
                    "New Lead Available",
                    "A new lead matching your profile has been assigned to you. Score: " + assignment.getScore(),
                    lead.getId(),
                    assignment.getId()
            );
        }
    }

    @Override
    public Lead getLeadById(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found with id: " + id));
    }

    @Override
    public List<Lead> getAllLeads() {
        return leadRepository.findAll();
    }

    @Override
    public Page<Lead> getAllLeadsPaged(Pageable pageable) {
        return leadRepository.findAll(pageable);
    }

    @Override
    public List<Lead> getLeadsByStatus(LeadStatus status) {
        return leadRepository.findByStatus(status);
    }

    @Override
    public Page<Lead> getLeadsByStatus(LeadStatus status, Pageable pageable) {
        return leadRepository.findByStatus(status, pageable);
    }

    @Override
    public List<Lead> getLeadsByCategory(Long categoryId) {
        return leadRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Lead> getLeadsByStatusAndCategory(LeadStatus status, Long categoryId) {
        return leadRepository.findByStatusAndCategoryId(status, categoryId);
    }

    @Override
    @Transactional
    public Lead updateLead(Long id, Lead lead) {
        Lead existing = getLeadById(id);

        existing.setCustomerName(lead.getCustomerName());
        existing.setCustomerPhone(lead.getCustomerPhone());
        existing.setCustomerEmail(lead.getCustomerEmail());
        existing.setCategoryId(lead.getCategoryId());
        existing.setDescription(lead.getDescription());
        existing.setBudgetFrom(lead.getBudgetFrom());
        existing.setBudgetTo(lead.getBudgetTo());
        existing.setLocationText(lead.getLocationText());
        existing.setLatitude(lead.getLatitude());
        existing.setLongitude(lead.getLongitude());
        existing.setUrgency(lead.getUrgency());

        return leadRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteLead(Long id) {
        Lead lead = getLeadById(id);
        leadRepository.delete(lead);
    }
}

