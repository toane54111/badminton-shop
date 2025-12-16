package com.badmintonshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity CouponUsage - Lịch sử sử dụng coupon
 */
@Entity
@Table(name = "coupon_usage", indexes = {
    @Index(name = "idx_coupon_usage_coupon", columnList = "coupon_id"),
    @Index(name = "idx_coupon_usage_user", columnList = "user_id"),
    @Index(name = "idx_coupon_usage_order", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    private Long usageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "used_at")
    @Builder.Default
    private LocalDateTime usedAt = LocalDateTime.now();
}
