package com.badmintonshop.dto.product;

import com.badmintonshop.entity.Product;
import com.badmintonshop.entity.enums.ProductStatus;
import com.badmintonshop.entity.enums.ProductType;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for Product list (lightweight version for listings)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListDTO {

    private Long productId;
    private String name;
    private String slug;
    private String sku;

    // Category & Brand
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private String brandLogoUrl;

    private ProductType productType;
    private String shortDescription;

    // Pricing
    private BigDecimal basePrice;
    private BigDecimal compareAtPrice;
    private BigDecimal currentPrice;
    private BigDecimal discountPercentage;

    // Image
    private String primaryImageUrl;

    // Status
    private ProductStatus status;
    private Boolean isFeatured;
    private Boolean isNewArrival;
    private Boolean isBestSeller;

    // Stats
    private Integer soldCount;
    private BigDecimal ratingAverage;
    private Integer ratingCount;

    /**
     * Convert from Entity to List DTO
     */
    public static ProductListDTO fromEntity(Product product) {
        if (product == null)
            return null;

        ProductListDTOBuilder builder = ProductListDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .productType(product.getProductType())
                .shortDescription(product.getShortDescription())
                .basePrice(product.getBasePrice())
                .compareAtPrice(product.getCompareAtPrice())
                .currentPrice(product.getCurrentPrice())
                .discountPercentage(product.getDiscountPercentage())
                .status(product.getStatus())
                .isFeatured(product.getIsFeatured())
                .isNewArrival(product.getIsNewArrival())
                .isBestSeller(product.getIsBestSeller())
                .soldCount(product.getSoldCount())
                .ratingAverage(product.getRatingAverage())
                .ratingCount(product.getRatingCount());

        // Category info
        if (product.getCategory() != null) {
            builder.categoryId(product.getCategory().getCategoryId())
                    .categoryName(product.getCategory().getName());
        }

        // Brand info
        if (product.getBrand() != null) {
            builder.brandId(product.getBrand().getBrandId())
                    .brandName(product.getBrand().getName())
                    .brandLogoUrl(product.getBrand().getLogoUrl());
        }

        // Primary image
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            product.getImages().stream()
                    .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
                    .findFirst()
                    .ifPresent(img -> builder.primaryImageUrl(img.getImageUrl()));

            // Fallback to first image if no primary
            if (builder.build().getPrimaryImageUrl() == null) {
                builder.primaryImageUrl(product.getImages().get(0).getImageUrl());
            }
        }

        return builder.build();
    }
}
