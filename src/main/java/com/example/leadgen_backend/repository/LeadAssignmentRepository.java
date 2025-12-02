package com.example.leadgen_backend.repository;

import com.example.leadgen_backend.model.LeadAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadAssignmentRepository extends JpaRepository<LeadAssignment, Long> {

    List<LeadAssignment> findByBusinessIdAndIsClaimedFalseOrderByScoreDesc(Long businessId);

    List<LeadAssignment> findByLeadId(Long leadId);
}