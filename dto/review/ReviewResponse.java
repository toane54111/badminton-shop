package com.badmintonshop.dto.review;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for review details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    
    private Long reviewId;
    private Long productId;
    private String productName;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Integer rating;
    private String title;
    private String comment;
    private Boolean isVerifiedPurchase;
    private Integer helpfulCount;
    private String status;
    private List<ReviewImageDTO> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // For current user context
    private Boolean userVotedHelpful;
    private Boolean isOwnReview;
}
