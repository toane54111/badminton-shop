package com.badmintonshop.controller;

import com.badmintonshop.service.BannerService;
import com.badmintonshop.service.CategoryService;
import com.badmintonshop.service.ProductService;
import com.badmintonshop.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home Controller - Handle home and public pages
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final BannerService bannerService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final PromotionService promotionService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        // Load active banners
        model.addAttribute("banners", bannerService.getAllDisplayableBanners());
        
        // Load featured products
        model.addAttribute("featuredProducts", productService.getFeaturedProducts(8));
        
        // Load best sellers
        model.addAttribute("bestSellers", productService.getBestSellers(8));
        
        // Load new arrivals
        model.addAttribute("newArrivals", productService.getNewArrivals(8));
        
        // Load categories (root/parent only)
        model.addAttribute("categories", categoryService.getRootCategories());
        
        // Load active promotions
        model.addAttribute("promotions", promotionService.getActivePromotions());
        
        return "home";
    }
}
