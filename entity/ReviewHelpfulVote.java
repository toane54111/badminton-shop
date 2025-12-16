package com.badmintonshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity ReviewHelpfulVote - Vote review có hữu ích không
 */
@Entity
@Table(name = "review_helpful_votes", indexes = {
    @Index(name = "idx_review_votes_review", columnList = "review_id"),
    @Index(name = "idx_review_votes_user", columnList = "user_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_review_vote", columnNames = {"review_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewHelpfulVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_id")
    private Long voteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private ProductReview review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_helpful", nullable = false)
    private Boolean isHelpful; // 1=helpful, 0=not helpful

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
