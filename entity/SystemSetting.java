package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.SettingType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity SystemSetting - Cài đặt hệ thống
 */
@Entity
@Table(name = "system_settings", indexes = {
    @Index(name = "idx_settings_public", columnList = "is_public")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long settingId;

    @Column(name = "setting_key", nullable = false, unique = true, length = 100)
    private String settingKey;

    @Column(name = "setting_value", nullable = false, columnDefinition = "TEXT")
    private String settingValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "setting_type")
    @Builder.Default
    private SettingType settingType = SettingType.STRING;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false; // Có thể public ra API không

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private Staff updatedByStaff;

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public Integer getIntValue() {
        try {
            return Integer.parseInt(settingValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Boolean getBooleanValue() {
        return "true".equalsIgnoreCase(settingValue) || "1".equals(settingValue);
    }

    public Double getDoubleValue() {
        try {
            return Double.parseDouble(settingValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
