package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.ActivityAction;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity StaffActivityLog - Audit Trail - nhật ký hoạt động nhân viên
 */
@Entity
@Table(name = "staff_activity_logs", indexes = {
    @Index(name = "idx_activity_logs_staff", columnList = "staff_id"),
    @Index(name = "idx_activity_logs_action", columnList = "action"),
    @Index(name = "idx_activity_logs_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_activity_logs_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private ActivityAction action;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // product/order/user/coupon...

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "old_values", columnDefinition = "JSON")
    private String oldValues; // Old data before update

    @Column(name = "new_values", columnDefinition = "JSON")
    private String newValues; // New data after update

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
