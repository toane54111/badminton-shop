package com.badmintonshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity RestockNotification - Thông báo khi hàng về (cho vợt hot)
 */
@Entity
@Table(name = "restock_notifications", indexes = {
    @Index(name = "idx_restock_notif_user", columnList = "user_id"),
    @Index(name = "idx_restock_notif_product", columnList = "product_id"),
    @Index(name = "idx_restock_notif_variant", columnList = "variant_id"),
    @Index(name = "idx_restock_notif_sent", columnList = "is_sent")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestockNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Column(name = "is_sent")
    @Builder.Default
    private Boolean isSent = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Helper methods
    public void markAsSent() {
        this.isSent = true;
        this.sentAt = LocalDateTime.now();
    }
}
