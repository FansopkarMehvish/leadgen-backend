package com.example.leadgen_backend.controller;

import com.example.leadgen_backend.enums.LeadStatus;
import com.example.leadgen_backend.model.Lead;
import com.example.leadgen_backend.service.LeadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<?> createLead(@RequestBody Lead lead) {
        Lead saved = leadService.createLead(lead);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS','CUSTOMER')")
    public ResponseEntity<?> getAllLeads(Pageable pageable) {
        Page<Lead> leads = leadService.getAllLeadsPaged(pageable);
        return ResponseEntity.ok(leads);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS','CUSTOMER')")
    public ResponseEntity<?> getLeadById(@PathVariable Long id) {
        Lead lead = leadService.getLeadById(id);
        return ResponseEntity.ok(lead);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS')")
    public ResponseEntity<?> getLeadsByStatus(@PathVariable LeadStatus status, Pageable pageable) {
        Page<Lead> leads = leadService.getLeadsByStatus(status, pageable);
        return ResponseEntity.ok(leads);
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS')")
    public ResponseEntity<?> getLeadsByCategory(@PathVariable Long categoryId) {
        List<Lead> leads = leadService.getLeadsByCategory(categoryId);
        return ResponseEntity.ok(leads);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS')")
    public ResponseEntity<?> getLeadsByStatusAndCategory(
            @RequestParam LeadStatus status,
            @RequestParam Long categoryId) {
        List<Lead> leads = leadService.getLeadsByStatusAndCategory(status, categoryId);
        return ResponseEntity.ok(leads);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<?> updateLead(@PathVariable Long id, @RequestBody Lead lead) {
        Lead updated = leadService.updateLead(id, lead);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<?> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok("Lead deleted successfully");
    }
}
