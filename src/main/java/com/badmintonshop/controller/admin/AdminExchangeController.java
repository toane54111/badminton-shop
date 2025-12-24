package com.badmintonshop.controller.admin;

import com.badmintonshop.dto.response.ExchangeResponse;
import com.badmintonshop.entity.Staff;
import com.badmintonshop.entity.enums.ActivityAction;
import com.badmintonshop.entity.enums.ExchangeStatus;
import com.badmintonshop.repository.StaffRepository;
import com.badmintonshop.service.AuditService;
import com.badmintonshop.service.ExchangeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/exchanges")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALE_STAFF')")
public class AdminExchangeController {

    private final ExchangeService exchangeService;
    private final AuditService auditService;
    private final StaffRepository staffRepository;

    @GetMapping
    public String listExchanges(Model model) {
        model.addAttribute("exchanges", exchangeService.getAllExchanges());
        return "admin/exchanges/index";
    }

    @GetMapping("/{id}")
    public String viewExchange(@PathVariable Long id, Model model) {
        ExchangeResponse exchange = exchangeService.getExchangeById(id);
        model.addAttribute("exchange", exchange);
        model.addAttribute("statuses", ExchangeStatus.values());
        return "admin/exchanges/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
            @RequestParam ExchangeStatus status,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        try {
            exchangeService.updateStatus(id, status, notes);
            auditService.logActivity(getCurrentStaff(), ActivityAction.UPDATE, "Exchange", 
                id, "Cập nhật trạng thái đổi trả: " + status, null, null, request);
            redirectAttributes.addFlashAttribute("successMessage", "Exchange status updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating status: " + e.getMessage());
        }
        return "redirect:/admin/exchanges/" + id;
    }

    private Staff getCurrentStaff() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return staffRepository.findByEmail(email).orElse(null);
    }
}
