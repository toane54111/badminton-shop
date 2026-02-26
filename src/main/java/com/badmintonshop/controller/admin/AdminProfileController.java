package com.badmintonshop.controller.admin;

import com.badmintonshop.dto.profile.ChangePasswordRequest;
import com.badmintonshop.entity.Staff;
import com.badmintonshop.repository.StaffRepository;
import com.badmintonshop.security.StaffUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin Profile Controller - handles staff/admin profile pages
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminProfileController {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Display admin/staff profile page
     */
    @GetMapping("/profile")
    public String profilePage(Model model) {
        Staff staff = getCurrentStaff();
        if (staff == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("staff", staff);
        return "admin/profile";
    }

    /**
     * Update staff profile info
     */
    @PostMapping("/api/profile")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> request) {
        try {
            Staff staff = getCurrentStaff();
            if (staff == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
            }

            String fullName = request.get("fullName");
            String phone = request.get("phone");

            if (fullName != null && !fullName.trim().isEmpty()) {
                staff.setFullName(fullName.trim());
            }
            if (phone != null) {
                staff.setPhone(phone.trim());
            }

            staffRepository.save(staff);
            log.info("Updated profile for staff: {}", staff.getEmail());

            return ResponseEntity.ok(Map.of("message", "Cập nhật thông tin thành công"));
        } catch (Exception e) {
            log.error("Error updating profile", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    /**
     * Change staff password
     */
    @PostMapping("/api/profile/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            Staff staff = getCurrentStaff();
            if (staff == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
            }

            // Verify current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), staff.getPasswordHash())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu hiện tại không đúng"));
            }

            // Check new password confirmation
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới không khớp"));
            }

            // Update password
            staff.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            staffRepository.save(staff);

            log.info("Changed password for staff: {}", staff.getEmail());
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
        } catch (Exception e) {
            log.error("Error changing password", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    /**
     * Get current authenticated staff
     */
    private Staff getCurrentStaff() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        if (auth.getPrincipal() instanceof StaffUserDetails staffDetails) {
            return staffRepository.findById(staffDetails.getStaffId()).orElse(null);
        }

        return null;
    }
}
