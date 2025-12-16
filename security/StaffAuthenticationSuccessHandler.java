package com.badmintonshop.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication success handler for Staff/Admin login
 * Redirects to admin dashboard with staffId in URL
 */
@Component("staffAuthenticationSuccessHandler")
public class StaffAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        Long staffId = null;

        Object principal = authentication.getPrincipal();
        if (principal instanceof StaffUserDetails) {
            staffId = ((StaffUserDetails) principal).getStaffId();
        }

        String redirectUrl;
        if (staffId != null) {
            redirectUrl = "/admin/dashboard?sid=" + staffId;
        } else {
            redirectUrl = "/admin/dashboard";
        }

        response.sendRedirect(redirectUrl);
    }
}
