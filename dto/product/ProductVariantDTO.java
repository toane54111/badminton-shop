package com.badmintonshop.dto.product;

import com.badmintonshop.entity.ProductVariant;
import com.badmintonshop.entity.enums.VariantStatus;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for ProductVariant
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantDTO {

    private Long variantId;
    private Long productId;
    private String sku;
    private String barcode;
    private String attributes;
    private String variantName;
    private BigDecimal priceAdjustment;
    private BigDecimal finalPrice;
    private String imageUrl;
    private VariantStatus status;
    private Integer stockQuantity;

    /**
     * Convert from Entity to DTO
     */
    public static ProductVariantDTO fromEntity(ProductVariant variant) {
        if (variant == null)
            return null;

        return ProductVariantDTO.builder()
                .variantId(variant.getVariantId())
                .productId(variant.getProduct() != null ? variant.getProduct().getProductId() : null)
                .sku(variant.getSku())
                .barcode(variant.getBarcode())
                .attributes(variant.getAttributes())
                .variantName(variant.getVariantName())
                .priceAdjustment(variant.getPriceAdjustment())
                .finalPrice(variant.getFinalPrice())
                .imageUrl(variant.getImageUrl())
                .status(variant.getStatus())
                .build();
    }
}
