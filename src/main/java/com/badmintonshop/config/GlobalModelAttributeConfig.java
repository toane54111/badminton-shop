package com.badmintonshop.config;

import com.badmintonshop.security.CustomUserDetails;
import com.badmintonshop.security.CustomOAuth2User;
import com.badmintonshop.security.StaffUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global Controller Advice to provide user information to all views
 * Adds currentUserId/currentStaffId to model for logged-in users, enabling
 * personalized URLs
 */
@ControllerAdvice
@Slf4j
public class GlobalModelAttributeConfig {

    /**
     * Add current user's ID to model for all controller methods (Customer)
     * This enables templates to include userId in URLs for logged-in users
     * Supports both regular login and OAuth2 login
     */
    @ModelAttribute("currentUserId")
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();

            // Regular customer login
            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getUserId();
            }

            // OAuth2 login (Google, etc.)
            if (principal instanceof CustomOAuth2User) {
                return ((CustomOAuth2User) principal).getUserId();
            }
        }
        return null;
    }

    /**
     * Add current staff's ID to model for all controller methods (Admin/Staff)
     * This enables templates to include staffId in URLs for logged-in staff
     */
    @ModelAttribute("currentStaffId")
    public Long getCurrentStaffId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();

            if (principal instanceof StaffUserDetails) {
                return ((StaffUserDetails) principal).getStaffId();
            }
        }
        return null;
    }

    /**
     * Check if current user is authenticated (not anonymous) - Customer
     */
    @ModelAttribute("isLoggedIn")
    public Boolean isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            return principal instanceof CustomUserDetails || principal instanceof CustomOAuth2User;
        }
        return false;
    }

    /**
     * Check if current staff is authenticated - Staff/Admin
     */
    @ModelAttribute("isStaffLoggedIn")
    public Boolean isStaffLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            return principal instanceof StaffUserDetails;
        }
        return false;
    }

    /**
     * Get current user's full name (Customer)
     */
    @ModelAttribute("currentUserName")
    public String getCurrentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();

            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getFullName();
            }

            if (principal instanceof CustomOAuth2User) {
                return ((CustomOAuth2User) principal).getFullName();
            }
        }
        return null;
    }

    /**
     * Get current staff's full name (Admin/Staff)
     */
    @ModelAttribute("currentStaffName")
    public String getCurrentStaffName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();

            if (principal instanceof StaffUserDetails) {
                return ((StaffUserDetails) principal).getFullName();
            }
        }
        return null;
    }

    /**
     * Get current staff's role
     */
    @ModelAttribute("currentStaffRole")
    public String getCurrentStaffRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();

            if (principal instanceof StaffUserDetails) {
                return ((StaffUserDetails) principal).getRole();
            }
        }
        return null;
    }
}
