package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.CategoryStatus;
import com.badmintonshop.entity.enums.CategoryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity Category - Danh mục sản phẩm - hỗ trợ cây phân cấp
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_categories_parent", columnList = "parent_category_id"),
    @Index(name = "idx_categories_type", columnList = "category_type"),
    @Index(name = "idx_categories_active", columnList = "is_active"),
    @Index(name = "idx_categories_status", columnList = "status"),
    @Index(name = "idx_categories_order", columnList = "display_order"),
    @Index(name = "idx_categories_deleted", columnList = "deleted_at"),
    @Index(name = "idx_categories_active_deleted_order", columnList = "is_active, deleted_at, display_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon", length = 100)
    private String icon; // Icon class hoặc emoji

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false)
    private CategoryType categoryType;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private CategoryStatus status = CategoryStatus.ACTIVE;

    // Relationships
    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Category> childCategories = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    // Helper methods
    public boolean isRootCategory() {
        return parentCategory == null;
    }

    public boolean hasChildren() {
        return !childCategories.isEmpty();
    }
}
