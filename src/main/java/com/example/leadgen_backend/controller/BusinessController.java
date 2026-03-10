package com.example.leadgen_backend.controller;

import com.example.leadgen_backend.model.User;
import com.example.leadgen_backend.repository.LeadAssignmentRepository;
import com.example.leadgen_backend.repository.UserRepository;
import com.example.leadgen_backend.service.LeadAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/leads/paged")
    @PreAuthorize("hasAnyRole('BUSINESS','ADMIN')")
    public ResponseEntity<?> getLeadsForBusinessPaged(Authentication authentication, Pageable pageable) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long businessId = user.getId();
        List assignments = assignmentRepository.findByBusinessIdAndIsClaimedFalseOrderByScoreDesc(businessId);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), assignments.size());
        Page page = new PageImpl<>(assignments.subList(start, end), pageable, assignments.size());

        return ResponseEntity.ok(page);
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
