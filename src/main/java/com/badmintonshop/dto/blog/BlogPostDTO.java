package com.badmintonshop.dto.blog;

import com.badmintonshop.entity.enums.BlogStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostDTO {
    private Long postId;
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private String featuredImage;
    private String category;
    private String tags; // JSON array string

    // SEO fields
    private String metaTitle;
    private String metaDescription;

    // Author info
    private Long authorId;
    private String authorName;

    // Stats
    private Integer viewCount;

    // Status
    private BlogStatus status;
    private Boolean isPublished;
    private LocalDateTime publishedAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
