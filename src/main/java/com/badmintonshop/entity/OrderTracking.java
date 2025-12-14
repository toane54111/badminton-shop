package com.badmintonshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity OrderTracking - Theo dõi vận chuyển
 */
@Entity
@Table(name = "order_tracking", indexes = {
    @Index(name = "idx_order_tracking_order", columnList = "order_id"),
    @Index(name = "idx_order_tracking_number", columnList = "tracking_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tracking_id")
    private Long trackingId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "carrier", length = 100)
    private String carrier; // VD: Giao Hàng Nhanh, Viettel Post

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "current_status", length = 100)
    private String currentStatus;

    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;

    @Column(name = "tracking_url", length = 500)
    private String trackingUrl;

    @Column(name = "qr_code_data", columnDefinition = "TEXT")
    private String qrCodeData; // QR code cho tracking

    // Timeline events (JSON)
    @Column(name = "events", columnDefinition = "JSON")
    private String events; // Array of tracking events

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
