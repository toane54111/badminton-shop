package com.badmintonshop.dto.review;

import lombok.*;

/**
 * DTO for review image
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewImageDTO {
    
    private Long imageId;
    private String imageUrl;
    private String imageType;
}
