package com.badmintonshop.controller;

import com.badmintonshop.dto.blog.BlogPostDTO;
import com.badmintonshop.dto.blog.BlogPostListDTO;
import com.badmintonshop.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Public controller for blog pages
 */
@Controller
@RequestMapping("/blogs")
@RequiredArgsConstructor
public class BlogPageController {

    private final BlogService blogService;

    /**
     * Display blog list page
     * GET /blogs
     */
    @GetMapping
    public String listBlogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<BlogPostListDTO> blogs;

        if (keyword != null && !keyword.trim().isEmpty()) {
            blogs = blogService.searchBlogs(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else if (category != null && !category.trim().isEmpty()) {
            blogs = blogService.getBlogsByCategory(category, pageable);
            model.addAttribute("category", category);
        } else {
            blogs = blogService.getAllPublishedBlogs(pageable);
        }

        model.addAttribute("blogs", blogs);
        model.addAttribute("categories", blogService.getCategories());
        model.addAttribute("popularBlogs", blogService.getPopularBlogs(PageRequest.of(0, 5)).getContent());

        return "shop/blogs";
    }

    /**
     * Display single blog post
     * GET /blogs/{slug}
     */
    @GetMapping("/{slug}")
    public String viewBlog(@PathVariable String slug, Model model) {
        Optional<BlogPostDTO> blogOpt = blogService.getBlogBySlug(slug);

        if (blogOpt.isEmpty()) {
            return "redirect:/blogs";
        }

        BlogPostDTO blog = blogOpt.get();
        model.addAttribute("blog", blog);
        model.addAttribute("popularBlogs", blogService.getPopularBlogs(PageRequest.of(0, 5)).getContent());
        model.addAttribute("categories", blogService.getCategories());

        return "shop/blog-detail";
    }
}
