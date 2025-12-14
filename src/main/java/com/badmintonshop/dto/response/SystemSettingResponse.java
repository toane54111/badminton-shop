package com.badmintonshop.dto.response;

import com.badmintonshop.entity.SystemSetting;
import com.badmintonshop.entity.enums.SettingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for SystemSetting
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingResponse {

    private Long settingId;
    private String settingKey;
    private String settingValue;
    private SettingType settingType;
    private String description;
    private Boolean isPublic;
    private String updatedBy; // Staff email or name
    private LocalDateTime updatedAt;

    /**
     * Convert from entity to response DTO
     */
    public static SystemSettingResponse fromEntity(SystemSetting entity) {
        return SystemSettingResponse.builder()
                .settingId(entity.getSettingId())
                .settingKey(entity.getSettingKey())
                .settingValue(entity.getSettingValue())
                .settingType(entity.getSettingType())
                .description(entity.getDescription())
                .isPublic(entity.getIsPublic())
                .updatedBy(entity.getUpdatedByStaff() != null ? 
                        entity.getUpdatedByStaff().getFullName() : null)
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
