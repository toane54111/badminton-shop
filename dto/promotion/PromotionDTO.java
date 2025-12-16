package com.badmintonshop.dto.promotion;

import com.badmintonshop.entity.enums.DiscountType;
import com.badmintonshop.entity.enums.PromotionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Promotion (public API)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDTO {
    private Long promotionId;
    private String name;
    private String description;
    private PromotionType type;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private String applicableProducts;
    private String applicableCategories;
    private String bundleProducts;
    private BigDecimal bundlePrice;
    private Integer buyQuantity;
    private Integer getQuantity;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Boolean isActive;

    // Computed fields for display
    private String typeText;
    private String discountText;
    private Long remainingTimeSeconds;
}
