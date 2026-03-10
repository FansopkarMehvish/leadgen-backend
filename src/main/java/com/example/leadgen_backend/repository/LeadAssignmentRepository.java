package com.example.leadgen_backend.repository;

import com.example.leadgen_backend.enums.AssignmentStatus;
import com.example.leadgen_backend.model.LeadAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface LeadAssignmentRepository extends JpaRepository<LeadAssignment, Long> {

    List<LeadAssignment> findByBusinessIdAndIsClaimedFalseOrderByScoreDesc(Long businessId);

    List<LeadAssignment> findByLeadId(Long leadId);

    List<LeadAssignment> findByIsClaimedFalseAndStatusAndExpiresAtBefore(
            AssignmentStatus status, Instant now);
}
