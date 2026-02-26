package com.badmintonshop.controller.admin;

import com.badmintonshop.dto.response.WarrantyResponse;
import com.badmintonshop.entity.Staff;
import com.badmintonshop.entity.enums.ActivityAction;
import com.badmintonshop.entity.enums.WarrantyStatus;
import com.badmintonshop.repository.StaffRepository;
import com.badmintonshop.service.AuditService;
import com.badmintonshop.service.WarrantyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/warranties")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALE_STAFF')")
public class AdminWarrantyController {

    private final WarrantyService warrantyService;
    private final AuditService auditService;
    private final StaffRepository staffRepository;

    @GetMapping
    public String listWarranties(Model model) {
        model.addAttribute("warranties", warrantyService.getAllWarranties());
        return "admin/warranties/index";
    }

    @GetMapping("/{id}")
    public String viewWarranty(@PathVariable Long id, Model model) {
        WarrantyResponse warranty = warrantyService.getWarrantyById(id);
        model.addAttribute("warranty", warranty);
        model.addAttribute("statuses", WarrantyStatus.values());
        return "admin/warranties/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
            @RequestParam WarrantyStatus status,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        try {
            warrantyService.updateStatus(id, status, notes);
            auditService.logActivity(getCurrentStaff(), ActivityAction.UPDATE, "Warranty", 
                id, "Cập nhật trạng thái bảo hành: " + status, null, null, request);
            redirectAttributes.addFlashAttribute("successMessage", "Warranty status updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating status: " + e.getMessage());
        }
        return "redirect:/admin/warranties/" + id;
    }

    private Staff getCurrentStaff() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return staffRepository.findByEmail(email).orElse(null);
    }
}
