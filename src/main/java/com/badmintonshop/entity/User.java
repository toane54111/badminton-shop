package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.ColumnTransformer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity User - Người dùng
 * Có playing profile để hệ thống tư vấn vợt
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_phone", columnList = "phone"),
    @Index(name = "idx_users_status", columnList = "status"),
    @Index(name = "idx_users_created_at", columnList = "created_at"),
    @Index(name = "idx_users_deleted_at", columnList = "deleted_at"),
    @Index(name = "idx_users_email_deleted", columnList = "email, deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

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

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    @ColumnTransformer(read = "UPPER(gender)")
    private Gender gender;

    // OAuth Integration
    @Column(name = "google_oauth_id", unique = true)
    private String googleOauthId;

    // Email Verification
    @Column(name = "is_email_verified")
    @Builder.Default
    private Boolean isEmailVerified = false;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    // Playing Profile (Đặc thù cầu lông - dùng cho tư vấn)
    @Enumerated(EnumType.STRING)
    @Column(name = "playing_style")
    @ColumnTransformer(read = "UPPER(playing_style)")
    private PlayingStyle playingStyle;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level")
    @ColumnTransformer(read = "UPPER(skill_level)")
    private SkillLevel skillLevel;

    @Column(name = "preferred_racket_weight", length = 10)
    private String preferredRacketWeight; // 3U/4U/5U

    @Column(name = "preferred_tension_min", precision = 4, scale = 1)
    private BigDecimal preferredTensionMin; // Min tension in lbs

    @Column(name = "preferred_tension_max", precision = 4, scale = 1)
    private BigDecimal preferredTensionMax; // Max tension in lbs

    @Column(name = "preferred_string_type", length = 100)
    private String preferredStringType; // Loại cước ưa dùng

    // Account Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserSession> sessions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PasswordResetToken> passwordResetTokens = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EmailVerification> emailVerifications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Cart> carts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Wishlist> wishlists = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductReview> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    // Helper methods
    public void addAddress(UserAddress address) {
        addresses.add(address);
        address.setUser(this);
    }

    public void removeAddress(UserAddress address) {
        addresses.remove(address);
        address.setUser(null);
    }

    public UserAddress getDefaultAddress() {
        return addresses.stream()
            .filter(UserAddress::getIsDefault)
            .findFirst()
            .orElse(null);
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public boolean isBanned() {
        return status == UserStatus.BANNED;
    }

    public boolean isLocked() {
        return status == UserStatus.LOCKED;
    }
}
