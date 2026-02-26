package com.badmintonshop.controller.admin;

import com.badmintonshop.entity.Staff;
import com.badmintonshop.entity.User;
import com.badmintonshop.entity.enums.ActivityAction;
import com.badmintonshop.entity.enums.NotificationType;
import com.badmintonshop.entity.enums.UserStatus;
import com.badmintonshop.repository.StaffRepository;
import com.badmintonshop.repository.UserRepository;
import com.badmintonshop.service.AuditService;
import com.badmintonshop.service.EmailService;
import com.badmintonshop.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin API for sending notifications (web + email)
 */
@RestController
@RequestMapping("/admin/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationApiController {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final StaffRepository staffRepository;

    /**
     * Send notification to specific users
     * POST /admin/api/notifications/send
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('notifications.send')")
    public ResponseEntity<?> sendNotification(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            // Required fields
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String typeStr = (String) request.get("type");

            // Optional fields
            String actionUrl = (String) request.get("actionUrl");
            Boolean sendToAll = (Boolean) request.get("sendToAll");
            Boolean sendEmail = (Boolean) request.get("sendEmail");

            // Coupon fields (for promotional emails)
            String couponCode = (String) request.get("couponCode");
            String discountText = (String) request.get("discountText");
            String couponExpiry = (String) request.get("couponExpiry");

            @SuppressWarnings("unchecked")
            List<Number> userIdNumbers = (List<Number>) request.get("userIds");
            List<Long> userIds = userIdNumbers != null
                    ? userIdNumbers.stream().map(Number::longValue).toList()
                    : null;

            // Validation
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tiêu đề không được để trống"));
            }
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nội dung không được để trống"));
            }

            NotificationType type = NotificationType.SYSTEM;
            if (typeStr != null) {
                try {
                    type = NotificationType.valueOf(typeStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Use default type
                }
            }

            int notificationCount = 0;
            int emailCount = 0;

            if (sendToAll != null && sendToAll) {
                // Send to all users
                notificationService.sendNotificationToAllUsers(type, title, message, actionUrl);
                log.info("Sending notification to all users: {}", title);

                // Send email to all if requested
                if (sendEmail != null && sendEmail) {
                    List<User> allUsers = userRepository.findByStatusAndDeletedAtIsNull(UserStatus.ACTIVE);
                    for (User user : allUsers) {
                        emailService.sendPromotionEmail(
                                user.getEmail(),
                                user.getFullName(),
                                title,
                                message,
                                typeStr,
                                couponCode,
                                discountText,
                                couponExpiry,
                                actionUrl);
                        emailCount++;
                    }
                    log.info("Sending email to {} users: {}", emailCount, title);
                }

                String responseMsg = "Đang gửi thông báo đến tất cả người dùng";
                if (sendEmail != null && sendEmail) {
                    responseMsg += " và " + emailCount + " email";
                }
                auditService.logActivity(getCurrentStaff(), ActivityAction.CREATE, "Notification", 
                    null, "Gửi thông báo: " + title + " (đến tất cả users)", null, null, httpRequest);
                return ResponseEntity.ok(Map.of("message", responseMsg));

            } else if (userIds != null && !userIds.isEmpty()) {
                // Send to specific users
                notificationService.sendNotificationToUsers(userIds, type, title, message, actionUrl);
                notificationCount = userIds.size();
                log.info("Sending notification to {} users: {}", notificationCount, title);

                // Send email if requested
                if (sendEmail != null && sendEmail) {
                    for (Long userId : userIds) {
                        userRepository.findById(userId).ifPresent(user -> {
                            emailService.sendPromotionEmail(
                                    user.getEmail(),
                                    user.getFullName(),
                                    title,
                                    message,
                                    typeStr,
                                    couponCode,
                                    discountText,
                                    couponExpiry,
                                    actionUrl);
                        });
                    }
                    emailCount = userIds.size();
                    log.info("Sending email to {} users: {}", emailCount, title);
                }

                String responseMsg = "Đang gửi thông báo đến " + notificationCount + " người dùng";
                if (sendEmail != null && sendEmail) {
                    responseMsg += " và " + emailCount + " email";
                }
                auditService.logActivity(getCurrentStaff(), ActivityAction.CREATE, "Notification", 
                    null, "Gửi thông báo: " + title + " (đến " + notificationCount + " users)", null, null, httpRequest);
                return ResponseEntity.ok(Map.of("message", responseMsg));

            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn người dùng hoặc gửi tất cả"));
            }
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Staff getCurrentStaff() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return staffRepository.findByEmail(email).orElse(null);
    }
}
