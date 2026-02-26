package com.badmintonshop.repository;

import com.badmintonshop.entity.ReviewHelpfulVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewHelpfulVoteRepository extends JpaRepository<ReviewHelpfulVote, Long> {

    // Find vote by review and user
    Optional<ReviewHelpfulVote> findByReviewReviewIdAndUserUserId(Long reviewId, Long userId);
    
    // Check if user already voted
    boolean existsByReviewReviewIdAndUserUserId(Long reviewId, Long userId);
    
    // Count helpful votes for a review
    long countByReviewReviewIdAndIsHelpfulTrue(Long reviewId);
    
    // Delete vote by review and user
    void deleteByReviewReviewIdAndUserUserId(Long reviewId, Long userId);
}
