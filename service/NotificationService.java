package com.badmintonshop.service;

import com.badmintonshop.dto.notification.NotificationDTO;
import com.badmintonshop.entity.Notification;
import com.badmintonshop.entity.User;
import com.badmintonshop.entity.enums.NotificationType;
import com.badmintonshop.repository.NotificationRepository;
import com.badmintonshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * Get user's notifications with pagination and optional type filter
     */
    public Page<NotificationDTO> getUserNotifications(Long userId, String type, Pageable pageable) {
        if (type != null && !type.trim().isEmpty()) {
            try {
                NotificationType notifType = NotificationType.valueOf(type.toUpperCase());
                return notificationRepository.findByUserUserIdAndType(userId, notifType, pageable)
                        .map(this::mapToDTO);
            } catch (IllegalArgumentException e) {
                // Invalid type, fall back to all notifications
            }
        }
        return notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Get unread notification count for user
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserUserIdAndIsReadFalse(userId);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public NotificationDTO markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification không tồn tại"));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Không có quyền truy cập notification này");
        }

        notification.markAsRead();
        Notification saved = notificationRepository.save(notification);
        return mapToDTO(saved);
    }

    /**
     * Mark all notifications as read for user
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        int updated = notificationRepository.markAllAsReadByUserId(userId);
        log.info("Marked {} notifications as read for user {}", updated, userId);
        return updated;
    }

    /**
     * Create notification for a single user
     */
    @Transactional
    public NotificationDTO createNotification(Long userId, NotificationType type, String title,
            String message, String actionUrl, String relatedEntityType, Long relatedEntityId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại: " + userId));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .actionUrl(actionUrl)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Created notification for user {}: {}", userId, title);
        return mapToDTO(saved);
    }

    /**
     * Send notification to multiple users (async)
     */
    @Async
    @Transactional
    public CompletableFuture<Integer> sendNotificationToUsers(List<Long> userIds, NotificationType type,
            String title, String message, String actionUrl) {

        int count = 0;
        for (Long userId : userIds) {
            try {
                createNotification(userId, type, title, message, actionUrl, null, null);
                count++;
            } catch (Exception e) {
                log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
            }
        }

        log.info("Sent notification to {} users: {}", count, title);
        return CompletableFuture.completedFuture(count);
    }

    /**
     * Send notification to all active users (async)
     */
    @Async
    @Transactional
    public CompletableFuture<Integer> sendNotificationToAllUsers(NotificationType type, String title,
            String message, String actionUrl) {

        List<Long> activeUserIds = userRepository.findAll().stream()
                .filter(u -> u.isActive() && !u.isLocked())
                .map(User::getUserId)
                .collect(Collectors.toList());

        return sendNotificationToUsers(activeUserIds, type, title, message, actionUrl);
    }

    /**
     * Send notification with email (async)
     */
    @Async
    public void sendNotificationWithEmail(Long userId, NotificationType type, String title,
            String message, String actionUrl) {

        try {
            // Create in-app notification
            createNotification(userId, type, title, message, actionUrl, null, null);

            // Log email sending (EmailService only supports verification/password reset
            // emails)
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.getEmail() != null) {
                log.info("Would send email notification to {}: {}", user.getEmail(), title);
                // TODO: Add general email sending support to EmailService
            }
        } catch (Exception e) {
            log.error("Error sending notification with email to user {}: {}", userId, e.getMessage());
        }
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .actionUrl(notification.getActionUrl())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
