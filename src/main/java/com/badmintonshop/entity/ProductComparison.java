package com.badmintonshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity ProductComparison - So sánh sản phẩm (chủ yếu vợt)
 */
@Entity
@Table(name = "product_comparisons", indexes = {
    @Index(name = "idx_comparisons_user", columnList = "user_id"),
    @Index(name = "idx_comparisons_session", columnList = "session_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductComparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comparison_id")
    private Long comparisonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "product_ids", columnDefinition = "JSON")
    private String productIds; // Array of product IDs to compare

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
