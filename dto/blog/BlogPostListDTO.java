package com.badmintonshop.dto.blog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Simplified DTO for blog listing (without full content)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostListDTO {
    private Long postId;
    private String title;
    private String slug;
    private String excerpt;
    private String featuredImage;
    private String category;
    private String authorName;
    private Integer viewCount;
    private LocalDateTime publishedAt;
}
