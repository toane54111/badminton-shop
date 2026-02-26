package com.badmintonshop.dto.product;

import com.badmintonshop.entity.ProductImage;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for ProductImage
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageDTO {

    private Long imageId;
    private Long productId;
    private String imageUrl;
    private String altText;
    private String title;
    private Integer displayOrder;
    private Boolean isPrimary;
    private LocalDateTime createdAt;

    /**
     * Convert from Entity to DTO
     */
    public static ProductImageDTO fromEntity(ProductImage image) {
        if (image == null)
            return null;

        return ProductImageDTO.builder()
                .imageId(image.getImageId())
                .productId(image.getProduct() != null ? image.getProduct().getProductId() : null)
                .imageUrl(image.getImageUrl())
                .altText(image.getAltText())
                .title(image.getTitle())
                .displayOrder(image.getDisplayOrder())
                .isPrimary(image.getIsPrimary())
                .createdAt(image.getCreatedAt())
                .build();
    }
}
