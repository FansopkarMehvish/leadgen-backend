package com.example.leadgen_backend.service.impl;

import com.example.leadgen_backend.enums.AssignmentStatus;
import com.example.leadgen_backend.enums.LeadStatus;
import com.example.leadgen_backend.model.Lead;
import com.example.leadgen_backend.model.LeadAssignment;
import com.example.leadgen_backend.repository.LeadAssignmentRepository;
import com.example.leadgen_backend.repository.LeadRepository;
import com.example.leadgen_backend.service.LeadAssignmentService;
import com.example.leadgen_backend.service.NotificationService;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class LeadAssignmentServiceImpl implements LeadAssignmentService {

    private final LeadAssignmentRepository assignmentRepository;
    private final LeadRepository leadRepository;
    private final NotificationService notificationService;

    public LeadAssignmentServiceImpl(
            LeadAssignmentRepository assignmentRepository,
            LeadRepository leadRepository,
            NotificationService notificationService) {
        this.assignmentRepository = assignmentRepository;
        this.leadRepository = leadRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void claimLead(Long assignmentId, Long businessId) {

        LeadAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Validate owner
        if (!assignment.getBusinessId().equals(businessId)) {
            throw new AccessDeniedException("Not your assignment");
        }

        // Already claimed?
        if (assignment.getIsClaimed()) {
            throw new RuntimeException("Lead already claimed");
        }

        // Expired?
        if (assignment.getExpiresAt() != null &&
                Instant.now().isAfter(assignment.getExpiresAt())) {
            assignment.setStatus(AssignmentStatus.EXPIRED);
            assignmentRepository.save(assignment);
            throw new RuntimeException("Assignment has expired");
        }

        // Claim successfully
        assignment.setIsClaimed(true);
        assignment.setStatus(AssignmentStatus.CLAIMED);
        assignment.setClaimedAt(Instant.now());
        assignmentRepository.save(assignment);

        // Update main Lead status
        Lead lead = leadRepository.findById(assignment.getLeadId())
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        lead.setStatus(LeadStatus.CLAIMED);
        leadRepository.save(lead);

        // Expire other business assignments
        List<LeadAssignment> others =
                assignmentRepository.findByLeadId(assignment.getLeadId());

        for (LeadAssignment other : others) {
            if (!other.getId().equals(assignmentId)) {
                other.setStatus(AssignmentStatus.EXPIRED);
            }
        }

        assignmentRepository.saveAll(others);

        // Send notification to business that claimed
        notificationService.createNotification(
                businessId,
                "LEAD_CLAIMED",
                "Lead Claimed Successfully",
                "You have successfully claimed lead #" + lead.getId() + " for " + lead.getCustomerName(),
                lead.getId(),
                assignment.getId()
        );

        // Send notifications to other businesses that they lost the opportunity
        for (LeadAssignment other : others) {
            if (!other.getId().equals(assignmentId)) {
                notificationService.createNotification(
                        other.getBusinessId(),
                        "LEAD_EXPIRED",
                        "Lead No Longer Available",
                        "Lead #" + lead.getId() + " has been claimed by another business",
                        lead.getId(),
                        other.getId()
                );
            }
        }
    }
}
