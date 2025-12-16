package com.badmintonshop.controller.admin;

import com.badmintonshop.dto.banner.BannerDTO;
import com.badmintonshop.dto.product.BrandDTO;
import com.badmintonshop.dto.product.CategoryDTO;
import com.badmintonshop.dto.product.ProductListDTO;
import com.badmintonshop.service.BannerService;
import com.badmintonshop.service.BrandService;
import com.badmintonshop.service.CategoryService;
import com.badmintonshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin Trash Controller - manages soft-deleted items
 * SUPER_ADMIN only access
 */
@RestController
@RequestMapping("/admin/api/trash")
@RequiredArgsConstructor
@Slf4j
public class AdminTrashController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final BannerService bannerService;

    // ===== PRODUCTS =====

    /**
     * Get deleted products
     * GET /admin/api/trash/products
     */
    @GetMapping("/products")
    public ResponseEntity<Page<ProductListDTO>> getDeletedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("deletedAt").descending());
        Page<ProductListDTO> products = productService.getDeletedProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Restore product from trash
     * POST /admin/api/trash/products/{id}/restore
     */
    @PostMapping("/products/{id}/restore")
    public ResponseEntity<?> restoreProduct(@PathVariable Long id) {
        try {
            productService.restoreProduct(id);
            return ResponseEntity.ok(Map.of("message", "Đã khôi phục sản phẩm"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Hard delete product (permanently)
     * DELETE /admin/api/trash/products/{id}
     */
    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> hardDeleteProduct(@PathVariable Long id) {
        try {
            productService.hardDeleteProduct(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa vĩnh viễn sản phẩm"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== CATEGORIES =====

    /**
     * Get deleted categories
     * GET /admin/api/trash/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<Page<CategoryDTO>> getDeletedCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("deletedAt").descending());
        Page<CategoryDTO> categories = categoryService.getDeletedCategories(pageable);
        return ResponseEntity.ok(categories);
    }

    /**
     * Restore category from trash
     * POST /admin/api/trash/categories/{id}/restore
     */
    @PostMapping("/categories/{id}/restore")
    public ResponseEntity<?> restoreCategory(@PathVariable Long id) {
        try {
            categoryService.restoreCategory(id);
            return ResponseEntity.ok(Map.of("message", "Đã khôi phục danh mục"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Hard delete category (permanently)
     * DELETE /admin/api/trash/categories/{id}
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> hardDeleteCategory(@PathVariable Long id) {
        try {
            categoryService.hardDeleteCategory(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa vĩnh viễn danh mục"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== BRANDS =====

    /**
     * Get deleted brands
     * GET /admin/api/trash/brands
     */
    @GetMapping("/brands")
    public ResponseEntity<Page<BrandDTO>> getDeletedBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("deletedAt").descending());
        Page<BrandDTO> brands = brandService.getDeletedBrands(pageable);
        return ResponseEntity.ok(brands);
    }

    /**
     * Restore brand from trash
     * POST /admin/api/trash/brands/{id}/restore
     */
    @PostMapping("/brands/{id}/restore")
    public ResponseEntity<?> restoreBrand(@PathVariable Long id) {
        try {
            brandService.restoreBrand(id);
            return ResponseEntity.ok(Map.of("message", "Đã khôi phục thương hiệu"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Hard delete brand (permanently)
     * DELETE /admin/api/trash/brands/{id}
     */
    @DeleteMapping("/brands/{id}")
    public ResponseEntity<?> hardDeleteBrand(@PathVariable Long id) {
        try {
            brandService.hardDeleteBrand(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa vĩnh viễn thương hiệu"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== BANNERS =====

    /**
     * Get deleted banners
     * GET /admin/api/trash/banners
     */
    @GetMapping("/banners")
    public ResponseEntity<Page<BannerDTO>> getDeletedBanners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("deletedAt").descending());
        Page<BannerDTO> banners = bannerService.getDeletedBanners(pageable);
        return ResponseEntity.ok(banners);
    }

    /**
     * Restore banner from trash
     * POST /admin/api/trash/banners/{id}/restore
     */
    @PostMapping("/banners/{id}/restore")
    public ResponseEntity<?> restoreBanner(@PathVariable Long id) {
        try {
            bannerService.restoreBanner(id);
            return ResponseEntity.ok(Map.of("message", "Đã khôi phục banner"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Hard delete banner (permanently)
     * DELETE /admin/api/trash/banners/{id}
     */
    @DeleteMapping("/banners/{id}")
    public ResponseEntity<?> hardDeleteBanner(@PathVariable Long id) {
        try {
            bannerService.hardDeleteBanner(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa vĩnh viễn banner"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
