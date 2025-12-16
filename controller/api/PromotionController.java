package com.badmintonshop.controller.api;

import com.badmintonshop.dto.promotion.PromotionDTO;
import com.badmintonshop.entity.enums.PromotionType;
import com.badmintonshop.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for Promotions (Public - for guests and users)
 */
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Slf4j
public class PromotionController {

    private final PromotionService promotionService;

    /**
     * Get all active promotions
     * GET /api/promotions/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<PromotionDTO>> getActivePromotions() {
        List<PromotionDTO> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(promotions);
    }

    /**
     * Get active flash sales
     * GET /api/promotions/flash-sales
     */
    @GetMapping("/flash-sales")
    public ResponseEntity<List<PromotionDTO>> getFlashSales() {
        List<PromotionDTO> flashSales = promotionService.getActivePromotionsByType(PromotionType.FLASH_SALE);
        return ResponseEntity.ok(flashSales);
    }

    /**
     * Get active bundles/combos
     * GET /api/promotions/bundles
     */
    @GetMapping("/bundles")
    public ResponseEntity<List<PromotionDTO>> getBundles() {
        List<PromotionDTO> bundles = promotionService.getActivePromotionsByType(PromotionType.BUNDLE);
        return ResponseEntity.ok(bundles);
    }

    /**
     * Get promotion by ID
     * GET /api/promotions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PromotionDTO> getPromotionById(@PathVariable Long id) {
        return promotionService.getPromotionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get active promotion by ID (only returns if currently active)
     * GET /api/promotions/{id}/active
     */
    @GetMapping("/{id}/active")
    public ResponseEntity<PromotionDTO> getActivePromotionById(@PathVariable Long id) {
        return promotionService.getActivePromotionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
