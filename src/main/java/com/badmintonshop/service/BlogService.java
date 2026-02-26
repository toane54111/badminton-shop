package com.badmintonshop.service;

import com.badmintonshop.dto.blog.BlogPostDTO;
import com.badmintonshop.dto.blog.BlogPostListDTO;
import com.badmintonshop.entity.BlogPost;
import com.badmintonshop.entity.Staff;
import com.badmintonshop.entity.enums.BlogStatus;
import com.badmintonshop.repository.BlogPostRepository;
import com.badmintonshop.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BlogService {

    private final BlogPostRepository blogPostRepository;
    private final StaffRepository staffRepository;

    // ===== PUBLIC METHODS =====

    /**
     * Get all published blogs with pagination
     */
    public Page<BlogPostListDTO> getAllPublishedBlogs(Pageable pageable) {
        return blogPostRepository.findByIsPublishedTrueOrderByPublishedAtDesc(pageable)
                .map(this::mapToListDTO);
    }

    /**
     * Get blog by slug and increment view count
     */
    @Transactional
    public Optional<BlogPostDTO> getBlogBySlug(String slug) {
        Optional<BlogPost> blogOpt = blogPostRepository.findBySlug(slug);

        if (blogOpt.isPresent() && blogOpt.get().getIsPublished()) {
            blogPostRepository.incrementViewCount(blogOpt.get().getPostId());
            return Optional.of(mapToDTO(blogOpt.get()));
        }

        return Optional.empty();
    }

    /**
     * Get blogs by category
     */
    public Page<BlogPostListDTO> getBlogsByCategory(String category, Pageable pageable) {
        return blogPostRepository.findByCategoryAndIsPublishedTrueOrderByPublishedAtDesc(category, pageable)
                .map(this::mapToListDTO);
    }

    /**
     * Search published blogs
     */
    public Page<BlogPostListDTO> searchBlogs(String keyword, Pageable pageable) {
        return blogPostRepository.searchPublishedBlogs(keyword, pageable)
                .map(this::mapToListDTO);
    }

    /**
     * Get distinct blog categories
     */
    public List<String> getCategories() {
        return blogPostRepository.findDistinctCategories();
    }

    /**
     * Get popular blogs
     */
    public Page<BlogPostListDTO> getPopularBlogs(Pageable pageable) {
        return blogPostRepository.findPopularBlogs(pageable)
                .map(this::mapToListDTO);
    }

    // ===== ADMIN METHODS =====

    /**
     * Get all blogs for admin
     */
    public Page<BlogPostDTO> getAllBlogsForAdmin(Pageable pageable) {
        return blogPostRepository.findAll(pageable).map(this::mapToDTO);
    }

    /**
     * Get blog by ID for admin
     */
    public Optional<BlogPostDTO> getBlogById(Long id) {
        return blogPostRepository.findById(id).map(this::mapToDTO);
    }

    /**
     * Create new blog post
     */
    @Transactional
    public BlogPostDTO createBlog(BlogPostDTO dto, Long authorId) {
        // Check slug uniqueness
        if (blogPostRepository.existsBySlug(dto.getSlug())) {
            throw new IllegalArgumentException("Slug đã tồn tại: " + dto.getSlug());
        }

        Staff author = staffRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Staff không tồn tại"));

        BlogPost blog = BlogPost.builder()
                .title(dto.getTitle())
                .slug(dto.getSlug())
                .excerpt(dto.getExcerpt())
                .content(dto.getContent())
                .featuredImage(dto.getFeaturedImage())
                .category(dto.getCategory())
                .tags(dto.getTags())
                .metaTitle(dto.getMetaTitle())
                .metaDescription(dto.getMetaDescription())
                .author(author)
                .status(dto.getStatus() != null ? dto.getStatus() : BlogStatus.DRAFT)
                .build();

        // Auto-publish if status is PUBLISHED
        if (dto.getStatus() == BlogStatus.PUBLISHED) {
            blog.publish();
        }

        BlogPost saved = blogPostRepository.save(blog);
        log.info("Created blog post: {} by author {}", saved.getTitle(), authorId);
        return mapToDTO(saved);
    }

    /**
     * Update blog post
     */
    @Transactional
    public BlogPostDTO updateBlog(Long id, BlogPostDTO dto) {
        BlogPost blog = blogPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Blog không tồn tại: " + id));

        // Check slug uniqueness if changed
        if (!blog.getSlug().equals(dto.getSlug()) && blogPostRepository.existsBySlug(dto.getSlug())) {
            throw new IllegalArgumentException("Slug đã tồn tại: " + dto.getSlug());
        }

        blog.setTitle(dto.getTitle());
        blog.setSlug(dto.getSlug());
        blog.setExcerpt(dto.getExcerpt());
        blog.setContent(dto.getContent());
        blog.setFeaturedImage(dto.getFeaturedImage());
        blog.setCategory(dto.getCategory());
        blog.setTags(dto.getTags());
        blog.setMetaTitle(dto.getMetaTitle());
        blog.setMetaDescription(dto.getMetaDescription());

        // Handle status change
        if (dto.getStatus() != null && dto.getStatus() != blog.getStatus()) {
            blog.setStatus(dto.getStatus());
            if (dto.getStatus() == BlogStatus.PUBLISHED && !blog.getIsPublished()) {
                blog.publish();
            }
        }

        BlogPost updated = blogPostRepository.save(blog);
        log.info("Updated blog post: {}", updated.getTitle());
        return mapToDTO(updated);
    }

    /**
     * Delete blog post (soft delete)
     */
    @Transactional
    public void deleteBlog(Long id) {
        BlogPost blog = blogPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Blog không tồn tại: " + id));

        blog.setDeletedAt(LocalDateTime.now());
        blogPostRepository.save(blog);
        log.info("Deleted blog post: {}", blog.getTitle());
    }

    /**
     * Publish blog post
     */
    @Transactional
    public BlogPostDTO publishBlog(Long id) {
        BlogPost blog = blogPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Blog không tồn tại: " + id));

        blog.publish();
        BlogPost saved = blogPostRepository.save(blog);
        log.info("Published blog post: {}", blog.getTitle());
        return mapToDTO(saved);
    }

    // ===== MAPPING =====

    private BlogPostDTO mapToDTO(BlogPost blog) {
        return BlogPostDTO.builder()
                .postId(blog.getPostId())
                .title(blog.getTitle())
                .slug(blog.getSlug())
                .excerpt(blog.getExcerpt())
                .content(blog.getContent())
                .featuredImage(blog.getFeaturedImage())
                .category(blog.getCategory())
                .tags(blog.getTags())
                .metaTitle(blog.getMetaTitle())
                .metaDescription(blog.getMetaDescription())
                .authorId(blog.getAuthor().getStaffId())
                .authorName(blog.getAuthor().getFullName())
                .viewCount(blog.getViewCount())
                .status(blog.getStatus())
                .isPublished(blog.getIsPublished())
                .publishedAt(blog.getPublishedAt())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .build();
    }

    private BlogPostListDTO mapToListDTO(BlogPost blog) {
        return BlogPostListDTO.builder()
                .postId(blog.getPostId())
                .title(blog.getTitle())
                .slug(blog.getSlug())
                .excerpt(blog.getExcerpt())
                .featuredImage(blog.getFeaturedImage())
                .category(blog.getCategory())
                .authorName(blog.getAuthor().getFullName())
                .viewCount(blog.getViewCount())
                .publishedAt(blog.getPublishedAt())
                .build();
    }
}
