package com.badmintonshop.dto.product;

import com.badmintonshop.entity.Brand;
import com.badmintonshop.entity.enums.BrandStatus;
import lombok.*;

/**
 * DTO for Brand entity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandDTO {

    private Long brandId;
    private String name;
    private String slug;
    private String logoUrl;
    private String description;
    private String country;
    private String websiteUrl;
    private Integer displayOrder;
    private Boolean isActive;
    private BrandStatus status;
    private Long productCount;

    /**
     * Convert from Entity to DTO
     */
    public static BrandDTO fromEntity(Brand brand) {
        if (brand == null)
            return null;

        return BrandDTO.builder()
                .brandId(brand.getBrandId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .logoUrl(brand.getLogoUrl())
                .description(brand.getDescription())
                .country(brand.getCountry())
                .websiteUrl(brand.getWebsiteUrl())
                .displayOrder(brand.getDisplayOrder())
                .isActive(brand.getIsActive())
                .status(brand.getStatus())
                .productCount(brand.getProducts() != null ? (long) brand.getProducts().size() : 0L)
                .build();
    }

    /**
     * Convert from Entity to DTO (simple version without product count)
     */
    public static BrandDTO fromEntitySimple(Brand brand) {
        if (brand == null)
            return null;

        return BrandDTO.builder()
                .brandId(brand.getBrandId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .logoUrl(brand.getLogoUrl())
                .description(brand.getDescription())
                .country(brand.getCountry())
                .isActive(brand.getIsActive())
                .status(brand.getStatus())
                .build();
    }
}
