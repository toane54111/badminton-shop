package com.badmintonshop.repository;

import com.badmintonshop.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    // Find images by review
    List<ReviewImage> findByReviewReviewId(Long reviewId);
    
    // Delete all images by review
    void deleteByReviewReviewId(Long reviewId);
    
    // Count images by review
    long countByReviewReviewId(Long reviewId);
}
