package com.badmintonshop.controller;

import com.badmintonshop.dto.auth.*;
import com.badmintonshop.entity.PasswordResetToken;
import com.badmintonshop.service.AuthService;
import com.badmintonshop.service.EmailVerificationService;
import com.badmintonshop.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controller for authentication pages and API
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;

    // ==================== LOGIN ====================

    /**
     * Display login page
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            @RequestParam(value = "verified", required = false) String verified,
            @RequestParam(value = "reset", required = false) String reset,
            @RequestParam(value = "expired", required = false) String expired,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng");
        }
        if (logout != null) {
            model.addAttribute("success", "Đăng xuất thành công");
        }
        if (registered != null) {
            model.addAttribute("success", "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");
        }
        if (verified != null) {
            model.addAttribute("success", "Xác thực email thành công! Bạn có thể đăng nhập ngay.");
        }
        if (reset != null) {
            model.addAttribute("success", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập với mật khẩu mới.");
        }
        if (expired != null) {
            model.addAttribute("error", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }

        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
    }

    /**
     * AJAX login endpoint
     */
    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<AuthResponse> apiLogin(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    // ==================== REGISTER ====================

    /**
     * Display registration page
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    /**
     * Handle registration form submission
     */
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            AuthResponse response = authService.register(request);
            if (response.isSuccess()) {
                redirectAttributes.addFlashAttribute("success", response.getMessage());
                return "redirect:" + response.getRedirectUrl();
            } else {
                model.addAttribute("error", response.getMessage());
                return "auth/register";
            }
        } catch (Exception e) {
            log.error("Registration error", e);
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    /**
     * AJAX registration endpoint
     */
    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<AuthResponse> apiRegister(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(AuthResponse.error(e.getMessage()));
        }
    }

    // ==================== FORGOT PASSWORD ====================

    /**
     * Display forgot password page
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        return "auth/forgot-password";
    }

    /**
     * Handle forgot password form submission
     */
    @PostMapping("/forgot-password")
    public String forgotPassword(
            @Valid @ModelAttribute("forgotPasswordRequest") ForgotPasswordRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        AuthResponse response = passwordResetService.createResetToken(request);
        redirectAttributes.addFlashAttribute("success", response.getMessage());
        return "redirect:/login";
    }

    /**
     * AJAX forgot password endpoint
     */
    @PostMapping("/api/auth/forgot-password")
    @ResponseBody
    public ResponseEntity<AuthResponse> apiForgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        AuthResponse response = passwordResetService.createResetToken(request);
        return ResponseEntity.ok(response);
    }

    // ==================== RESET PASSWORD ====================

    /**
     * Display reset password page
     */
    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam("token") String token,
            RedirectAttributes redirectAttributes,
            Model model) {

        Optional<PasswordResetToken> tokenOpt = passwordResetService.getValidToken(token);
        
        if (tokenOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            return "redirect:/forgot-password";
        }

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token(token)
                .build();
        model.addAttribute("resetPasswordRequest", request);
        return "auth/reset-password";
    }

    /**
     * Handle reset password form submission
     */
    @PostMapping("/reset-password")
    public String resetPassword(
            @Valid @ModelAttribute("resetPasswordRequest") ResetPasswordRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }

        AuthResponse response = passwordResetService.resetPassword(request);
        
        if (response.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", response.getMessage());
            return "redirect:" + response.getRedirectUrl();
        } else {
            model.addAttribute("error", response.getMessage());
            return "auth/reset-password";
        }
    }

    /**
     * AJAX reset password endpoint
     */
    @PostMapping("/api/auth/reset-password")
    @ResponseBody
    public ResponseEntity<AuthResponse> apiResetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        AuthResponse response = passwordResetService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    // ==================== EMAIL VERIFICATION ====================

    /**
     * Display verification required page
     */
    @GetMapping("/verification-required")
    public String verificationRequiredPage(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "auth/verification-required";
    }

    /**
     * Verify email using token
     */
    @GetMapping("/verify-email")
    public String verifyEmail(
            @RequestParam("token") String token,
            Model model) {

        AuthResponse response = emailVerificationService.verifyEmail(token);
        
        if (response.isSuccess()) {
            model.addAttribute("success", true);
        } else {
            model.addAttribute("error", response.getMessage());
        }
        
        return "auth/verify-email-result";
    }

    /**
     * Resend verification email (form submission)
     */
    @PostMapping("/resend-verification")
    public String resendVerification(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        
        AuthResponse response = emailVerificationService.resendVerification(email);
        
        if (response.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", response.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("error", response.getMessage());
        }
        
        return "redirect:/verification-required?email=" + email;
    }

    /**
     * Resend verification email (API)
     */
    @PostMapping("/api/auth/resend-verification")
    @ResponseBody
    public ResponseEntity<AuthResponse> apiResendVerification(@RequestParam("email") String email) {
        AuthResponse response = emailVerificationService.resendVerification(email);
        return ResponseEntity.ok(response);
    }
}
