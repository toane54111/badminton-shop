package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.StringingServiceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;

/**
 * Entity StringingService - Các loại dịch vụ đan vợt
 */
@Entity
@Table(name = "stringing_services", indexes = {
    @Index(name = "idx_stringing_type", columnList = "service_type"),
    @Index(name = "idx_stringing_active", columnList = "is_active"),
    @Index(name = "idx_stringing_deleted", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
public class StringingService extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "service_name", nullable = false)
    private String serviceName; // VD: Đan vợt thường, Đan vợt 4 điểm

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    @Builder.Default
    private StringingServiceType serviceType = StringingServiceType.STANDARD;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice; // Giá công đan

    @Column(name = "estimated_time_minutes")
    @Builder.Default
    private Integer estimatedTimeMinutes = 30; // Thời gian ước tính

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
