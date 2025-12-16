package com.badmintonshop.controller.api;

import com.badmintonshop.dto.coupon.CouponDTO;
import com.badmintonshop.dto.coupon.CouponValidationResponse;
import com.badmintonshop.entity.Coupon;
import com.badmintonshop.security.CustomOAuth2User;
import com.badmintonshop.security.CustomUserDetails;
import com.badmintonshop.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Coupons (Public - for guests and users)
 */
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Slf4j
public class CouponController {

    private final CouponService couponService;

    /**
     * Get current user ID from authentication
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        } else if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getUserId();
        }
        return null;
    }

    /**
     * Validate a coupon code
     * POST /api/coupons/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @RequestParam String code,
            @RequestParam BigDecimal orderTotal) {

        Long userId = getCurrentUserId();

        CouponValidationResponse response = couponService.validateCoupon(code, userId, orderTotal);
        return ResponseEntity.ok(response);
    }

    /**
     * Apply a coupon during checkout
     * POST /api/coupons/apply
     * This validates and applies the coupon to the current session/order
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyCoupon(
            @RequestParam String code,
            @RequestParam BigDecimal orderTotal) {

        Long userId = getCurrentUserId();

        // Validate coupon first
        CouponValidationResponse validation = couponService.validateCoupon(code, userId, orderTotal);

        if (!validation.isValid()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", validation.getErrorMessage()));
        }

        // Get coupon details for the response
        Coupon coupon = couponService.getCouponByCode(code);

        log.info("Coupon {} applied for user {}, discount: {}",
                code, userId, validation.getDiscountAmount());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Áp dụng mã giảm giá thành công",
                "couponCode", validation.getCode(),
                "couponName", validation.getName(),
                "discountAmount", validation.getDiscountAmount(),
                "newTotal", validation.getNewTotal(),
                "couponId", coupon.getCouponId()));
    }

    /**
     * Remove applied coupon
     * DELETE /api/coupons/remove
     */
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeCoupon() {
        log.info("Coupon removed from cart by user {}", getCurrentUserId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã hủy mã giảm giá"));
    }

    /**
     * Get available coupons for checkout
     * GET /api/coupons/available
     */
    @GetMapping("/available")
    public ResponseEntity<List<CouponDTO>> getAvailableCoupons() {
        Long userId = getCurrentUserId();
        List<CouponDTO> coupons = couponService.getAvailableCoupons(userId);
        return ResponseEntity.ok(coupons);
    }
}
