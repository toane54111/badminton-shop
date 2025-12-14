package com.badmintonshop.controller.admin;

import com.badmintonshop.dto.product.ProductImageDTO;
import com.badmintonshop.dto.product.ProductListDTO;
import com.badmintonshop.dto.product.ProductRequest;
import com.badmintonshop.dto.product.ProductResponse;
import com.badmintonshop.dto.product.ProductVariantDTO;
import com.badmintonshop.entity.enums.ProductStatus;
import com.badmintonshop.entity.enums.ProductType;
import com.badmintonshop.service.ProductImageService;
import com.badmintonshop.service.ProductService;
import com.badmintonshop.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Admin API Controller for Product CRUD operations
 * Includes image and variant management
 */
@RestController
@RequestMapping("/admin/api/products")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALE_STAFF', 'CONTENT_STAFF')")
public class AdminProductController {

    private final ProductService productService;
    private final ProductImageService productImageService;
    private final ProductVariantService productVariantService;

    // ==================== PRODUCT CRUD ====================

    /**
     * Get all products with pagination and filtering
     * GET /admin/api/products
     */
    @GetMapping
    public ResponseEntity<Page<ProductListDTO>> getAllProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(productService.searchProductsAdvanced(
                keyword, categoryId, brandId, productType,
                minPrice, maxPrice, status, isPublished, pageable));
    }

    /**
     * Get product by ID
     * GET /admin/api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new product
     * POST /admin/api/products
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse created = productService.createProduct(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update product
     * PUT /admin/api/products/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse updated = productService.updateProduct(id, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete product (soft delete)
     * DELETE /admin/api/products/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("message", "Xóa sản phẩm thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get low stock products
     * GET /admin/api/products/low-stock
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductListDTO>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(productService.getLowStockProducts(threshold));
    }

    // ==================== IMAGE MANAGEMENT ====================

    /**
     * Get images for a product
     * GET /admin/api/products/{id}/images
     */
    @GetMapping("/{id}/images")
    public ResponseEntity<List<ProductImageDTO>> getProductImages(@PathVariable Long id) {
        return ResponseEntity.ok(productImageService.getImagesByProductId(id));
    }

    /**
     * Upload image for a product
     * POST /admin/api/products/{id}/images
     */
    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> uploadProductImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String altText,
            @RequestParam(defaultValue = "false") boolean isPrimary) {
        try {
            ProductImageDTO image = productImageService.uploadImage(id, file, altText, isPrimary);
            return ResponseEntity.status(HttpStatus.CREATED).body(image);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi upload file: " + e.getMessage()));
        }
    }

    /**
     * Add image by URL
     * POST /admin/api/products/{id}/images/url
     */
    @PostMapping("/{id}/images/url")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> addProductImageByUrl(
            @PathVariable Long id,
            @RequestParam String imageUrl,
            @RequestParam(required = false) String altText,
            @RequestParam(defaultValue = "false") boolean isPrimary) {
        try {
            ProductImageDTO image = productImageService.addImage(id, imageUrl, altText, isPrimary);
            return ResponseEntity.status(HttpStatus.CREATED).body(image);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete product image
     * DELETE /admin/api/products/{id}/images/{imageId}
     */
    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteProductImage(@PathVariable Long id, @PathVariable Long imageId) {
        try {
            productImageService.deleteImage(id, imageId);
            return ResponseEntity.ok(Map.of("message", "Xóa hình ảnh thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Set primary image
     * PUT /admin/api/products/{id}/images/{imageId}/primary
     */
    @PutMapping("/{id}/images/{imageId}/primary")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> setPrimaryImage(@PathVariable Long id, @PathVariable Long imageId) {
        try {
            productImageService.setPrimaryImage(id, imageId);
            return ResponseEntity.ok(Map.of("message", "Đã đặt làm hình ảnh chính"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Reorder images
     * PUT /admin/api/products/{id}/images/reorder
     */
    @PutMapping("/{id}/images/reorder")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> reorderImages(@PathVariable Long id, @RequestBody List<Long> imageIds) {
        try {
            productImageService.reorderImages(id, imageIds);
            return ResponseEntity.ok(Map.of("message", "Đã sắp xếp lại hình ảnh"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== VARIANT MANAGEMENT ====================

    /**
     * Get variants for a product
     * GET /admin/api/products/{id}/variants
     */
    @GetMapping("/{id}/variants")
    public ResponseEntity<List<ProductVariantDTO>> getProductVariants(@PathVariable Long id) {
        return ResponseEntity.ok(productVariantService.getVariantsByProductId(id));
    }

    /**
     * Create variant for a product
     * POST /admin/api/products/{id}/variants
     */
    @PostMapping("/{id}/variants")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createVariant(@PathVariable Long id, @Valid @RequestBody ProductVariantDTO dto) {
        try {
            ProductVariantDTO created = productVariantService.createVariant(id, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update variant
     * PUT /admin/api/products/{id}/variants/{variantId}
     */
    @PutMapping("/{id}/variants/{variantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateVariant(
            @PathVariable Long id,
            @PathVariable Long variantId,
            @Valid @RequestBody ProductVariantDTO dto) {
        try {
            ProductVariantDTO updated = productVariantService.updateVariant(id, variantId, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete variant
     * DELETE /admin/api/products/{id}/variants/{variantId}
     */
    @DeleteMapping("/{id}/variants/{variantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteVariant(@PathVariable Long id, @PathVariable Long variantId) {
        try {
            productVariantService.deleteVariant(id, variantId);
            return ResponseEntity.ok(Map.of("message", "Xóa biến thể thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
