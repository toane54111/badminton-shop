package com.badmintonshop.service;

import com.badmintonshop.entity.Coupon;
import com.badmintonshop.entity.Promotion;
import com.badmintonshop.entity.User;
import com.badmintonshop.entity.enums.NotificationType;
import com.badmintonshop.entity.enums.UserStatus;
import com.badmintonshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for sending automatic notifications when coupons/promotions are
 * created
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionNotificationService {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Send notification to all users when a new coupon is created
     */
    @Async("emailExecutor")
    public void notifyNewCoupon(Coupon coupon) {
        if (coupon == null || !coupon.getIsActive()) {
            return;
        }

        log.info("Sending notifications for new coupon: {}", coupon.getCode());

        String title = "🎉 Mã giảm giá mới: " + coupon.getCode();
        String discountText = formatDiscount(coupon);
        String message = String.format(
                "Sử dụng mã <strong>%s</strong> để được %s cho đơn hàng của bạn!<br><br>%s",
                coupon.getCode(),
                discountText,
                coupon.getDescription() != null ? coupon.getDescription() : "");
        String expiryDate = coupon.getExpiresAt() != null
                ? coupon.getExpiresAt().format(DATE_FORMAT)
                : null;
        String actionUrl = "/coupons";

        // Get all active users
        List<User> users = userRepository.findByStatusAndDeletedAtIsNull(UserStatus.ACTIVE);

        for (User user : users) {
            try {
                // Send web notification
                notificationService.createNotification(
                        user.getUserId(),
                        NotificationType.PROMOTION,
                        title,
                        "Mã: " + coupon.getCode() + " - " + discountText,
                        actionUrl,
                        "COUPON",
                        coupon.getCouponId());

                // Send email
                emailService.sendPromotionEmail(
                        user.getEmail(),
                        user.getFullName(),
                        title,
                        message,
                        "PROMOTION",
                        coupon.getCode(),
                        discountText,
                        expiryDate,
                        actionUrl);
            } catch (Exception e) {
                log.error("Failed to send coupon notification to user {}: {}", user.getUserId(), e.getMessage());
            }
        }

        log.info("Sent coupon notifications to {} users", users.size());
    }

    /**
     * Send notification to all users when a new promotion is created
     */
    @Async("emailExecutor")
    public void notifyNewPromotion(Promotion promotion) {
        if (promotion == null || !promotion.getIsActive()) {
            return;
        }

        log.info("Sending notifications for new promotion: {}", promotion.getName());

        String title = "🔥 Khuyến mãi mới: " + promotion.getName();
        String discountText = formatPromotionDiscount(promotion);
        String message = String.format(
                "<strong>%s</strong><br><br>%s<br><br>%s",
                promotion.getName(),
                discountText,
                promotion.getDescription() != null ? promotion.getDescription() : "");
        String expiryDate = promotion.getEndsAt() != null
                ? promotion.getEndsAt().format(DATE_FORMAT)
                : null;
        String actionUrl = "/promotions";

        // Get all active users
        List<User> users = userRepository.findByStatusAndDeletedAtIsNull(UserStatus.ACTIVE);

        for (User user : users) {
            try {
                // Send web notification
                notificationService.createNotification(
                        user.getUserId(),
                        NotificationType.PROMOTION,
                        title,
                        discountText,
                        actionUrl,
                        "PROMOTION",
                        promotion.getPromotionId());

                // Send email
                emailService.sendPromotionEmail(
                        user.getEmail(),
                        user.getFullName(),
                        title,
                        message,
                        "PROMOTION",
                        null, // No coupon code for promotions
                        discountText,
                        expiryDate,
                        actionUrl);
            } catch (Exception e) {
                log.error("Failed to send promotion notification to user {}: {}", user.getUserId(), e.getMessage());
            }
        }

        log.info("Sent promotion notifications to {} users", users.size());
    }

    private String formatDiscount(Coupon coupon) {
        if (coupon.getType() == null) {
            return "giảm giá";
        }
        switch (coupon.getType()) {
            case PERCENTAGE:
                return "Giảm " + coupon.getValue().intValue() + "%";
            case FIXED_AMOUNT:
                return "Giảm " + formatPrice(coupon.getValue());
            case FREE_SHIPPING:
                return "Miễn phí vận chuyển";
            default:
                return "giảm giá";
        }
    }

    private String formatPromotionDiscount(Promotion promotion) {
        if (promotion.getDiscountType() == null) {
            return "Khuyến mãi đặc biệt";
        }
        switch (promotion.getDiscountType()) {
            case PERCENTAGE:
                return "Giảm " + promotion.getDiscountValue().intValue() + "%";
            case FIXED_AMOUNT:
                return "Giảm " + formatPrice(promotion.getDiscountValue());
            default:
                return "Khuyến mãi đặc biệt";
        }
    }

    private String formatPrice(BigDecimal price) {
        if (price == null)
            return "0đ";
        return String.format("%,.0fđ", price);
    }
}
