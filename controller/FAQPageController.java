package com.badmintonshop.controller;

import com.badmintonshop.dto.faq.FAQDTO;
import com.badmintonshop.service.FAQService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public controller for FAQ page
 */
@Controller
@RequestMapping("/faq")
@RequiredArgsConstructor
public class FAQPageController {

    private final FAQService faqService;

    /**
     * Display FAQ page
     * GET /faq
     */
    @GetMapping
    public String showFAQ(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            Model model) {

        List<FAQDTO> faqs;

        if (keyword != null && !keyword.trim().isEmpty()) {
            faqs = faqService.searchFAQs(keyword);
            model.addAttribute("keyword", keyword);
        } else if (category != null && !category.trim().isEmpty()) {
            faqs = faqService.getFAQsByCategory(category);
            model.addAttribute("category", category);
        } else {
            faqs = faqService.getAllActiveFAQs();
        }

        model.addAttribute("faqs", faqs);
        model.addAttribute("categories", faqService.getCategories());

        return "shop/faq";
    }
}
