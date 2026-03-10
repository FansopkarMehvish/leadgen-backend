package com.example.leadgen_backend.service;

import com.example.leadgen_backend.enums.LeadStatus;
import com.example.leadgen_backend.model.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LeadService {
    Lead createLead(Lead lead);
    Lead getLeadById(Long id);
    List<Lead> getAllLeads();
    Page<Lead> getAllLeadsPaged(Pageable pageable);
    List<Lead> getLeadsByStatus(LeadStatus status);
    Page<Lead> getLeadsByStatus(LeadStatus status, Pageable pageable);
    List<Lead> getLeadsByCategory(Long categoryId);
    List<Lead> getLeadsByStatusAndCategory(LeadStatus status, Long categoryId);
    Lead updateLead(Long id, Lead lead);
    void deleteLead(Long id);
    void computeMatchesAndCreateAssignments(Lead lead);
}
