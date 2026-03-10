package com.example.leadgen_backend.service;

import com.example.leadgen_backend.model.AuditLog;
import com.example.leadgen_backend.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void log(String entityType, Long entityId, String action, Object oldValues, Object newValues) {
        AuditLog auditLog = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .performedBy(getCurrentUserEmail())
                .performedByUserId(getCurrentUserId())
                .oldValues(oldValues != null ? objectMapper.writeValueAsString(oldValues) : null)
                .newValues(newValues != null ? objectMapper.writeValueAsString(newValues) : null)
                .ipAddress(getClientIpAddress())
                .createdAt(Instant.now())
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Audit log saved: {} {} for {}", action, entityType, entityId);
    }

    public void logCreate(String entityType, Long entityId, Object newValues) {
        log(entityType, entityId, "CREATE", null, newValues);
    }

    public void logUpdate(String entityType, Long entityId, Object oldValues, Object newValues) {
        log(entityType, entityId, "UPDATE", oldValues, newValues);
    }

    public void logDelete(String entityType, Long entityId, Object oldValues) {
        log(entityType, entityId, "DELETE", oldValues, null);
    }

    public void logClaim(Long leadId, Long businessId) {
        AuditLog auditLog = AuditLog.builder()
                .entityType("LeadAssignment")
                .entityId(leadId)
                .action("CLAIM")
                .performedBy(getCurrentUserEmail())
                .performedByUserId(getCurrentUserId())
                .newValues("BusinessId: " + businessId)
                .ipAddress(getClientIpAddress())
                .createdAt(Instant.now())
                .build();

        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAuditLogsForEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public List<AuditLog> getAuditLogsForUser(String email) {
        return auditLogRepository.findByPerformedBy(email);
    }

    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }

    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    public List<AuditLog> getAuditLogsByDateRange(Instant start, Instant end) {
        return auditLogRepository.findByCreatedAtBetween(start, end);
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "SYSTEM";
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getDetails() instanceof Long) {
            return (Long) authentication.getDetails();
        }
        return null;
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not get IP address", e);
        }
        return "unknown";
    }
}
