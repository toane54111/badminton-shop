package com.badmintonshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity StaffPermission - Phân quyền chi tiết cho staff
 */
@Entity
@Table(name = "staff_permissions", indexes = {
    @Index(name = "idx_staff_permissions_staff", columnList = "staff_id"),
    @Index(name = "idx_staff_permissions_key", columnList = "permission_key")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_staff_permission", columnNames = {"staff_id", "permission_key"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long permissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "permission_key", nullable = false, length = 100)
    private String permissionKey; // VD: products.create, orders.view, orders.edit

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
