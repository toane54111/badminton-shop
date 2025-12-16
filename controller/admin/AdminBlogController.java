package com.badmintonshop.controller.admin;

import com.badmintonshop.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Admin page controller for blogs
 */
@Controller
@RequestMapping("/admin/blogs")
@RequiredArgsConstructor
public class AdminBlogController {

    private final BlogService blogService;

    /**
     * Display blogs management page
     * GET /admin/blogs
     */
    @GetMapping
    public String listBlogs(Model model) {
        model.addAttribute("categories", blogService.getCategories());
        return "admin/blogs";
    }
}
