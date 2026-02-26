package com.badmintonshop.controller.api;

import com.badmintonshop.dto.review.*;
import com.badmintonshop.security.CustomOAuth2User;
import com.badmintonshop.security.CustomUserDetails;
import com.badmintonshop.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API Controller for Product Reviews
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Helper method to get current user ID from both regular and OAuth2 login
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getUserId();
            }
            if (principal instanceof CustomOAuth2User) {
                return ((CustomOAuth2User) principal).getUserId();
            }
        }
        return null;
    }

    /**
     * Get reviews for a product
     * GET /api/products/{productId}/reviews
     */
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Long currentUserId = getCurrentUserId();
        Page<ReviewResponse> reviews = reviewService.getProductReviews(productId, currentUserId, pageable);
        
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get rating summary for a product
     * GET /api/products/{productId}/rating-summary
     */
    @GetMapping("/products/{productId}/rating-summary")
    public ResponseEntity<RatingSummaryDTO> getRatingSummary(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getRatingSummary(productId));
    }

    /**
     * Create a new review
     * POST /api/products/{productId}/reviews
     */
    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<?> createReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request) {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Vui lòng đăng nhập"));
        }

        try {
            ReviewResponse review = reviewService.createReview(productId, userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi gửi đánh giá: " + e.getMessage()));
        }
    }

    /**
     * Update a review
     * PUT /api/reviews/{reviewId}
     */
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request) {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReviewResponse review = reviewService.updateReview(reviewId, userId, request);
        return ResponseEntity.ok(review);
    }

    /**
     * Delete a review
     * DELETE /api/reviews/{reviewId}
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Vote a review as helpful
     * POST /api/reviews/{reviewId}/helpful
     */
    @PostMapping("/reviews/{reviewId}/helpful")
    public ResponseEntity<Map<String, Object>> voteHelpful(
            @PathVariable Long reviewId,
            @RequestBody Map<String, Boolean> body) {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Boolean isHelpful = body.getOrDefault("isHelpful", true);
        reviewService.voteHelpful(reviewId, userId, isHelpful);
        
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Check if user can review a product
     * GET /api/products/{productId}/can-review
     */
    @GetMapping("/products/{productId}/can-review")
    public ResponseEntity<Map<String, Boolean>> canReview(@PathVariable Long productId) {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(Map.of("canReview", false, "loggedIn", false));
        }

        boolean canReview = reviewService.canUserReview(productId, userId);
        return ResponseEntity.ok(Map.of("canReview", canReview, "loggedIn", true));
    }

    /**
     * Get user's reviews
     * GET /api/reviews/my-reviews
     */
    @GetMapping("/reviews/my-reviews")
    public ResponseEntity<Page<ReviewResponse>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewResponse> reviews = reviewService.getUserReviews(userId, pageable);
        
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get user's own review for a specific product
     * GET /api/products/{productId}/my-review
     */
    @GetMapping("/products/{productId}/my-review")
    public ResponseEntity<ReviewResponse> getMyProductReview(@PathVariable Long productId) {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(null);
        }

        ReviewResponse review = reviewService.getUserReviewForProduct(productId, userId);
        return ResponseEntity.ok(review);
    }

    /**
     * Upload review images
     * POST /api/reviews/upload-images
     */
    @PostMapping("/reviews/upload-images")
    public ResponseEntity<Map<String, Object>> uploadReviewImages(
            @RequestParam("files") java.util.List<org.springframework.web.multipart.MultipartFile> files) {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            java.util.List<String> imageUrls = fileStorageService.storeReviewImages(files);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "imageUrls", imageUrls
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Không thể tải lên hình ảnh: " + e.getMessage()
            ));
        }
    }

    // Inject FileStorageService
    private final com.badmintonshop.service.FileStorageService fileStorageService;
}
