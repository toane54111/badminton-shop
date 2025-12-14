package com.badmintonshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity PriceAlert - Cảnh báo khi giá giảm
 */
@Entity
@Table(name = "price_alerts", indexes = {
    @Index(name = "idx_price_alerts_user", columnList = "user_id"),
    @Index(name = "idx_price_alerts_product", columnList = "product_id"),
    @Index(name = "idx_price_alerts_active", columnList = "is_active"),
    @Index(name = "idx_price_alerts_triggered", columnList = "is_triggered")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long alertId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "target_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal targetPrice;

    @Column(name = "current_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_triggered")
    @Builder.Default
    private Boolean isTriggered = false;

    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Helper methods
    public boolean shouldTrigger(BigDecimal newPrice) {
        return isActive && !isTriggered && newPrice.compareTo(targetPrice) <= 0;
    }

    public void trigger() {
        this.isTriggered = true;
        this.triggeredAt = LocalDateTime.now();
        this.isActive = false;
    }
}
