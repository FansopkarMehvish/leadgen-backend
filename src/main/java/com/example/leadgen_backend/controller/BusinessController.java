package com.example.leadgen_backend.controller;

import com.example.leadgen_backend.model.User;
import com.example.leadgen_backend.repository.LeadAssignmentRepository;
import com.example.leadgen_backend.repository.UserRepository;
import com.example.leadgen_backend.service.LeadAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final LeadAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final LeadAssignmentService assignmentService;

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
    public ResponseEntity<?> claimLead(
            @PathVariable Long assignmentId,
            Authentication auth) {

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        assignmentService.claimLead(assignmentId, user.getId());

        return ResponseEntity.ok("Lead claimed successfully");
    }
}