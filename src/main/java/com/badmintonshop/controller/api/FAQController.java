package com.badmintonshop.controller.api;

import com.badmintonshop.dto.faq.FAQDTO;
import com.badmintonshop.service.FAQService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public API for FAQs
 */
@RestController
@RequestMapping("/api/faq")
@RequiredArgsConstructor
@Slf4j
public class FAQController {

    private final FAQService faqService;

    /**
     * Get all active FAQs
     * GET /api/faq
     */
    @GetMapping
    public ResponseEntity<List<FAQDTO>> getFAQs(
            @RequestParam(required = false) String keyword) {

        List<FAQDTO> faqs;
        if (keyword != null && !keyword.trim().isEmpty()) {
            faqs = faqService.searchFAQs(keyword);
        } else {
            faqs = faqService.getAllActiveFAQs();
        }

        return ResponseEntity.ok(faqs);
    }

    /**
     * Get FAQs by category
     * GET /api/faq/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<FAQDTO>> getFAQsByCategory(@PathVariable String category) {
        List<FAQDTO> faqs = faqService.getFAQsByCategory(category);
        return ResponseEntity.ok(faqs);
    }

    /**
     * Get FAQ categories
     * GET /api/faq/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(faqService.getCategories());
    }

    /**
     * Increment FAQ view count (when user views an FAQ)
     * POST /api/faq/{id}/view
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Long id) {
        faqService.incrementViewCount(id);
        return ResponseEntity.ok().build();
    }
}
