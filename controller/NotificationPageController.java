package com.badmintonshop.controller;

import com.badmintonshop.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for notifications page
 */
@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationPageController {

    /**
     * Display notifications page
     * GET /notifications
     */
    @GetMapping
    public String showNotifications(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("userId", user.getUserId());
        return "shop/notifications";
    }
}
