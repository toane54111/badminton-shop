package com.badmintonshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity ProductView - Lượt xem sản phẩm
 */
@Entity
@Table(name = "product_views", indexes = {
    @Index(name = "idx_product_views_product", columnList = "product_id"),
    @Index(name = "idx_product_views_user", columnList = "user_id"),
    @Index(name = "idx_product_views_session", columnList = "session_id"),
    @Index(name = "idx_product_views_viewed", columnList = "viewed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_id")
    private Long viewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "referrer", columnDefinition = "TEXT")
    private String referrer;

    @Column(name = "viewed_at")
    @Builder.Default
    private LocalDateTime viewedAt = LocalDateTime.now();
}
