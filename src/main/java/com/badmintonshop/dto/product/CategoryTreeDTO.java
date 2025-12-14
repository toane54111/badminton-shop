package com.badmintonshop.dto.product;

import com.badmintonshop.entity.Category;
import com.badmintonshop.entity.enums.CategoryStatus;
import com.badmintonshop.entity.enums.CategoryType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for Category tree structure
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTreeDTO {

    private Long categoryId;
    private String name;
    private String slug;
    private String icon;
    private String imageUrl;
    private CategoryType categoryType;
    private Integer displayOrder;
    private Boolean isActive;
    private Long productCount;

    @Builder.Default
    private List<CategoryTreeDTO> children = new ArrayList<>();

    /**
     * Convert from Entity to Tree DTO (recursive)
     */
    public static CategoryTreeDTO fromEntity(Category category) {
        if (category == null)
            return null;

        CategoryTreeDTO dto = CategoryTreeDTO.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .slug(category.getSlug())
                .icon(category.getIcon())
                .imageUrl(category.getImageUrl())
                .categoryType(category.getCategoryType())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .productCount(category.getProducts() != null ? (long) category.getProducts().size() : 0L)
                .children(new ArrayList<>())
                .build();

        // Recursively convert children
        if (category.getChildCategories() != null && !category.getChildCategories().isEmpty()) {
            dto.setChildren(
                    category.getChildCategories().stream()
                            .filter(c -> c.getIsActive() != null && c.getIsActive())
                            .map(CategoryTreeDTO::fromEntity)
                            .collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * Check if has children
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
}
