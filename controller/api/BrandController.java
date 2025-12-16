package com.badmintonshop.controller.api;

import com.badmintonshop.dto.product.BrandDTO;
import com.badmintonshop.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public API Controller for Brand
 */
@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    /**
     * Get all active brands
     * GET /api/brands
     */
    @GetMapping
    public ResponseEntity<List<BrandDTO>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllActiveBrands());
    }

    /**
     * Get brand by slug
     * GET /api/brands/{slug}
     */
    @GetMapping("/{slug}")
    public ResponseEntity<BrandDTO> getBrandBySlug(@PathVariable String slug) {
        return brandService.getBrandBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
