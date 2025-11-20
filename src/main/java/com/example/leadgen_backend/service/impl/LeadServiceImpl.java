package com.example.leadgen_backend.service.impl;

import com.example.leadgen_backend.model.Lead;
import com.example.leadgen_backend.repository.BusinessProfileRepository;
import com.example.leadgen_backend.repository.LeadAssignmentRepository;
import com.example.leadgen_backend.repository.LeadRepository;
import com.example.leadgen_backend.service.LeadService;
import com.example.leadgen_backend.service.ScoringService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final LeadAssignmentRepository assignmentRepository;
    private final ScoringService scoringService;

    public LeadServiceImpl(LeadRepository leadRepository,
                           BusinessProfileRepository businessProfileRepository,
                           LeadAssignmentRepository assignmentRepository,
                           ScoringService scoringService) {
        this.leadRepository = leadRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.assignmentRepository = assignmentRepository;
        this.scoringService = scoringService;
    }

    @Override
    @Transactional
    public Lead createLead(Lead lead) {
        Lead saved = leadRepository.save(lead);
        // compute matches async ideally. here we call sync for skeleton
        computeMatchesAndCreateAssignments(saved);
        return saved;
    }

    @Override
    public void computeMatchesAndCreateAssignments(Lead lead) {
        // TODO: load nearby businesses, compute score using scoringService,
        // persist top N LeadAssignment rows with expiresAt, rank and status
    }
}

