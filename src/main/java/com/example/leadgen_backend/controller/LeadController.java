package com.example.leadgen_backend.controller;

import com.example.leadgen_backend.model.Lead;
import com.example.leadgen_backend.service.LeadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @PostMapping
    public ResponseEntity<?> createLead(@RequestBody Lead lead) {
        Lead saved = leadService.createLead(lead);
        return ResponseEntity.status(201).body(saved);
    }
}
