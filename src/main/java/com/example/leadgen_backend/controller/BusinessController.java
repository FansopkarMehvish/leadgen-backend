package com.example.leadgen_backend.controller;

import com.example.leadgen_backend.model.LeadAssignment;
import com.example.leadgen_backend.repository.LeadAssignmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final LeadAssignmentRepository assignmentRepository;

    public BusinessController(LeadAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @GetMapping("/leads")
    public ResponseEntity<?> getLeadsForBusiness(@RequestParam Long businessId) {
        List<LeadAssignment> assignments = assignmentRepository.findByBusinessIdAndIsClaimedFalseOrderByScoreDesc(businessId);
        return ResponseEntity.ok(assignments);
    }

    @PostMapping("/claim/{assignmentId}")
    public ResponseEntity<?> claimLead(@PathVariable Long assignmentId) {
        // implement optimistic claim with repository update
        return ResponseEntity.ok().body("claimed");
    }
}
