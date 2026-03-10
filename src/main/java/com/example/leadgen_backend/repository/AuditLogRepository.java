package com.example.leadgen_backend.repository;

import com.example.leadgen_backend.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<AuditLog> findByPerformedBy(String performedBy);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByCreatedAtBetween(Instant start, Instant end);

    Page<AuditLog> findAll(Pageable pageable);
}
