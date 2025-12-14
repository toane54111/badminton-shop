package com.badmintonshop.service;

import com.badmintonshop.dto.auth.AuthResponse;
import com.badmintonshop.entity.EmailVerification;
import com.badmintonshop.entity.User;
import com.badmintonshop.exception.BadRequestException;
import com.badmintonshop.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for email verification operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationRepository verificationRepository;
    private final AuthService authService;
    private final EmailService emailService;

    @Value("${app.auth.email-verification.expiry-hours:48}")
    private int tokenExpiryHours;

    @Value("${app.auth.email-verification.rate-limit:5}")
    private int rateLimitPerHour;

    /**
     * Create email verification token
     */
    @Transactional
    public EmailVerification createVerification(User user) {
        // Rate limiting
        long recentTokens = verificationRepository.countRecentByUser(user, LocalDateTime.now().minusHours(1));
        if (recentTokens >= rateLimitPerHour) {
            log.warn("Rate limit exceeded for email verification: {}", user.getEmail());
            throw new BadRequestException("Bạn đã yêu cầu quá nhiều lần. Vui lòng thử lại sau.");
        }

        // Create new token
        String token = UUID.randomUUID().toString();
        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(tokenExpiryHours))
                .build();

        verificationRepository.save(verification);
        log.info("Email verification token created for: {}", user.getEmail());

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), token);

        // Also log for debugging
        log.debug("Verification token for {}: {}", user.getEmail(), token);

        return verification;
    }

    /**
     * Resend verification email
     */
    @Transactional
    public AuthResponse resendVerification(String email) {
        try {
            User user = authService.getUserByEmail(email);

            if (user.getIsEmailVerified()) {
                return AuthResponse.error("Email đã được xác thực.");
            }

            createVerification(user);
            return AuthResponse.success("Email xác thực đã được gửi lại.");
        } catch (Exception e) {
            log.error("Error resending verification for: {}", email, e);
            return AuthResponse.error("Không thể gửi email xác thực. Vui lòng thử lại.");
        }
    }

    /**
     * Verify email using token
     */
    @Transactional
    public AuthResponse verifyEmail(String token) {
        Optional<EmailVerification> verificationOpt = verificationRepository.findValidToken(
                token, LocalDateTime.now());

        if (verificationOpt.isEmpty()) {
            return AuthResponse.error("Link xác thực không hợp lệ hoặc đã hết hạn.");
        }

        EmailVerification verification = verificationOpt.get();
        User user = verification.getUser();

        // Mark as verified
        verification.verify();
        verificationRepository.save(verification);

        // Update user
        authService.markEmailVerified(user);

        log.info("Email verified for: {}", user.getEmail());

        return AuthResponse.builder()
                .success(true)
                .message("Xác thực email thành công! Bạn có thể đăng nhập ngay bây giờ.")
                .redirectUrl("/login?verified=true")
                .build();
    }

    /**
     * Check if user has pending verification
     */
    @Transactional(readOnly = true)
    public boolean hasPendingVerification(User user) {
        return verificationRepository.hasPendingVerification(user, LocalDateTime.now());
    }

    /**
     * Cleanup expired verifications (called by scheduler)
     */
    @Transactional
    public int cleanupExpiredVerifications() {
        int deleted = verificationRepository.deleteExpiredVerifications(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired email verifications", deleted);
        }
        return deleted;
    }
}
