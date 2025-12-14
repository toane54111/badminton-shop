package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.DiscountType;
import com.badmintonshop.entity.enums.PromotionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity Promotion - Chương trình khuyến mãi
 */
@Entity
@Table(name = "promotions", indexes = {
    @Index(name = "idx_promotions_type", columnList = "type"),
    @Index(name = "idx_promotions_active", columnList = "is_active"),
    @Index(name = "idx_promotions_starts", columnList = "starts_at"),
    @Index(name = "idx_promotions_ends", columnList = "ends_at"),
    @Index(name = "idx_promotions_deleted", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
public class Promotion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Long promotionId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PromotionType type;

    // Discount Config
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType;

    @Column(name = "discount_value", precision = 12, scale = 2)
    private BigDecimal discountValue;

    // Applicable Products
    @Column(name = "applicable_products", columnDefinition = "JSON")
    private String applicableProducts; // Array of product IDs

    @Column(name = "applicable_categories", columnDefinition = "JSON")
    private String applicableCategories; // Array of category IDs

    // Bundle Config
    @Column(name = "bundle_products", columnDefinition = "JSON")
    private String bundleProducts; // Array: [{product_id, quantity}]

    @Column(name = "bundle_price", precision = 12, scale = 2)
    private BigDecimal bundlePrice;

    // Buy X Get Y Config
    @Column(name = "buy_quantity")
    private Integer buyQuantity;

    @Column(name = "get_quantity")
    private Integer getQuantity;

    // Validity
    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Helper methods
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && now.isAfter(startsAt) && now.isBefore(endsAt);
    }

    public boolean isFlashSale() {
        return type == PromotionType.FLASH_SALE;
    }

    public boolean isBundle() {
        return type == PromotionType.BUNDLE;
    }
}
