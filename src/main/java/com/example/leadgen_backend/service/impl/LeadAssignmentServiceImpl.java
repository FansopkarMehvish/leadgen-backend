package com.example.leadgen_backend.service.impl;

import com.example.leadgen_backend.enums.AssignmentStatus;
import com.example.leadgen_backend.enums.LeadStatus;
import com.example.leadgen_backend.model.Lead;
import com.example.leadgen_backend.model.LeadAssignment;
import com.example.leadgen_backend.repository.LeadAssignmentRepository;
import com.example.leadgen_backend.repository.LeadRepository;
import com.example.leadgen_backend.service.LeadAssignmentService;
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

    public LeadAssignmentServiceImpl(
            LeadAssignmentRepository assignmentRepository,
            LeadRepository leadRepository) {
        this.assignmentRepository = assignmentRepository;
        this.leadRepository = leadRepository;
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
    }
}

