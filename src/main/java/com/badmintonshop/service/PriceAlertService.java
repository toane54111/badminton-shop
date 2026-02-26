package com.badmintonshop.service;

import com.badmintonshop.dto.notification.PriceAlertDTO;
import com.badmintonshop.entity.PriceAlert;
import com.badmintonshop.entity.Product;
import com.badmintonshop.entity.User;
import com.badmintonshop.entity.enums.NotificationType;
import com.badmintonshop.repository.PriceAlertRepository;
import com.badmintonshop.repository.ProductRepository;
import com.badmintonshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PriceAlertService {

    private final PriceAlertRepository priceAlertRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Get user's price alerts
     */
    public List<PriceAlertDTO> getUserPriceAlerts(Long userId) {
        return priceAlertRepository.findByUserUserIdAndIsActiveTrue(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create price alert
     */
    @Transactional
    public PriceAlertDTO createPriceAlert(Long userId, Long productId, BigDecimal targetPrice) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        // Check if alert already exists
        priceAlertRepository.findByUserUserIdAndProductProductIdAndIsActiveTrue(userId, productId)
                .ifPresent(alert -> {
                    throw new IllegalArgumentException("Bạn đã đăng ký thông báo giá cho sản phẩm này");
                });

        PriceAlert alert = PriceAlert.builder()
                .user(user)
                .product(product)
                .targetPrice(targetPrice)
                .currentPrice(product.getBasePrice())
                .build();

        PriceAlert saved = priceAlertRepository.save(alert);
        log.info("Created price alert for user {} on product {}: target={}", userId, productId, targetPrice);
        return mapToDTO(saved);
    }

    /**
     * Delete price alert
     */
    @Transactional
    public void deletePriceAlert(Long alertId, Long userId) {
        PriceAlert alert = priceAlertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Price alert không tồn tại"));

        if (!alert.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Không có quyền xóa price alert này");
        }

        alert.setIsActive(false);
        priceAlertRepository.save(alert);
        log.info("Deleted price alert {} for user {}", alertId, userId);
    }

    /**
     * Trigger price alerts when product price drops
     * Call this method when product price is updated
     */
    @Async
    @Transactional
    public void triggerPriceAlerts(Long productId, BigDecimal newPrice) {
        List<PriceAlert> alertsToTrigger = priceAlertRepository.findAlertsThatShouldTrigger(productId, newPrice);

        if (alertsToTrigger.isEmpty()) {
            return;
        }

        Product product = productRepository.findById(productId).orElse(null);
        String productName = product != null ? product.getName() : "Sản phẩm";

        for (PriceAlert alert : alertsToTrigger) {
            try {
                alert.trigger();
                priceAlertRepository.save(alert);

                // Send notification
                String message = String.format("Giá %s đã giảm xuống %s₫! Mục tiêu của bạn là %s₫",
                        productName, newPrice.toString(), alert.getTargetPrice().toString());

                notificationService.sendNotificationWithEmail(
                        alert.getUser().getUserId(),
                        NotificationType.PRICE_DROP,
                        "Giá sản phẩm đã giảm!",
                        message,
                        "/products/" + productId);

                log.info("Triggered price alert for user {} on product {}",
                        alert.getUser().getUserId(), productId);
            } catch (Exception e) {
                log.error("Error triggering price alert {}: {}", alert.getAlertId(), e.getMessage());
            }
        }
    }

    private PriceAlertDTO mapToDTO(PriceAlert alert) {
        String imageUrl = null;
        if (alert.getProduct().getPrimaryImage() != null) {
            imageUrl = alert.getProduct().getPrimaryImage().getImageUrl();
        }
        return PriceAlertDTO.builder()
                .alertId(alert.getAlertId())
                .productId(alert.getProduct().getProductId())
                .productName(alert.getProduct().getName())
                .productImageUrl(imageUrl)
                .targetPrice(alert.getTargetPrice())
                .currentPrice(alert.getCurrentPrice())
                .isActive(alert.getIsActive())
                .isTriggered(alert.getIsTriggered())
                .triggeredAt(alert.getTriggeredAt())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
