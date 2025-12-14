package com.badmintonshop.dto.product;

import com.badmintonshop.entity.Product;
import com.badmintonshop.entity.enums.ProductStatus;
import com.badmintonshop.entity.enums.ProductType;
import com.badmintonshop.entity.enums.RacketFlexibility;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for Product detail
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long productId;
    private String name;
    private String slug;
    private String sku;

    // Category & Brand info
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private Long brandId;
    private String brandName;
    private String brandSlug;
    private String brandLogoUrl;

    private ProductType productType;
    private String shortDescription;
    private String fullDescription;

    // Pricing
    private BigDecimal basePrice;
    private BigDecimal compareAtPrice;
    private BigDecimal currentPrice;
    private BigDecimal discountPercentage;

    // Weight & Dimensions
    private BigDecimal weight;
    private String dimensions;

    // Racket specific
    private RacketFlexibility racketFlexibility;
    private String racketBalancePoint;
    private String racketShaftDiameter;
    private String racketFrameShape;
    private String racketRecommendedTension;
    private String racketMaxTension;
    private String racketMaterial;

    // Status
    private ProductStatus status;
    private Boolean isFeatured;
    private Boolean isNewArrival;
    private Boolean isBestSeller;

    // Stats
    private Integer viewCount;
    private Integer soldCount;
    private BigDecimal ratingAverage;
    private Integer ratingCount;

    // SEO
    private String metaTitle;
    private String metaDescription;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    // Images
    private String primaryImageUrl;
    private List<ProductImageDTO> images;

    // Variants (for detailed view)
    private List<ProductVariantDTO> variants;

    /**
     * Convert from Entity to Response DTO
     */
    public static ProductResponse fromEntity(Product product) {
        if (product == null)
            return null;

        ProductResponseBuilder builder = ProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .productType(product.getProductType())
                .shortDescription(product.getShortDescription())
                .fullDescription(product.getFullDescription())
                .basePrice(product.getBasePrice())
                .compareAtPrice(product.getCompareAtPrice())
                .currentPrice(product.getCurrentPrice())
                .discountPercentage(product.getDiscountPercentage())
                .weight(product.getWeight())
                .dimensions(product.getDimensions())
                .racketFlexibility(product.getRacketFlexibility())
                .racketBalancePoint(product.getRacketBalancePoint())
                .racketShaftDiameter(product.getRacketShaftDiameter())
                .racketFrameShape(product.getRacketFrameShape())
                .racketRecommendedTension(product.getRacketRecommendedTension())
                .racketMaxTension(product.getRacketMaxTension())
                .racketMaterial(product.getRacketMaterial())
                .status(product.getStatus())
                .isFeatured(product.getIsFeatured())
                .isNewArrival(product.getIsNewArrival())
                .isBestSeller(product.getIsBestSeller())
                .viewCount(product.getViewCount())
                .soldCount(product.getSoldCount())
                .ratingAverage(product.getRatingAverage())
                .ratingCount(product.getRatingCount())
                .metaTitle(product.getMetaTitle())
                .metaDescription(product.getMetaDescription())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .publishedAt(product.getPublishedAt());

        // Category info
        if (product.getCategory() != null) {
            builder.categoryId(product.getCategory().getCategoryId())
                    .categoryName(product.getCategory().getName())
                    .categorySlug(product.getCategory().getSlug());
        }

        // Brand info
        if (product.getBrand() != null) {
            builder.brandId(product.getBrand().getBrandId())
                    .brandName(product.getBrand().getName())
                    .brandSlug(product.getBrand().getSlug())
                    .brandLogoUrl(product.getBrand().getLogoUrl());
        }

        // Images
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            builder.images(product.getImages().stream()
                    .map(ProductImageDTO::fromEntity)
                    .collect(Collectors.toList()));

            // Primary image
            product.getImages().stream()
                    .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
                    .findFirst()
                    .ifPresent(img -> builder.primaryImageUrl(img.getImageUrl()));
        }

        // Variants
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            builder.variants(product.getVariants().stream()
                    .map(ProductVariantDTO::fromEntity)
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
