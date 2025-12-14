package com.badmintonshop.controller.api;

import com.badmintonshop.dto.product.ProductListDTO;
import com.badmintonshop.dto.product.ProductResponse;
import com.badmintonshop.entity.enums.ProductType;
import com.badmintonshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Public API Controller for Product
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Get all products with pagination and filtering
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<Page<ProductListDTO>> getAllProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductListDTO> products = productService.searchProductsAdvanced(
                keyword, categoryId, brandId, productType,
                minPrice, maxPrice, null, true, pageable);

        return ResponseEntity.ok(products);
    }

    /**
     * Get product by slug
     * GET /api/products/{slug}
     */
    @GetMapping("/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(@PathVariable String slug) {
        return productService.getProductBySlug(slug)
                .map(product -> {
                    // Increment view count asynchronously
                    productService.incrementViewCount(product.getProductId());
                    return ResponseEntity.ok(product);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get featured products
     * GET /api/products/featured
     */
    @GetMapping("/featured")
    public ResponseEntity<List<ProductListDTO>> getFeaturedProducts(
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(productService.getFeaturedProducts(limit));
    }

    /**
     * Get new arrivals
     * GET /api/products/new-arrivals
     */
    @GetMapping("/new-arrivals")
    public ResponseEntity<List<ProductListDTO>> getNewArrivals(
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(productService.getNewArrivals(limit));
    }

    /**
     * Get best sellers
     * GET /api/products/best-sellers
     */
    @GetMapping("/best-sellers")
    public ResponseEntity<List<ProductListDTO>> getBestSellers(
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(productService.getBestSellers(limit));
    }

    /**
     * Search products
     * GET /api/products/search?q=...
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductListDTO>> searchProducts(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(productService.searchProducts(query, pageable));
    }

    /**
     * Get products by category
     * GET /api/products/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductListDTO>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
    }

    /**
     * Get products by brand
     * GET /api/products/brand/{brandId}
     */
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<Page<ProductListDTO>> getProductsByBrand(
            @PathVariable Long brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(productService.getProductsByBrand(brandId, pageable));
    }

    /**
     * Get products on sale
     * GET /api/products/on-sale
     */
    @GetMapping("/on-sale")
    public ResponseEntity<Page<ProductListDTO>> getProductsOnSale(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsOnSale(pageable));
    }
}
