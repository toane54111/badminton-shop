package com.badmintonshop.controller.admin;

import com.badmintonshop.dto.promotion.PromotionDTO;
import com.badmintonshop.entity.Staff;
import com.badmintonshop.entity.enums.ActivityAction;
import com.badmintonshop.entity.enums.PromotionType;
import com.badmintonshop.repository.StaffRepository;
import com.badmintonshop.service.AuditService;
import com.badmintonshop.service.PromotionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin REST API Controller for Promotion Management
 */
@RestController
@RequestMapping("/admin/api/promotions")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CONTENT_STAFF') or hasAuthority('promotions.view')")
public class AdminPromotionApiController {

    private final PromotionService promotionService;
    private final AuditService auditService;
    private final StaffRepository staffRepository;

    /**
     * Get all promotions with pagination and filters
     * GET /admin/api/promotions
     */
    @GetMapping
    public ResponseEntity<Page<PromotionDTO>> getAllPromotions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PromotionType type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PromotionDTO> promotions = promotionService.searchPromotions(keyword, type, status, pageable);
        return ResponseEntity.ok(promotions);
    }

    /**
     * Get promotion by ID
     * GET /admin/api/promotions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPromotionById(@PathVariable Long id) {
        return promotionService.getPromotionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new promotion
     * POST /admin/api/promotions
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CONTENT_STAFF') or hasAuthority('promotions.create')")
    public ResponseEntity<?> createPromotion(@RequestBody PromotionDTO promotionDTO, HttpServletRequest request) {
        try {
            PromotionDTO created = promotionService.createPromotion(promotionDTO);
            auditService.logActivity(getCurrentStaff(), ActivityAction.CREATE, "Promotion", 
                created.getPromotionId(), "Tạo khuyến mãi: " + created.getName(), null, created, request);
            log.info("Created promotion: {}", created.getName());
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update promotion
     * PUT /admin/api/promotions/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CONTENT_STAFF') or hasAuthority('promotions.update')")
    public ResponseEntity<?> updatePromotion(@PathVariable Long id, @RequestBody PromotionDTO promotionDTO, HttpServletRequest request) {
        try {
            PromotionDTO updated = promotionService.updatePromotion(id, promotionDTO);
            auditService.logActivity(getCurrentStaff(), ActivityAction.UPDATE, "Promotion", 
                id, "Cập nhật khuyến mãi: " + updated.getName(), null, updated, request);
            log.info("Updated promotion: {}", updated.getName());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete promotion
     * DELETE /admin/api/promotions/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CONTENT_STAFF') or hasAuthority('promotions.delete')")
    public ResponseEntity<?> deletePromotion(@PathVariable Long id, HttpServletRequest request) {
        try {
            promotionService.deletePromotion(id);
            auditService.logActivity(getCurrentStaff(), ActivityAction.DELETE, "Promotion", 
                id, "Xóa khuyến mãi ID: " + id, null, null, request);
            log.info("Deleted promotion: {}", id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa khuyến mãi thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Staff getCurrentStaff() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return staffRepository.findByEmail(email).orElse(null);
    }
}
