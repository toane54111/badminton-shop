package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.BannerPosition;
import com.badmintonshop.entity.enums.LinkTarget;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

/**
 * Entity Banner - Banner quảng cáo
 */
@Entity
@Table(name = "banners", indexes = {
    @Index(name = "idx_banners_position", columnList = "position"),
    @Index(name = "idx_banners_active", columnList = "is_active"),
    @Index(name = "idx_banners_order", columnList = "display_order"),
    @Index(name = "idx_banners_starts", columnList = "starts_at"),
    @Index(name = "idx_banners_ends", columnList = "ends_at"),
    @Index(name = "idx_banners_deleted", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
public class Banner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_id")
    private Long bannerId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "mobile_image_url", length = 500)
    private String mobileImageUrl;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "link_target")
    @Builder.Default
    private LinkTarget linkTarget = LinkTarget.SELF;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false)
    private BannerPosition position;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Staff createdByStaff;

    // Helper methods
    public boolean isDisplayable() {
        LocalDateTime now = LocalDateTime.now();
        boolean withinTime = true;
        
        if (startsAt != null && now.isBefore(startsAt)) {
            withinTime = false;
        }
        if (endsAt != null && now.isAfter(endsAt)) {
            withinTime = false;
        }
        
        return isActive && withinTime;
    }
}
