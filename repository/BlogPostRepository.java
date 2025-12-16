package com.badmintonshop.repository;

import com.badmintonshop.entity.BlogPost;
import com.badmintonshop.entity.enums.BlogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long>, JpaSpecificationExecutor<BlogPost> {

    // Find by slug
    Optional<BlogPost> findBySlug(String slug);

    // Find published blogs
    Page<BlogPost> findByIsPublishedTrueOrderByPublishedAtDesc(Pageable pageable);

    // Find by category (published only)
    Page<BlogPost> findByCategoryAndIsPublishedTrueOrderByPublishedAtDesc(String category, Pageable pageable);

    // Find by status
    Page<BlogPost> findByStatus(BlogStatus status, Pageable pageable);

    // Search by keyword in title and content
    @Query("SELECT b FROM BlogPost b WHERE b.isPublished = true AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY b.publishedAt DESC")
    Page<BlogPost> searchPublishedBlogs(@Param("keyword") String keyword, Pageable pageable);

    // Get distinct categories
    @Query("SELECT DISTINCT b.category FROM BlogPost b WHERE b.category IS NOT NULL AND b.isPublished = true ORDER BY b.category")
    List<String> findDistinctCategories();

    // Find popular blogs (by view count)
    @Query("SELECT b FROM BlogPost b WHERE b.isPublished = true ORDER BY b.viewCount DESC")
    Page<BlogPost> findPopularBlogs(Pageable pageable);

    // Increment view count
    @Modifying
    @Query("UPDATE BlogPost b SET b.viewCount = b.viewCount + 1 WHERE b.postId = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    // Find deleted blogs (for trash)
    @Query("SELECT b FROM BlogPost b WHERE b.deletedAt IS NOT NULL ORDER BY b.deletedAt DESC")
    Page<BlogPost> findDeletedBlogs(Pageable pageable);

    // Find by ID including deleted
    @Query("SELECT b FROM BlogPost b WHERE b.postId = :id")
    Optional<BlogPost> findByIdIncludingDeleted(@Param("id") Long id);

    // Check if slug exists
    boolean existsBySlug(String slug);
}
