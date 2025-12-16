package com.badmintonshop.controller.api;

import com.badmintonshop.dto.notification.NotificationDTO;
import com.badmintonshop.security.CustomUserDetails;
import com.badmintonshop.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public API for user notifications
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get current user's notifications
     * GET /api/notifications
     */
    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type) {

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDTO> notifications = notificationService.getUserNotifications(
                currentUser.getUserId(), type, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notification count
     * GET /api/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        long count = notificationService.getUnreadCount(currentUser.getUserId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Mark notification as read
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        try {
            NotificationDTO notification = notificationService.markAsRead(id, currentUser.getUserId());
            return ResponseEntity.ok(notification);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Mark all notifications as read
     * PUT /api/notifications/read-all
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        int count = notificationService.markAllAsRead(currentUser.getUserId());
        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu tất cả đã đọc", "count", count));
    }
}
