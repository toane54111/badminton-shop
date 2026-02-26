package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity ProductReview - Đánh giá sản phẩm
 */
@Entity
@Table(name = "product_reviews", indexes = {
    @Index(name = "idx_reviews_product", columnList = "product_id"),
    @Index(name = "idx_reviews_user", columnList = "user_id"),
    @Index(name = "idx_reviews_order_item", columnList = "order_item_id"),
    @Index(name = "idx_reviews_rating", columnList = "rating"),
    @Index(name = "idx_reviews_status", columnList = "status"),
    @Index(name = "idx_reviews_verified", columnList = "is_verified_purchase"),
    @Index(name = "idx_reviews_created", columnList = "created_at"),
    @Index(name = "idx_reviews_deleted", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
public class ProductReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5 stars

    @Column(name = "title")
    private String title;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    // Verification
    @Column(name = "is_verified_purchase")
    @Builder.Default
    private Boolean isVerifiedPurchase = false;

    // Engagement
    @Column(name = "helpful_count")
    @Builder.Default
    private Integer helpfulCount = 0;

    // Admin Moderation
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(name = "moderation_notes", columnDefinition = "TEXT")
    private String moderationNotes;

    // Relationships
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewHelpfulVote> votes = new ArrayList<>();

    // Helper methods
    public void approve() {
        this.status = ReviewStatus.APPROVED;
    }

    public void reject(String reason) {
        this.status = ReviewStatus.REJECTED;
        this.moderationNotes = reason;
    }

    public void incrementHelpfulCount() {
        this.helpfulCount++;
    }

    public void addImage(ReviewImage image) {
        images.add(image);
        image.setReview(this);
    }
}
