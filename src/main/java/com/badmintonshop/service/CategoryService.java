package com.badmintonshop.service;

import com.badmintonshop.dto.product.CategoryDTO;
import com.badmintonshop.dto.product.CategoryTreeDTO;
import com.badmintonshop.entity.Category;
import com.badmintonshop.entity.enums.CategoryStatus;
import com.badmintonshop.entity.enums.CategoryType;
import com.badmintonshop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for Category operations with tree structure support
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Get all active categories
     */
    public List<CategoryDTO> getAllActiveCategories() {
        return categoryRepository.findAllActive().stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get category tree (hierarchical structure)
     */
    public List<CategoryTreeDTO> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findRootCategoriesWithChildren();
        return rootCategories.stream()
                .map(CategoryTreeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get root categories only
     */
    public List<CategoryDTO> getRootCategories() {
        return categoryRepository.findRootCategories().stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get children of a category
     */
    public List<CategoryDTO> getChildCategories(Long parentId) {
        return categoryRepository.findChildrenByParentId(parentId).stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all categories with pagination
     */
    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(CategoryDTO::fromEntity);
    }

    /**
     * Get category by ID
     */
    public Optional<CategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(CategoryDTO::fromEntity);
    }

    /**
     * Get category by slug
     */
    public Optional<CategoryDTO> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(CategoryDTO::fromEntity);
    }

    /**
     * Get categories by type
     */
    public List<CategoryDTO> getCategoriesByType(CategoryType type) {
        return categoryRepository.findByCategoryTypeAndIsActiveTrueOrderByDisplayOrderAsc(type).stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Search categories
     */
    public Page<CategoryDTO> searchCategories(String keyword, CategoryStatus status, CategoryType type,
            Pageable pageable) {
        return categoryRepository.searchCategories(keyword, status, type, pageable)
                .map(CategoryDTO::fromEntity);
    }

    /**
     * Create new category
     */
    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        // Generate slug if not provided
        String slug = dto.getSlug();
        if (slug == null || slug.isEmpty()) {
            slug = generateSlug(dto.getName());
        }

        // Check if slug exists
        if (categoryRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Slug đã tồn tại: " + slug);
        }

        Category.CategoryBuilder builder = Category.builder()
                .name(dto.getName())
                .slug(slug)
                .description(dto.getDescription())
                .icon(dto.getIcon())
                .imageUrl(dto.getImageUrl())
                .categoryType(dto.getCategoryType())
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .status(dto.getStatus() != null ? dto.getStatus() : CategoryStatus.ACTIVE);

        // Set parent if provided
        if (dto.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(dto.getParentCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy danh mục cha: " + dto.getParentCategoryId()));
            builder.parentCategory(parent);
        }

        Category category = builder.build();
        category = categoryRepository.save(category);
        log.info("Created category: {}", category.getName());
        return CategoryDTO.fromEntity(category);
    }

    /**
     * Update category
     */
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục: " + id));

        if (dto.getName() != null)
            category.setName(dto.getName());

        if (dto.getSlug() != null && !dto.getSlug().isEmpty()) {
            if (!category.getSlug().equals(dto.getSlug()) && categoryRepository.existsBySlug(dto.getSlug())) {
                throw new IllegalArgumentException("Slug đã tồn tại: " + dto.getSlug());
            }
            category.setSlug(dto.getSlug());
        }

        if (dto.getDescription() != null)
            category.setDescription(dto.getDescription());
        if (dto.getIcon() != null)
            category.setIcon(dto.getIcon());
        if (dto.getImageUrl() != null)
            category.setImageUrl(dto.getImageUrl());
        if (dto.getCategoryType() != null)
            category.setCategoryType(dto.getCategoryType());
        if (dto.getDisplayOrder() != null)
            category.setDisplayOrder(dto.getDisplayOrder());
        if (dto.getIsActive() != null)
            category.setIsActive(dto.getIsActive());
        if (dto.getStatus() != null)
            category.setStatus(dto.getStatus());

        // Update parent
        if (dto.getParentCategoryId() != null) {
            if (dto.getParentCategoryId().equals(id)) {
                throw new IllegalArgumentException("Danh mục không thể là cha của chính nó");
            }
            Category parent = categoryRepository.findById(dto.getParentCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy danh mục cha: " + dto.getParentCategoryId()));
            category.setParentCategory(parent);
        }

        category = categoryRepository.save(category);
        log.info("Updated category: {}", category.getName());
        return CategoryDTO.fromEntity(category);
    }

    /**
     * Delete category (soft delete)
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục: " + id));

        category.setDeletedAt(LocalDateTime.now());
        category.setIsActive(false);
        category.setStatus(CategoryStatus.INACTIVE);
        categoryRepository.save(category);
        log.info("Soft deleted category: {}", category.getName());
    }

    /**
     * Generate slug from name
     */
    private String generateSlug(String name) {
        if (name == null)
            return "";
        return name.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
