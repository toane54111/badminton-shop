package com.badmintonshop.dto.product;

import com.badmintonshop.entity.Category;
import com.badmintonshop.entity.enums.CategoryStatus;
import com.badmintonshop.entity.enums.CategoryType;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for Category entity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

    private Long categoryId;
    private Long parentCategoryId;
    private String parentCategoryName;
    private String name;
    private String slug;
    private String description;
    private String icon;
    private String imageUrl;
    private CategoryType categoryType;
    private Integer displayOrder;
    private Boolean isActive;
    private CategoryStatus status;
    private Long productCount;

    /**
     * Convert from Entity to DTO
     */
    public static CategoryDTO fromEntity(Category category) {
        if (category == null)
            return null;

        return CategoryDTO.builder()
                .categoryId(category.getCategoryId())
                .parentCategoryId(
                        category.getParentCategory() != null ? category.getParentCategory().getCategoryId() : null)
                .parentCategoryName(
                        category.getParentCategory() != null ? category.getParentCategory().getName() : null)
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .icon(category.getIcon())
                .imageUrl(category.getImageUrl())
                .categoryType(category.getCategoryType())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .status(category.getStatus())
                .productCount(category.getProducts() != null ? (long) category.getProducts().size() : 0L)
                .build();
    }

    /**
     * Convert from Entity to DTO (simple version)
     */
    public static CategoryDTO fromEntitySimple(Category category) {
        if (category == null)
            return null;

        return CategoryDTO.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .slug(category.getSlug())
                .icon(category.getIcon())
                .imageUrl(category.getImageUrl())
                .categoryType(category.getCategoryType())
                .isActive(category.getIsActive())
                .build();
    }
}
