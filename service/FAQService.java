package com.badmintonshop.service;

import com.badmintonshop.dto.faq.FAQDTO;
import com.badmintonshop.entity.FAQ;
import com.badmintonshop.repository.FAQRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FAQService {

    private final FAQRepository faqRepository;

    // ===== PUBLIC METHODS =====

    /**
     * Get all active FAQs
     */
    public List<FAQDTO> getAllActiveFAQs() {
        return faqRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get FAQs by category
     */
    public List<FAQDTO> getFAQsByCategory(String category) {
        return faqRepository.findByCategoryAndIsActiveTrueOrderByDisplayOrderAsc(category).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search FAQs by keyword
     */
    public List<FAQDTO> searchFAQs(String keyword) {
        return faqRepository.searchActiveFAQs(keyword).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get distinct FAQ categories
     */
    public List<String> getCategories() {
        return faqRepository.findDistinctCategories();
    }

    /**
     * Increment FAQ view count
     */
    @Transactional
    public void incrementViewCount(Long faqId) {
        faqRepository.incrementViewCount(faqId);
    }

    // ===== ADMIN METHODS =====

    /**
     * Get all FAQs for admin
     */
    public Page<FAQDTO> getAllFAQsForAdmin(Pageable pageable) {
        return faqRepository.findAllByOrderByDisplayOrderAsc(pageable).map(this::mapToDTO);
    }

    /**
     * Get FAQ by ID
     */
    public Optional<FAQDTO> getFAQById(Long id) {
        return faqRepository.findById(id).map(this::mapToDTO);
    }

    /**
     * Create FAQ
     */
    @Transactional
    public FAQDTO createFAQ(FAQDTO dto) {
        FAQ faq = FAQ.builder()
                .category(dto.getCategory())
                .question(dto.getQuestion())
                .answer(dto.getAnswer())
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        FAQ saved = faqRepository.save(faq);
        log.info("Created FAQ: {}", saved.getQuestion());
        return mapToDTO(saved);
    }

    /**
     * Update FAQ
     */
    @Transactional
    public FAQDTO updateFAQ(Long id, FAQDTO dto) {
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FAQ không tồn tại: " + id));

        faq.setCategory(dto.getCategory());
        faq.setQuestion(dto.getQuestion());
        faq.setAnswer(dto.getAnswer());
        faq.setDisplayOrder(dto.getDisplayOrder());
        faq.setIsActive(dto.getIsActive());

        FAQ updated = faqRepository.save(faq);
        log.info("Updated FAQ: {}", updated.getQuestion());
        return mapToDTO(updated);
    }

    /**
     * Delete FAQ (soft delete)
     */
    @Transactional
    public void deleteFAQ(Long id) {
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FAQ không tồn tại: " + id));

        faq.setDeletedAt(LocalDateTime.now());
        faqRepository.save(faq);
        log.info("Deleted FAQ: {}", faq.getQuestion());
    }

    /**
     * Toggle FAQ active status
     */
    @Transactional
    public FAQDTO toggleActive(Long id) {
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FAQ không tồn tại: " + id));

        faq.setIsActive(!faq.getIsActive());
        FAQ saved = faqRepository.save(faq);
        log.info("Toggled FAQ active status: {} -> {}", faq.getQuestion(), faq.getIsActive());
        return mapToDTO(saved);
    }

    // ===== MAPPING =====

    private FAQDTO mapToDTO(FAQ faq) {
        return FAQDTO.builder()
                .faqId(faq.getFaqId())
                .category(faq.getCategory())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .displayOrder(faq.getDisplayOrder())
                .viewCount(faq.getViewCount())
                .isActive(faq.getIsActive())
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }
}
