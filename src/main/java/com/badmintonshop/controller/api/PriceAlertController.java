package com.badmintonshop.controller.api;

import com.badmintonshop.dto.notification.PriceAlertDTO;
import com.badmintonshop.security.CustomUserDetails;
import com.badmintonshop.service.PriceAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Public API for price alerts
 */
@RestController
@RequestMapping("/api/price-alerts")
@RequiredArgsConstructor
@Slf4j
public class PriceAlertController {

    private final PriceAlertService priceAlertService;

    /**
     * Get user's price alerts
     * GET /api/price-alerts
     */
    @GetMapping
    public ResponseEntity<List<PriceAlertDTO>> getPriceAlerts(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<PriceAlertDTO> alerts = priceAlertService.getUserPriceAlerts(currentUser.getUserId());
        return ResponseEntity.ok(alerts);
    }

    /**
     * Create price alert
     * POST /api/price-alerts
     */
    @PostMapping
    public ResponseEntity<?> createPriceAlert(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody Map<String, Object> request) {

        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            BigDecimal targetPrice = new BigDecimal(request.get("targetPrice").toString());

            PriceAlertDTO alert = priceAlertService.createPriceAlert(
                    currentUser.getUserId(), productId, targetPrice);
            return ResponseEntity.status(HttpStatus.CREATED).body(alert);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete price alert
     * DELETE /api/price-alerts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePriceAlert(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        try {
            priceAlertService.deletePriceAlert(id, currentUser.getUserId());
            return ResponseEntity.ok(Map.of("message", "Đã hủy đăng ký thông báo giá"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
