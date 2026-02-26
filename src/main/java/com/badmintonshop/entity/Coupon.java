package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.ApplicableTo;
import com.badmintonshop.entity.enums.CouponType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity Coupon - Mã giảm giá
 */
@Entity
@Table(name = "coupons", indexes = {
    @Index(name = "idx_coupons_type", columnList = "type"),
    @Index(name = "idx_coupons_active", columnList = "is_active"),
    @Index(name = "idx_coupons_starts", columnList = "starts_at"),
    @Index(name = "idx_coupons_expires", columnList = "expires_at"),
    @Index(name = "idx_coupons_deleted", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CouponType type;

    @Column(name = "value", nullable = false, precision = 12, scale = 2)
    private BigDecimal value; // Percentage (10) or Fixed amount (50000)

    // Conditions
    @Column(name = "min_order_value", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount; // Giảm tối đa (cho %)

    // Applicable Products
    @Enumerated(EnumType.STRING)
    @Column(name = "applicable_to")
    @Builder.Default
    private ApplicableTo applicableTo = ApplicableTo.ALL;

    @Column(name = "applicable_product_ids", columnDefinition = "JSON")
    private String applicableProductIds; // Array of product IDs

    @Column(name = "applicable_category_ids", columnDefinition = "JSON")
    private String applicableCategoryIds; // Array of category IDs

    @Column(name = "applicable_brand_ids", columnDefinition = "JSON")
    private String applicableBrandIds; // Array of brand IDs

    // Usage Limits
    @Column(name = "usage_limit")
    private Integer usageLimit; // Tổng số lần dùng được

    @Column(name = "usage_per_user")
    @Builder.Default
    private Integer usagePerUser = 1;

    @Column(name = "times_used")
    @Builder.Default
    private Integer timesUsed = 0;

    // Validity
    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Helper methods
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && 
               now.isAfter(startsAt) && 
               now.isBefore(expiresAt) &&
               (usageLimit == null || timesUsed < usageLimit);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public BigDecimal calculateDiscount(BigDecimal orderTotal) {
        if (orderTotal.compareTo(minOrderValue) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        switch (type) {
            case PERCENTAGE:
                discount = orderTotal.multiply(value).divide(BigDecimal.valueOf(100));
                if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
                    discount = maxDiscountAmount;
                }
                break;
            case FIXED_AMOUNT:
                discount = value;
                break;
            case FREE_SHIPPING:
                // Free shipping is handled separately
                discount = BigDecimal.ZERO;
                break;
            default:
                discount = BigDecimal.ZERO;
        }
        return discount;
    }

    public void incrementUsage() {
        this.timesUsed++;
    }
}
