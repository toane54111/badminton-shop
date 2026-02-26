package com.badmintonshop.service;

import com.badmintonshop.dto.review.*;
import com.badmintonshop.entity.*;
import com.badmintonshop.entity.enums.ImageType;
import com.badmintonshop.entity.enums.ReviewStatus;
import com.badmintonshop.exception.ResourceNotFoundException;
import com.badmintonshop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ReviewImageRepository imageRepository;
    private final ReviewHelpfulVoteRepository voteRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Get approved reviews for a product
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Long currentUserId, Pageable pageable) {
        Page<ProductReview> reviews = reviewRepository.findByProductProductIdAndStatus(
                productId, ReviewStatus.APPROVED, pageable);
        
        return reviews.map(review -> mapToResponse(review, currentUserId));
    }

    /**
     * Get all reviews for a product (for admin)
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllProductReviews(Long productId, Pageable pageable) {
        Page<ProductReview> reviews = reviewRepository.findByProductProductId(productId, pageable);
        return reviews.map(review -> mapToResponse(review, null));
    }

    /**
     * Get reviews by user
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getUserReviews(Long userId, Pageable pageable) {
        Page<ProductReview> reviews = reviewRepository.findByUserUserId(userId, pageable);
        return reviews.map(review -> mapToResponse(review, userId));
    }

    /**
     * Get rating summary for a product
     */
    @Transactional(readOnly = true)
    public RatingSummaryDTO getRatingSummary(Long productId) {
        Object[] stats = reviewRepository.getProductRatingStats(productId);
        List<Object[]> distribution = reviewRepository.getRatingDistribution(productId);

        // Handle different Number types that JPA might return
        long totalReviews = 0L;
        double avgRating = 0.0;
        
        if (stats != null && stats.length >= 2) {
            if (stats[0] != null) {
                totalReviews = ((Number) stats[0]).longValue();
            }
            if (stats[1] != null) {
                avgRating = ((Number) stats[1]).doubleValue();
            }
        }

        Map<Integer, Long> ratingDist = new HashMap<>();
        Map<Integer, Double> ratingPercent = new HashMap<>();
        
        // Initialize all ratings with 0
        for (int i = 1; i <= 5; i++) {
            ratingDist.put(i, 0L);
            ratingPercent.put(i, 0.0);
        }

        // Fill in actual values
        for (Object[] row : distribution) {
            Integer rating = ((Number) row[0]).intValue();
            Long count = ((Number) row[1]).longValue();
            ratingDist.put(rating, count);
            if (totalReviews > 0) {
                ratingPercent.put(rating, (count * 100.0) / totalReviews);
            }
        }

        return RatingSummaryDTO.builder()
                .productId(productId)
                .averageRating(BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP))
                .totalReviews((int) totalReviews)
                .ratingDistribution(ratingDist)
                .ratingPercentages(ratingPercent)
                .build();
    }

    /**
     * Create a new review
     */
    public ReviewResponse createReview(Long productId, Long userId, ReviewRequest request) {
        // Check if user already reviewed this product
        if (reviewRepository.existsByProductProductIdAndUserUserId(productId, userId)) {
            throw new IllegalStateException("You have already reviewed this product");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // TODO: Check if user has purchased this product (verified purchase)
        boolean isVerifiedPurchase = false;

        ProductReview review = ProductReview.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .isVerifiedPurchase(isVerifiedPurchase)
                .status(ReviewStatus.PENDING) // Needs moderation
                .helpfulCount(0)
                .build();

        review = reviewRepository.save(review);

        // Add images if provided
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (String imageUrl : request.getImageUrls()) {
                ReviewImage image = ReviewImage.builder()
                        .review(review)
                        .imageUrl(imageUrl)
                        .imageType(ImageType.PHOTO)
                        .build();
                review.addImage(image);
            }
            review = reviewRepository.save(review);
        }

        log.info("Created review {} for product {} by user {}", review.getReviewId(), productId, userId);
        return mapToResponse(review, userId);
    }

    /**
     * Update an existing review
     */
    public ReviewResponse updateReview(Long reviewId, Long userId, ReviewRequest request) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUser().getUserId().equals(userId)) {
            throw new IllegalStateException("You can only edit your own reviews");
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
        review.setStatus(ReviewStatus.PENDING); // Re-moderate after edit

        // Update images if provided
        if (request.getImageUrls() != null) {
            review.getImages().clear();
            for (String imageUrl : request.getImageUrls()) {
                ReviewImage image = ReviewImage.builder()
                        .review(review)
                        .imageUrl(imageUrl)
                        .imageType(ImageType.PHOTO)
                        .build();
                review.addImage(image);
            }
        }

        review = reviewRepository.save(review);
        
        // Update product rating
        updateProductRating(review.getProduct().getProductId());
        
        log.info("Updated review {}", reviewId);
        return mapToResponse(review, userId);
    }

    /**
     * Delete a review
     */
    public void deleteReview(Long reviewId, Long userId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUser().getUserId().equals(userId)) {
            throw new IllegalStateException("You can only delete your own reviews");
        }

        Long productId = review.getProduct().getProductId();
        reviewRepository.delete(review);
        
        // Update product rating
        updateProductRating(productId);
        
        log.info("Deleted review {}", reviewId);
    }

    /**
     * Vote review as helpful or not
     */
    public void voteHelpful(Long reviewId, Long userId, boolean isHelpful) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<ReviewHelpfulVote> existingVote = voteRepository.findByReviewReviewIdAndUserUserId(reviewId, userId);

        if (existingVote.isPresent()) {
            ReviewHelpfulVote vote = existingVote.get();
            if (vote.getIsHelpful() == isHelpful) {
                // Same vote, remove it (toggle)
                voteRepository.delete(vote);
                if (isHelpful) {
                    review.setHelpfulCount(Math.max(0, review.getHelpfulCount() - 1));
                }
            } else {
                // Different vote, update it
                vote.setIsHelpful(isHelpful);
                voteRepository.save(vote);
                if (isHelpful) {
                    review.incrementHelpfulCount();
                } else {
                    review.setHelpfulCount(Math.max(0, review.getHelpfulCount() - 1));
                }
            }
        } else {
            // New vote
            ReviewHelpfulVote vote = ReviewHelpfulVote.builder()
                    .review(review)
                    .user(user)
                    .isHelpful(isHelpful)
                    .build();
            voteRepository.save(vote);
            if (isHelpful) {
                review.incrementHelpfulCount();
            }
        }

        reviewRepository.save(review);
        log.info("User {} voted {} on review {}", userId, isHelpful ? "helpful" : "not helpful", reviewId);
    }

    /**
     * Approve a review (admin)
     */
    public void approveReview(Long reviewId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        review.approve();
        reviewRepository.save(review);
        
        // Update product rating
        updateProductRating(review.getProduct().getProductId());
        
        log.info("Approved review {}", reviewId);
    }

    /**
     * Reject a review (admin)
     */
    public void rejectReview(Long reviewId, String reason) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        review.reject(reason);
        reviewRepository.save(review);
        
        log.info("Rejected review {} with reason: {}", reviewId, reason);
    }

    /**
     * Get pending reviews for moderation
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getPendingReviews(Pageable pageable) {
        Page<ProductReview> reviews = reviewRepository.findByStatus(ReviewStatus.PENDING, pageable);
        return reviews.map(review -> mapToResponse(review, null));
    }

    /**
     * Get reviews by status (for admin)
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByStatus(ReviewStatus status, Pageable pageable) {
        Page<ProductReview> reviews = reviewRepository.findByStatus(status, pageable);
        return reviews.map(review -> mapToResponse(review, null));
    }

    /**
     * Check if user can review a product
     */
    @Transactional(readOnly = true)
    public boolean canUserReview(Long productId, Long userId) {
        return !reviewRepository.existsByProductProductIdAndUserUserId(productId, userId);
    }

    /**
     * Get user's own review for a product (including pending)
     */
    @Transactional(readOnly = true)
    public ReviewResponse getUserReviewForProduct(Long productId, Long userId) {
        return reviewRepository.findByProductProductIdAndUserUserId(productId, userId)
                .map(review -> mapToResponse(review, userId))
                .orElse(null);
    }

    /**
     * Admin delete review
     */
    public void adminDeleteReview(Long reviewId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        Long productId = review.getProduct().getProductId();
        reviewRepository.delete(review);
        
        // Update product rating
        updateProductRating(productId);
        
        log.info("Admin deleted review {}", reviewId);
    }

    /**
     * Get review statistics for admin dashboard
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getReviewStats() {
        long pending = reviewRepository.countByStatus(ReviewStatus.PENDING);
        long approved = reviewRepository.countByStatus(ReviewStatus.APPROVED);
        long rejected = reviewRepository.countByStatus(ReviewStatus.REJECTED);
        long total = pending + approved + rejected;
        
        return Map.of(
                "pending", pending,
                "approved", approved,
                "rejected", rejected,
                "total", total
        );
    }

    /**
     * Update product rating average after review changes
     */
    public void updateProductRating(Long productId) {
        Object[] stats = reviewRepository.getProductRatingStats(productId);
        
        // Handle different Number types that JPA might return
        long totalReviews = 0L;
        double avgRating = 0.0;
        
        if (stats != null && stats.length >= 2) {
            if (stats[0] != null) {
                totalReviews = ((Number) stats[0]).longValue();
            }
            if (stats[1] != null) {
                avgRating = ((Number) stats[1]).doubleValue();
            }
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        product.setRatingCount((int) totalReviews);
        product.setRatingAverage(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP));
        
        productRepository.save(product);
        log.info("Updated product {} rating: avg={}, count={}", productId, avgRating, totalReviews);
    }

    // Helper method to map entity to DTO
    private ReviewResponse mapToResponse(ProductReview review, Long currentUserId) {
        List<ReviewImageDTO> images = review.getImages().stream()
                .map(img -> ReviewImageDTO.builder()
                        .imageId(img.getImageId())
                        .imageUrl(img.getImageUrl())
                        .imageType(img.getImageType().name())
                        .build())
                .collect(Collectors.toList());

        Boolean userVotedHelpful = null;
        if (currentUserId != null) {
            Optional<ReviewHelpfulVote> vote = voteRepository.findByReviewReviewIdAndUserUserId(
                    review.getReviewId(), currentUserId);
            userVotedHelpful = vote.map(ReviewHelpfulVote::getIsHelpful).orElse(null);
        }

        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .productId(review.getProduct().getProductId())
                .productName(review.getProduct().getName())
                .userId(review.getUser().getUserId())
                .userName(review.getUser().getFullName())
                .userAvatar(review.getUser().getAvatarUrl())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .helpfulCount(review.getHelpfulCount())
                .status(review.getStatus().name())
                .images(images)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .userVotedHelpful(userVotedHelpful)
                .isOwnReview(currentUserId != null && review.getUser().getUserId().equals(currentUserId))
                .build();
    }
}
