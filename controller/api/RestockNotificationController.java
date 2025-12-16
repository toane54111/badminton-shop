package com.badmintonshop.controller.api;

import com.badmintonshop.dto.notification.RestockNotificationDTO;
import com.badmintonshop.security.CustomUserDetails;
import com.badmintonshop.service.RestockNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Public API for restock notifications
 */
@RestController
@RequestMapping("/api/restock-notifications")
@RequiredArgsConstructor
@Slf4j
public class RestockNotificationController {

    private final RestockNotificationService restockNotificationService;

    /**
     * Get user's restock subscriptions
     * GET /api/restock-notifications
     */
    @GetMapping
    public ResponseEntity<List<RestockNotificationDTO>> getRestockNotifications(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<RestockNotificationDTO> notifications = restockNotificationService
                .getUserRestockNotifications(currentUser.getUserId());
        return ResponseEntity.ok(notifications);
    }

    /**
     * Subscribe to restock notification
     * POST /api/restock-notifications
     */
    @PostMapping
    public ResponseEntity<?> createRestockNotification(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody Map<String, Object> request) {

        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            Long variantId = request.get("variantId") != null
                    ? Long.valueOf(request.get("variantId").toString())
                    : null;

            RestockNotificationDTO notification = restockNotificationService.createRestockNotification(
                    currentUser.getUserId(), productId, variantId);
            return ResponseEntity.status(HttpStatus.CREATED).body(notification);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Unsubscribe from restock notification
     * DELETE /api/restock-notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRestockNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        try {
            restockNotificationService.deleteRestockNotification(id, currentUser.getUserId());
            return ResponseEntity.ok(Map.of("message", "Đã hủy đăng ký thông báo hàng về"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
