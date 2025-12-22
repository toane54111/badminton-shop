package com.badmintonshop.service;

import com.badmintonshop.dto.auth.AuthResponse;
import com.badmintonshop.dto.auth.ForgotPasswordRequest;
import com.badmintonshop.dto.auth.ResetPasswordRequest;
import com.badmintonshop.entity.PasswordResetToken;
import com.badmintonshop.entity.User;
import com.badmintonshop.exception.BadRequestException;
import com.badmintonshop.repository.PasswordResetTokenRepository;
import com.badmintonshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for password reset operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final EmailService emailService;

    @Value("${app.auth.password-reset.expiry-hours:24}")
    private int tokenExpiryHours;

    @Value("${app.auth.password-reset.rate-limit:3}")
    private int rateLimitPerHour;

    /**
     * Create a password reset token and send email
     */
    @Transactional
    public AuthResponse createResetToken(ForgotPasswordRequest request) {
        String email = request.getEmail();
        
        Optional<User> userOpt = userRepository.findByEmailAndDeletedAtIsNull(email);
        
        // Always return success to prevent email enumeration
        if (userOpt.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return AuthResponse.success("Nếu email tồn tại, bạn sẽ nhận được link đặt lại mật khẩu.");
        }

        User user = userOpt.get();

        // Rate limiting: check how many tokens created in the last hour
        long recentTokens = tokenRepository.countRecentTokensByUser(user, LocalDateTime.now().minusHours(1));
        if (recentTokens >= rateLimitPerHour) {
            log.warn("Rate limit exceeded for password reset: {}", email);
            return AuthResponse.error("Bạn đã yêu cầu quá nhiều lần. Vui lòng thử lại sau 1 giờ.");
        }

        // Invalidate existing tokens
        tokenRepository.invalidateAllTokensForUser(user);

        // Create new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(tokenExpiryHours))
                .build();

        tokenRepository.save(resetToken);
        log.info("Password reset token created for: {}", email);

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), token);
        
        // Also log for debugging
        log.debug("Password reset token for {}: {}", email, token);

        return AuthResponse.success("Nếu email tồn tại, bạn sẽ nhận được link đặt lại mật khẩu.");
    }

    /**
     * Validate reset token
     */
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        return tokenRepository.findValidToken(token, LocalDateTime.now()).isPresent();
    }

    /**
     * Get token entity (for displaying reset form)
     */
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> getValidToken(String token) {
        return tokenRepository.findValidToken(token, LocalDateTime.now());
    }

    /**
     * Reset password using token
     */
    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findValidToken(
                request.getToken(), LocalDateTime.now());

        if (tokenOpt.isEmpty()) {
            return AuthResponse.error("Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
        }

        PasswordResetToken resetToken = tokenOpt.get();
        User user = resetToken.getUser();

        // Update password
        authService.updatePassword(user, request.getPassword());

        // Mark token as used
        resetToken.markAsUsed();
        tokenRepository.save(resetToken);

        log.info("Password reset successful for: {}", user.getEmail());

        return AuthResponse.builder()
                .success(true)
                .message("Đặt lại mật khẩu thành công! Vui lòng đăng nhập với mật khẩu mới.")
                .redirectUrl("/login?reset=true")
                .build();
    }

    /**
     * Cleanup expired tokens (called by scheduler)
     */
    @Transactional
    public int cleanupExpiredTokens() {
        int deleted = tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired password reset tokens", deleted);
        }
        return deleted;
    }
}
