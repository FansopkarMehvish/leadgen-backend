package com.example.leadgen_backend.service;

import com.example.leadgen_backend.model.Lead;

public interface LeadService {
    Lead createLead(Lead lead);
    void computeMatchesAndCreateAssignments(Lead lead);
}
