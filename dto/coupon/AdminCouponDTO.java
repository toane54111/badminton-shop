package com.badmintonshop.dto.coupon;

import com.badmintonshop.entity.enums.ApplicableTo;
import com.badmintonshop.entity.enums.CouponType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Admin Coupon Management
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCouponDTO {
    private Long couponId;
    private String code;
    private String name;
    private String description;
    private CouponType type;
    private BigDecimal value;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountAmount;
    private ApplicableTo applicableTo;
    private String applicableProductIds;
    private String applicableCategoryIds;
    private String applicableBrandIds;
    private Integer usageLimit;
    private Integer usagePerUser;
    private Integer timesUsed;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper for display
    private String statusText;
    private String typeText;
}
