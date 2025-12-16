package com.badmintonshop.dto.coupon;

import com.badmintonshop.entity.enums.CouponType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CouponDTO {
    private String code;
    private String name;
    private String description;
    private CouponType type;
    private BigDecimal value;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountAmount;
    private LocalDateTime expiresAt;
}
