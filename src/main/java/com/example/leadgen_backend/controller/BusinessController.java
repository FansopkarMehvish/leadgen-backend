package com.example.leadgen_backend.controller;


import com.example.leadgen_backend.enums.Role;
import com.example.leadgen_backend.model.LeadAssignment;
import com.example.leadgen_backend.model.User;
import com.example.leadgen_backend.repository.LeadAssignmentRepository;
import com.example.leadgen_backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final LeadAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    public BusinessController(LeadAssignmentRepository assignmentRepository, UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/leads")
    @PreAuthorize("hasAnyRole('BUSINESS','ADMIN')")
    public ResponseEntity<?> getLeadsForBusiness(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long businessId = user.getId();
        var assignments = assignmentRepository.findByBusinessIdAndIsClaimedFalseOrderByScoreDesc(businessId);
        return ResponseEntity.ok(assignments);
    }

    @PostMapping("/claim/{assignmentId}")
    @PreAuthorize("hasAnyRole('BUSINESS','ADMIN')")
    public ResponseEntity<?> claimLead(@PathVariable Long assignmentId, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeadAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Only the owning business or ADMIN can claim
        if (!assignment.getBusinessId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You are not allowed to claim this lead");
        }

        // TODO: implement optimistic locking + status updates
        return ResponseEntity.ok().body("claimed");
    }
}