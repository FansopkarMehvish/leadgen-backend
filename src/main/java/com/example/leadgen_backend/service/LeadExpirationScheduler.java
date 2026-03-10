package com.example.leadgen_backend.service;

import com.example.leadgen_backend.enums.AssignmentStatus;
import com.example.leadgen_backend.model.LeadAssignment;
import com.example.leadgen_backend.repository.LeadAssignmentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeadExpirationScheduler {

    private final LeadAssignmentRepository assignmentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Runs every minute to check and expire assignments that have passed their expiration time.
     * This handles cases where businesses didn't claim within the 30-minute window.
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    @Transactional
    public void expireOldAssignments() {
        Instant now = Instant.now();

        // Find all unclaimed, notified assignments that have expired
        List<LeadAssignment> expiredAssignments = entityManager
                .createQuery(
                        "SELECT a FROM LeadAssignment a " +
                                "WHERE a.isClaimed = false " +
                                "AND a.status = :status " +
                                "AND a.expiresAt < :now", LeadAssignment.class)
                .setParameter("status", AssignmentStatus.NOTIFIED)
                .setParameter("now", now)
                .getResultList();

        if (expiredAssignments.isEmpty()) {
            return;
        }

        log.info("Found {} expired lead assignments to process", expiredAssignments.size());

        for (LeadAssignment assignment : expiredAssignments) {
            assignment.setStatus(AssignmentStatus.EXPIRED);
            assignmentRepository.save(assignment);
            log.debug("Expired assignment id={} for leadId={} and businessId={}",
                    assignment.getId(), assignment.getLeadId(), assignment.getBusinessId());
        }

        log.info("Processed {} expired assignments", expiredAssignments.size());
    }

    /**
     * Optional: Runs daily to clean up very old expired assignments (older than 30 days)
     * This helps keep the database size manageable.
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    @Transactional
    public void cleanupOldExpiredAssignments() {
        Instant thirtyDaysAgo = Instant.now().minusSeconds(30L * 24 * 60 * 60);

        List<LeadAssignment> oldAssignments = entityManager
                .createQuery(
                        "SELECT a FROM LeadAssignment a " +
                                "WHERE a.status = :status " +
                                "AND a.expiresAt < :cutoff", LeadAssignment.class)
                .setParameter("status", AssignmentStatus.EXPIRED)
                .setParameter("cutoff", thirtyDaysAgo)
                .getResultList();

        if (!oldAssignments.isEmpty()) {
            log.info("Cleaning up {} expired assignments older than 30 days", oldAssignments.size());
            assignmentRepository.deleteAll(oldAssignments);
        }
    }
}
