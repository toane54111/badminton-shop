package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.ProductStatus;
import com.badmintonshop.entity.enums.ProductType;
import com.badmintonshop.entity.enums.RacketFlexibility;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity Product - Sản phẩm chính - có fields riêng cho vợt cầu lông
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_products_category", columnList = "category_id"),
    @Index(name = "idx_products_brand", columnList = "brand_id"),
    @Index(name = "idx_products_type", columnList = "product_type"),
    @Index(name = "idx_products_featured", columnList = "is_featured"),
    @Index(name = "idx_products_new_arrival", columnList = "is_new_arrival"),
    @Index(name = "idx_products_best_seller", columnList = "is_best_seller"),
    @Index(name = "idx_products_status", columnList = "status"),
    @Index(name = "idx_products_created", columnList = "created_at"),
    @Index(name = "idx_products_rating", columnList = "rating_average"),
    @Index(name = "idx_products_sold", columnList = "sold_count"),
    @Index(name = "idx_products_deleted", columnList = "deleted_at"),
    @Index(name = "idx_products_status_deleted_order", columnList = "status, deleted_at, display_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;

    // Pricing
    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "compare_at_price", precision = 12, scale = 2)
    private BigDecimal compareAtPrice; // Giá gạch

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice; // Giá vốn

    // Product Type
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType;

    // Racket Specific Fields
    @Enumerated(EnumType.STRING)
    @Column(name = "racket_flexibility")
    private RacketFlexibility racketFlexibility; // Độ cứng vợt

    @Column(name = "racket_balance_point", length = 20)
    private String racketBalancePoint; // VD: 295mm - Head Heavy/Even/Head Light

    @Column(name = "racket_shaft_diameter", length = 20)
    private String racketShaftDiameter; // VD: 7.0mm

    @Column(name = "racket_frame_shape", length = 50)
    private String racketFrameShape; // Isometric/Oval

    @Column(name = "racket_recommended_tension", length = 50)
    private String racketRecommendedTension; // VD: 20-28 lbs

    @Column(name = "racket_max_tension", length = 20)
    private String racketMaxTension; // VD: 30 lbs

    @Column(name = "racket_material")
    private String racketMaterial; // VD: High Modulus Graphite

    // Physical Properties
    @Column(name = "weight", precision = 6, scale = 2)
    private BigDecimal weight; // Trọng lượng (grams)

    @Column(name = "dimensions", length = 100)
    private String dimensions; // Kích thước

    // Stock & Status
    @Column(name = "has_variants")
    @Builder.Default
    private Boolean hasVariants = false;

    @Column(name = "track_inventory")
    @Builder.Default
    private Boolean trackInventory = true;

    // SEO
    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;

    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;

    // Display
    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "is_new_arrival")
    @Builder.Default
    private Boolean isNewArrival = false;

    @Column(name = "is_best_seller")
    @Builder.Default
    private Boolean isBestSeller = false;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    // Statistics
    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "sold_count")
    @Builder.Default
    private Integer soldCount = 0;

    @Column(name = "rating_average", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal ratingAverage = BigDecimal.ZERO;

    @Column(name = "rating_count")
    @Builder.Default
    private Integer ratingCount = 0;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;

    // Audit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Staff createdByStaff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private Staff updatedByStaff;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // Relationships
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductSupplier> suppliers = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Inventory> inventories = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductReview> reviews = new ArrayList<>();

    // Helper methods
    public boolean isRacket() {
        return productType == ProductType.RACKET;
    }

    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE;
    }

    public BigDecimal getCurrentPrice() {
        return basePrice;
    }

    public BigDecimal getDiscountPercentage() {
        if (compareAtPrice != null && compareAtPrice.compareTo(basePrice) > 0) {
            return compareAtPrice.subtract(basePrice)
                .divide(compareAtPrice, 2, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }

    public ProductImage getPrimaryImage() {
        return images.stream()
            .filter(ProductImage::getIsPrimary)
            .findFirst()
            .orElse(images.isEmpty() ? null : images.get(0));
    }

    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementSoldCount(int quantity) {
        this.soldCount += quantity;
    }
}
