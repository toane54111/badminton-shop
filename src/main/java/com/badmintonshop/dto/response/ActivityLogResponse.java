package com.badmintonshop.dto.response;

import com.badmintonshop.entity.StaffActivityLog;
import com.badmintonshop.entity.enums.ActivityAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for StaffActivityLog
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {

    private Long logId;
    private Long staffId;
    private String staffEmail;
    private String staffName;
    private ActivityAction action;
    private String entityType;
    private Long entityId;
    private String description;
    private String oldValues; // JSON
    private String newValues; // JSON
    private String ipAddress;
    private LocalDateTime createdAt;

    /**
     * Convert from entity to response DTO
     */
    public static ActivityLogResponse fromEntity(StaffActivityLog entity) {
        return ActivityLogResponse.builder()
                .logId(entity.getLogId())
                .staffId(entity.getStaff() != null ? entity.getStaff().getStaffId() : null)
                .staffEmail(entity.getStaff() != null ? entity.getStaff().getEmail() : null)
                .staffName(entity.getStaff() != null ? entity.getStaff().getFullName() : null)
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .description(entity.getDescription())
                .oldValues(entity.getOldValues())
                .newValues(entity.getNewValues())
                .ipAddress(entity.getIpAddress())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
