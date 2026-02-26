package com.badmintonshop.service;

import com.badmintonshop.dto.notification.RestockNotificationDTO;
import com.badmintonshop.entity.Product;
import com.badmintonshop.entity.ProductVariant;
import com.badmintonshop.entity.RestockNotification;
import com.badmintonshop.entity.User;
import com.badmintonshop.entity.enums.NotificationType;
import com.badmintonshop.repository.ProductRepository;
import com.badmintonshop.repository.ProductVariantRepository;
import com.badmintonshop.repository.RestockNotificationRepository;
import com.badmintonshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RestockNotificationService {

    private final RestockNotificationRepository restockNotificationRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Get user's restock subscriptions
     */
    public List<RestockNotificationDTO> getUserRestockNotifications(Long userId) {
        return restockNotificationRepository.findByUserUserIdAndIsSentFalse(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Subscribe to restock notification
     */
    @Transactional
    public RestockNotificationDTO createRestockNotification(Long userId, Long productId, Long variantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        ProductVariant variant = null;
        if (variantId != null) {
            variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new IllegalArgumentException("Variant không tồn tại"));
        }

        // Check if already subscribed
        if (variantId != null) {
            restockNotificationRepository.findByUserUserIdAndProductProductIdAndVariantVariantIdAndIsSentFalse(
                    userId, productId, variantId).ifPresent(n -> {
                        throw new IllegalArgumentException("Bạn đã đăng ký thông báo hàng về cho sản phẩm này");
                    });
        } else {
            restockNotificationRepository.findByUserUserIdAndProductProductIdAndVariantIsNullAndIsSentFalse(
                    userId, productId).ifPresent(n -> {
                        throw new IllegalArgumentException("Bạn đã đăng ký thông báo hàng về cho sản phẩm này");
                    });
        }

        RestockNotification notification = RestockNotification.builder()
                .user(user)
                .product(product)
                .variant(variant)
                .build();

        RestockNotification saved = restockNotificationRepository.save(notification);
        log.info("Created restock notification for user {} on product {}", userId, productId);
        return mapToDTO(saved);
    }

    /**
     * Unsubscribe from restock notification
     */
    @Transactional
    public void deleteRestockNotification(Long notificationId, Long userId) {
        RestockNotification notification = restockNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Restock notification không tồn tại"));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Không có quyền xóa restock notification này");
        }

        restockNotificationRepository.delete(notification);
        log.info("Deleted restock notification {} for user {}", notificationId, userId);
    }

    /**
     * Trigger restock notifications when product is restocked
     * Call this method when inventory is updated
     */
    @Async
    @Transactional
    public void triggerRestockNotifications(Long productId, Long variantId) {
        List<RestockNotification> notifications;

        if (variantId != null) {
            notifications = restockNotificationRepository.findByProductProductIdAndVariantVariantIdAndIsSentFalse(
                    productId, variantId);
        } else {
            notifications = restockNotificationRepository.findByProductProductIdAndIsSentFalse(productId);
        }

        if (notifications.isEmpty()) {
            return;
        }

        Product product = productRepository.findById(productId).orElse(null);
        String productName = product != null ? product.getName() : "Sản phẩm";

        for (RestockNotification notification : notifications) {
            try {
                notification.markAsSent();
                restockNotificationRepository.save(notification);

                // Send notification
                String message = String.format("%s đã có hàng trở lại! Hãy mua ngay trước khi hết hàng.", productName);

                notificationService.sendNotificationWithEmail(
                        notification.getUser().getUserId(),
                        NotificationType.RESTOCK,
                        "Sản phẩm đã có hàng!",
                        message,
                        "/products/" + productId);

                log.info("Sent restock notification to user {} for product {}",
                        notification.getUser().getUserId(), productId);
            } catch (Exception e) {
                log.error("Error sending restock notification {}: {}", notification.getNotificationId(),
                        e.getMessage());
            }
        }
    }

    private RestockNotificationDTO mapToDTO(RestockNotification notification) {
        String imageUrl = null;
        if (notification.getProduct().getPrimaryImage() != null) {
            imageUrl = notification.getProduct().getPrimaryImage().getImageUrl();
        }
        return RestockNotificationDTO.builder()
                .notificationId(notification.getNotificationId())
                .productId(notification.getProduct().getProductId())
                .productName(notification.getProduct().getName())
                .productImageUrl(imageUrl)
                .variantId(notification.getVariant() != null ? notification.getVariant().getVariantId() : null)
                .variantName(notification.getVariant() != null ? notification.getVariant().getVariantName() : null)
                .isSent(notification.getIsSent())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
