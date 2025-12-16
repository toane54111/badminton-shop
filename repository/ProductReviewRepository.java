package com.badmintonshop.repository;

import com.badmintonshop.entity.ProductReview;
import com.badmintonshop.entity.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    // Find reviews by product with pagination
    Page<ProductReview> findByProductProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable);
    
    // Find all reviews by product (for admin)
    Page<ProductReview> findByProductProductId(Long productId, Pageable pageable);
    
    // Find reviews by user
    Page<ProductReview> findByUserUserId(Long userId, Pageable pageable);
    
    // Check if user already reviewed this product
    boolean existsByProductProductIdAndUserUserId(Long productId, Long userId);
    
    // Find specific review by product and user
    Optional<ProductReview> findByProductProductIdAndUserUserId(Long productId, Long userId);
    
    // Count reviews by product and status
    long countByProductProductIdAndStatus(Long productId, ReviewStatus status);
    
    // Get rating statistics for a product
    @Query("SELECT COUNT(r), AVG(r.rating) FROM ProductReview r WHERE r.product.productId = :productId AND r.status = 'APPROVED'")
    Object[] getProductRatingStats(@Param("productId") Long productId);
    
    // Get rating distribution for a product
    @Query("SELECT r.rating, COUNT(r) FROM ProductReview r WHERE r.product.productId = :productId AND r.status = 'APPROVED' GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistribution(@Param("productId") Long productId);
    
    // Find reviews pending moderation
    Page<ProductReview> findByStatus(ReviewStatus status, Pageable pageable);
    
    // Find reviews with images
    @Query("SELECT DISTINCT r FROM ProductReview r LEFT JOIN FETCH r.images WHERE r.product.productId = :productId AND r.status = 'APPROVED'")
    List<ProductReview> findReviewsWithImages(@Param("productId") Long productId);
    
    // Count reviews by status
    long countByStatus(ReviewStatus status);
}
