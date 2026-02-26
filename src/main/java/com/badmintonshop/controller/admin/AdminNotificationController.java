package com.badmintonshop.controller.admin;

import com.badmintonshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Admin page controller for notifications management
 */
@Controller
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final UserService userService;

    /**
     * Display notification send page
     * GET /admin/notifications/send
     */
    @GetMapping("/send")
    public String showSendNotificationPage(Model model) {
        // Get all users for selection
        model.addAttribute("users", userService.getAllUsersForNotification());
        return "admin/notifications-send";
    }
}
