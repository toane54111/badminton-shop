package com.badmintonshop.repository;

import com.badmintonshop.entity.FAQ;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FAQRepository extends JpaRepository<FAQ, Long> {

    // Find all active FAQs ordered by display order
    List<FAQ> findByIsActiveTrueOrderByDisplayOrderAsc();

    // Find by category (active only)
    List<FAQ> findByCategoryAndIsActiveTrueOrderByDisplayOrderAsc(String category);

    // Find all FAQs for admin
    Page<FAQ> findAllByOrderByDisplayOrderAsc(Pageable pageable);

    // Find by category
    Page<FAQ> findByCategoryOrderByDisplayOrderAsc(String category, Pageable pageable);

    // Get distinct categories
    @Query("SELECT DISTINCT f.category FROM FAQ f WHERE f.category IS NOT NULL AND f.isActive = true ORDER BY f.category")
    List<String> findDistinctCategories();

    // Search FAQs by keyword
    @Query("SELECT f FROM FAQ f WHERE f.isActive = true AND (LOWER(f.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.answer) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY f.displayOrder ASC")
    List<FAQ> searchActiveFAQs(@Param("keyword") String keyword);

    // Increment view count
    @Modifying
    @Query("UPDATE FAQ f SET f.viewCount = f.viewCount + 1 WHERE f.faqId = :faqId")
    void incrementViewCount(@Param("faqId") Long faqId);

    // Find deleted FAQs (for trash)
    @Query("SELECT f FROM FAQ f WHERE f.deletedAt IS NOT NULL ORDER BY f.deletedAt DESC")
    Page<FAQ> findDeletedFAQs(Pageable pageable);

    // Find by ID including deleted
    @Query("SELECT f FROM FAQ f WHERE f.faqId = :id")
    Optional<FAQ> findByIdIncludingDeleted(@Param("id") Long id);
}
