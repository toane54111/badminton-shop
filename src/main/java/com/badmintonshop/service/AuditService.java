package com.badmintonshop.service;

import com.badmintonshop.dto.response.ActivityLogResponse;
import com.badmintonshop.entity.Staff;
import com.badmintonshop.entity.StaffActivityLog;
import com.badmintonshop.entity.enums.ActivityAction;
import com.badmintonshop.repository.StaffActivityLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for staff activity logging (Audit Trail)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuditService {

    private final StaffActivityLogRepository logRepository;
    private final ObjectMapper objectMapper;

    /**
     * Log a staff activity
     */
    @Transactional
    @Async("emailExecutor") // Reuse async executor
    public void logActivity(Staff staff, 
                           ActivityAction action, 
                           String entityType,
                           Long entityId, 
                           String description,
                           Object oldValues, 
                           Object newValues,
                           HttpServletRequest request) {
        try {
            StaffActivityLog activityLog = StaffActivityLog.builder()
                    .staff(staff)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .oldValues(toJson(oldValues))
                    .newValues(toJson(newValues))
                    .ipAddress(getClientIp(request))
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .createdAt(LocalDateTime.now())
                    .build();

            logRepository.save(activityLog);
            log.debug("Logged activity: {} {} {} by {}", action, entityType, entityId, 
                    staff != null ? staff.getEmail() : "unknown");
        } catch (Exception e) {
            log.error("Failed to log activity: {} {} {}", action, entityType, entityId, e);
        }
    }

    /**
     * Log activity (synchronous version)
     */
    @Transactional
    public void logActivitySync(Staff staff, 
                                ActivityAction action, 
                                String entityType,
                                Long entityId, 
                                String description) {
        StaffActivityLog activityLog = StaffActivityLog.builder()
                .staff(staff)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        logRepository.save(activityLog);
    }

    /**
     * Get activity logs with filters
     */
    public Page<ActivityLogResponse> getLogs(Long staffId,
                                              String entityType,
                                              ActivityAction action,
                                              LocalDateTime fromDate,
                                              LocalDateTime toDate,
                                              Pageable pageable) {
        log.debug("Fetching activity logs - staffId: {}, entityType: {}, action: {}", 
                staffId, entityType, action);
        
        return logRepository.findWithFilters(staffId, entityType, action, fromDate, toDate, pageable)
                .map(ActivityLogResponse::fromEntity);
    }

    /**
     * Get logs by staff
     */
    public Page<ActivityLogResponse> getLogsByStaff(Long staffId, Pageable pageable) {
        return logRepository.findByStaffStaffIdOrderByCreatedAtDesc(staffId, pageable)
                .map(ActivityLogResponse::fromEntity);
    }

    /**
     * Get logs by entity
     */
    public Page<ActivityLogResponse> getLogsByEntity(String entityType, Pageable pageable) {
        return logRepository.findByEntityTypeOrderByCreatedAtDesc(entityType, pageable)
                .map(ActivityLogResponse::fromEntity);
    }

    /**
     * Get logs for specific entity instance
     */
    public java.util.List<ActivityLogResponse> getLogsForEntity(String entityType, Long entityId) {
        return logRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream()
                .map(ActivityLogResponse::fromEntity)
                .toList();
    }

    /**
     * Get recent logs (last N activities)
     */
    public Page<ActivityLogResponse> getRecentLogs(Pageable pageable) {
        return logRepository.findAll(pageable)
                .map(ActivityLogResponse::fromEntity);
    }

    /**
     * Count activities by staff in date range
     */
    public long countByStaffInDateRange(Long staffId, LocalDateTime from, LocalDateTime to) {
        return logRepository.countByStaffInDateRange(staffId, from, to);
    }

    /**
     * Helper: Convert object to JSON string
     */
    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to JSON: {}", e.getMessage());
            return obj.toString();
        }
    }

    /**
     * Helper: Get client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // In case of multiple IPs (behind proxies), get the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
