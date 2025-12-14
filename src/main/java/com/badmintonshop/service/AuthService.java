package com.badmintonshop.service;

import com.badmintonshop.dto.auth.AuthResponse;
import com.badmintonshop.dto.auth.LoginRequest;
import com.badmintonshop.dto.auth.RegisterRequest;
import com.badmintonshop.entity.User;
import com.badmintonshop.entity.enums.UserStatus;
import com.badmintonshop.exception.BadRequestException;
import com.badmintonshop.exception.ResourceNotFoundException;
import com.badmintonshop.repository.UserRepository;
import com.badmintonshop.security.CustomOAuth2User;
import com.badmintonshop.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for authentication operations
 */
@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationService emailVerificationService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       @Lazy EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailVerificationService = emailVerificationService;
    }

    /**
     * Register a new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng");
        }

        // Check if phone already exists (if provided)
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (userRepository.existsByPhoneAndDeletedAtIsNull(request.getPhone())) {
                throw new BadRequestException("Số điện thoại đã được sử dụng");
            }
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .isEmailVerified(false)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        // Create email verification token
        emailVerificationService.createVerification(user);

        return AuthResponse.builder()
                .success(true)
                .message("Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.")
                .redirectUrl("/login?registered=true")
                .build();
    }

    /**
     * Authenticate user (used for AJAX login)
     */
    @Transactional
    public AuthResponse authenticate(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Update last login time
            userRepository.updateLastLoginTime(user.getUserId(), LocalDateTime.now());

            log.info("User logged in: {}", user.getEmail());

            return AuthResponse.builder()
                    .success(true)
                    .message("Đăng nhập thành công")
                    .redirectUrl("/")
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .avatarUrl(user.getAvatarUrl())
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            return AuthResponse.error("Email hoặc mật khẩu không đúng");
        } catch (Exception e) {
            log.error("Login error for email: {}", request.getEmail(), e);
            return AuthResponse.error("Đã có lỗi xảy ra. Vui lòng thử lại.");
        }
    }

    /**
     * Get current authenticated user
     */
    @Transactional(readOnly = true)
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            Long userId = userDetails.getUser().getUserId();
            return userRepository.findById(userId);
        } else if (authentication.getPrincipal() instanceof CustomOAuth2User oauth2User) {
            Long userId = oauth2User.getUser().getUserId();
            return userRepository.findById(userId);
        }

        return Optional.empty();
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email: " + email));
    }

    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    /**
     * Update user's password
     */
    @Transactional
    public void updatePassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password updated for user: {}", user.getEmail());
    }

    /**
     * Mark email as verified
     */
    @Transactional
    public void markEmailVerified(User user) {
        user.setIsEmailVerified(true);
        userRepository.save(user);
        log.info("Email verified for user: {}", user.getEmail());
    }
}
