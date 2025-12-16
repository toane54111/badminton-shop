package com.badmintonshop.controller.api;

import com.badmintonshop.dto.blog.BlogPostDTO;
import com.badmintonshop.dto.blog.BlogPostListDTO;
import com.badmintonshop.service.BlogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public API for blog posts
 */
@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
@Slf4j
public class BlogController {

    private final BlogService blogService;

    /**
     * Get published blogs
     * GET /api/blogs
     */
    @GetMapping
    public ResponseEntity<Page<BlogPostListDTO>> getBlogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<BlogPostListDTO> blogs;
        if (keyword != null && !keyword.trim().isEmpty()) {
            blogs = blogService.searchBlogs(keyword, pageable);
        } else {
            blogs = blogService.getAllPublishedBlogs(pageable);
        }

        return ResponseEntity.ok(blogs);
    }

    /**
     * Get blog by slug
     * GET /api/blogs/{slug}
     */
    @GetMapping("/{slug}")
    public ResponseEntity<BlogPostDTO> getBlogBySlug(@PathVariable String slug) {
        return blogService.getBlogBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get blogs by category
     * GET /api/blogs/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<BlogPostListDTO>> getBlogsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<BlogPostListDTO> blogs = blogService.getBlogsByCategory(category, pageable);
        return ResponseEntity.ok(blogs);
    }

    /**
     * Get popular blogs
     * GET /api/blogs/popular
     */
    @GetMapping("/popular")
    public ResponseEntity<Page<BlogPostListDTO>> getPopularBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<BlogPostListDTO> blogs = blogService.getPopularBlogs(pageable);
        return ResponseEntity.ok(blogs);
    }

    /**
     * Get blog categories
     * GET /api/blogs/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(blogService.getCategories());
    }
}
