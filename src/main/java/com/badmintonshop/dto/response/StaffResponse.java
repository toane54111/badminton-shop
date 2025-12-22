package com.badmintonshop.dto.response;

import com.badmintonshop.entity.Staff;
import com.badmintonshop.entity.enums.StaffRole;
import com.badmintonshop.entity.enums.StaffStatus;
import com.badmintonshop.entity.enums.StringingSkillLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Staff
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {

    private Long staffId;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private StaffRole role;
    private StaffStatus status;
    
    // For STRINGING_STAFF
    private StringingSkillLevel stringingSkillLevel;
    private Integer dailyStringingCapacity;
    
    // Permissions
    private List<String> permissions;
    
    // Timestamps
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert from entity to response DTO (without permissions)
     */
    public static StaffResponse fromEntity(Staff entity) {
        return StaffResponse.builder()
                .staffId(entity.getStaffId())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .phone(entity.getPhone())
                .avatarUrl(entity.getAvatarUrl())
                .role(entity.getRole())
                .status(entity.getStatus())
                .stringingSkillLevel(entity.getStringingSkillLevel())
                .dailyStringingCapacity(entity.getDailyStringingCapacity())
                .lastLoginAt(entity.getLastLoginAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Convert from entity to response DTO (with permissions)
     */
    public static StaffResponse fromEntityWithPermissions(Staff entity, List<String> permissions) {
        StaffResponse response = fromEntity(entity);
        response.setPermissions(permissions);
        return response;
    }
}
