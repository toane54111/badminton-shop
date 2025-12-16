package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.StaffRole;
import com.badmintonshop.entity.enums.StaffStatus;
import com.badmintonshop.entity.enums.StringingSkillLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity Staff - Nhân viên - có stringing staff (thợ đan vợt)
 */
@Entity
@Table(name = "staff", indexes = {
    @Index(name = "idx_staff_role", columnList = "role"),
    @Index(name = "idx_staff_status", columnList = "status"),
    @Index(name = "idx_staff_deleted", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
public class Staff extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private StaffRole role;

    // Stringing Staff specific
    @Enumerated(EnumType.STRING)
    @Column(name = "stringing_skill_level")
    private StringingSkillLevel stringingSkillLevel; // Cho stringing staff

    @Column(name = "daily_stringing_capacity")
    private Integer dailyStringingCapacity; // Số vợt đan được trong ngày

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private StaffStatus status = StaffStatus.ACTIVE;

    @Column(name = "resigned_at")
    private LocalDateTime resignedAt; // Ghi lại ngày nghỉ việc

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Relationships
    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StaffPermission> permissions = new ArrayList<>();

    @OneToMany(mappedBy = "createdByStaff")
    @Builder.Default
    private List<Product> createdProducts = new ArrayList<>();

    @OneToMany(mappedBy = "updatedByStaff")
    @Builder.Default
    private List<Product> updatedProducts = new ArrayList<>();

    @OneToMany(mappedBy = "assignedStaff")
    @Builder.Default
    private List<OrderItem> assignedStringingOrders = new ArrayList<>();

    // Helper methods
    public boolean isActive() {
        return status == StaffStatus.ACTIVE;
    }

    public boolean isStringingStaff() {
        return role == StaffRole.STRINGING_STAFF;
    }

    public boolean hasPermission(String permissionKey) {
        return permissions.stream()
            .anyMatch(p -> p.getPermissionKey().equals(permissionKey));
    }

    public void addPermission(StaffPermission permission) {
        permissions.add(permission);
        permission.setStaff(this);
    }

    public void removePermission(StaffPermission permission) {
        permissions.remove(permission);
        permission.setStaff(null);
    }
}
