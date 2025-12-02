package com.example.leadgen_backend.service;

public interface LeadAssignmentService {
    void claimLead(Long assignmentId, Long businessId);
}

