package com.badmintonshop.service;

import com.badmintonshop.entity.Promotion;
import com.badmintonshop.entity.enums.DiscountType;
import com.badmintonshop.entity.enums.PromotionType;
import com.badmintonshop.repository.PromotionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service for calculating promotion prices
 * Calculates discounted prices without modifying database values
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionPriceService {

    private final PromotionRepository promotionRepository;
    private final ObjectMapper objectMapper;

    /**
     * Get active promotions applicable to a product
     */
    @Transactional(readOnly = true)
    public List<Promotion> getActivePromotionsForProduct(Long productId, Long categoryId) {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> activePromotions = promotionRepository.findActivePromotions(now);
        
        return activePromotions.stream()
                .filter(p -> isPromotionApplicable(p, productId, categoryId))
                .toList();
    }

    /**
     * Check if a promotion is applicable to a specific product
     */
    private boolean isPromotionApplicable(Promotion promotion, Long productId, Long categoryId) {
        // Only DISCOUNT and FLASH_SALE types apply to product prices
        if (promotion.getType() != PromotionType.DISCOUNT && 
            promotion.getType() != PromotionType.FLASH_SALE) {
            return false;
        }

        // Check if promotion has no restrictions (applies to all)
        boolean hasProductRestriction = promotion.getApplicableProducts() != null && 
                                        !promotion.getApplicableProducts().isBlank();
        boolean hasCategoryRestriction = promotion.getApplicableCategories() != null && 
                                         !promotion.getApplicableCategories().isBlank();

        // If no restrictions, promotion applies to all products
        if (!hasProductRestriction && !hasCategoryRestriction) {
            return true;
        }

        // Check product restriction
        if (hasProductRestriction && productId != null) {
            List<Long> applicableProductIds = parseJsonArray(promotion.getApplicableProducts());
            if (applicableProductIds.contains(productId)) {
                return true;
            }
        }

        // Check category restriction
        if (hasCategoryRestriction && categoryId != null) {
            List<Long> applicableCategoryIds = parseJsonArray(promotion.getApplicableCategories());
            if (applicableCategoryIds.contains(categoryId)) {
                return true;
            }
        }

        // Has restrictions but product doesn't match
        return false;
    }

    /**
     * Parse JSON array string to list of Long IDs
     */
    private List<Long> parseJsonArray(String jsonArray) {
        if (jsonArray == null || jsonArray.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(jsonArray, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON array: {}", jsonArray, e);
            return Collections.emptyList();
        }
    }

    /**
     * Calculate the promotional price for a product
     * Returns the discounted price or original price if no promotion applies
     */
    @Transactional(readOnly = true)
    public BigDecimal calculatePromotionPrice(BigDecimal originalPrice, Long productId, Long categoryId) {
        if (originalPrice == null) {
            return null;
        }

        Optional<Promotion> bestPromotion = getBestPromotion(productId, categoryId);
        if (bestPromotion.isEmpty()) {
            return originalPrice;
        }

        Promotion promotion = bestPromotion.get();
        BigDecimal discount = calculateDiscountAmount(originalPrice, promotion);
        return originalPrice.subtract(discount).max(BigDecimal.ZERO);
    }

    /**
     * Calculate the discount amount for a product
     */
    @Transactional(readOnly = true)
    public BigDecimal calculatePromotionDiscount(BigDecimal originalPrice, Long productId, Long categoryId) {
        if (originalPrice == null) {
            return BigDecimal.ZERO;
        }

        Optional<Promotion> bestPromotion = getBestPromotion(productId, categoryId);
        if (bestPromotion.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return calculateDiscountAmount(originalPrice, bestPromotion.get());
    }

    /**
     * Get the best applicable promotion (highest discount)
     */
    private Optional<Promotion> getBestPromotion(Long productId, Long categoryId) {
        List<Promotion> applicablePromotions = getActivePromotionsForProduct(productId, categoryId);
        
        // Return the promotion with highest discount value (prioritize percentage)
        return applicablePromotions.stream()
                .max((p1, p2) -> {
                    BigDecimal v1 = p1.getDiscountValue() != null ? p1.getDiscountValue() : BigDecimal.ZERO;
                    BigDecimal v2 = p2.getDiscountValue() != null ? p2.getDiscountValue() : BigDecimal.ZERO;
                    return v1.compareTo(v2);
                });
    }

    /**
     * Calculate discount amount based on promotion type
     */
    private BigDecimal calculateDiscountAmount(BigDecimal originalPrice, Promotion promotion) {
        if (promotion.getDiscountValue() == null || promotion.getDiscountType() == null) {
            return BigDecimal.ZERO;
        }

        if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
            // Percentage discount
            return originalPrice.multiply(promotion.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        } else {
            // Fixed amount discount
            return promotion.getDiscountValue().min(originalPrice);
        }
    }

    /**
     * Get information about the applied promotion
     */
    @Transactional(readOnly = true)
    public PromotionInfo getAppliedPromotionInfo(Long productId, Long categoryId) {
        Optional<Promotion> bestPromotion = getBestPromotion(productId, categoryId);
        
        if (bestPromotion.isEmpty()) {
            return null;
        }

        Promotion promotion = bestPromotion.get();
        String discountText = formatDiscountText(promotion);
        
        return new PromotionInfo(
                promotion.getPromotionId(),
                promotion.getName(),
                discountText,
                promotion.getDiscountType(),
                promotion.getDiscountValue()
        );
    }

    /**
     * Format discount text for display (e.g., "-10%" or "-50,000₫")
     */
    private String formatDiscountText(Promotion promotion) {
        if (promotion.getDiscountValue() == null || promotion.getDiscountType() == null) {
            return "";
        }

        if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
            return "-" + promotion.getDiscountValue().stripTrailingZeros().toPlainString() + "%";
        } else {
            return "-" + String.format("%,d₫", promotion.getDiscountValue().longValue());
        }
    }

    /**
     * Check if there is any active promotion for a product
     */
    @Transactional(readOnly = true)
    public boolean hasActivePromotion(Long productId, Long categoryId) {
        return getBestPromotion(productId, categoryId).isPresent();
    }

    /**
     * DTO class for promotion information
     */
    public record PromotionInfo(
            Long promotionId,
            String name,
            String discountText,
            DiscountType discountType,
            BigDecimal discountValue
    ) {}
}
