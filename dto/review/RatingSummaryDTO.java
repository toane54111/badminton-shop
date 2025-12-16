package com.badmintonshop.dto.review;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for product rating summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingSummaryDTO {
    
    private Long productId;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Map<Integer, Long> ratingDistribution; // rating -> count
    
    // Percentage for each rating level
    private Map<Integer, Double> ratingPercentages;
}
