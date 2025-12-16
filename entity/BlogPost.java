package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.BlogStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

/**
 * Entity BlogPost - Blog/Bài viết - hướng dẫn, review chuyên môn
 */
@Entity
@Table(name = "blog_posts", indexes = {
    @Index(name = "idx_blog_posts_category", columnList = "category"),
    @Index(name = "idx_blog_posts_author", columnList = "author_id"),
    @Index(name = "idx_blog_posts_status", columnList = "status"),
    @Index(name = "idx_blog_posts_published", columnList = "is_published"),
    @Index(name = "idx_blog_posts_published_at", columnList = "published_at"),
    @Index(name = "idx_blog_posts_view_count", columnList = "view_count"),
    @Index(name = "idx_blog_posts_deleted", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
public class BlogPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "excerpt", columnDefinition = "TEXT")
    private String excerpt;

    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "featured_image", length = 500)
    private String featuredImage;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "tags", columnDefinition = "JSON")
    private String tags; // Array of tags

    // SEO
    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;

    // Author
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Staff author;

    // Stats
    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private BlogStatus status = BlogStatus.DRAFT;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean isPublished = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // Helper methods
    public void publish() {
        this.status = BlogStatus.PUBLISHED;
        this.isPublished = true;
        this.publishedAt = LocalDateTime.now();
    }

    public void archive() {
        this.status = BlogStatus.ARCHIVED;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}
