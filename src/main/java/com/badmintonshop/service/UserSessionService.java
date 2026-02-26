package com.badmintonshop.service;

import com.badmintonshop.entity.User;
import com.badmintonshop.entity.UserSession;
import com.badmintonshop.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing user sessions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSessionService {

    private final UserSessionRepository sessionRepository;

    @Value("${app.auth.session.expiry-days:7}")
    private int sessionExpiryDays;

    @Value("${app.auth.session.max-sessions:5}")
    private int maxSessionsPerUser;

    /**
     * Create a new session for user
     */
    @Transactional
    public UserSession createSession(User user, HttpServletRequest request) {
        // Check if user has too many active sessions
        long activeCount = sessionRepository.countActiveSessionsByUser(user, LocalDateTime.now());
        if (activeCount >= maxSessionsPerUser) {
            // Remove oldest sessions
            List<UserSession> sessions = sessionRepository.findActiveSessionsByUser(user, LocalDateTime.now());
            for (int i = maxSessionsPerUser - 1; i < sessions.size(); i++) {
                sessionRepository.delete(sessions.get(i));
            }
        }

        // Create new session
        String token = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();
        
        UserSession session = UserSession.builder()
                .user(user)
                .token(token)
                .refreshToken(refreshToken)
                .ipAddress(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .deviceType(detectDeviceType(request.getHeader("User-Agent")))
                .expiresAt(LocalDateTime.now().plusDays(sessionExpiryDays))
                .build();

        sessionRepository.save(session);
        log.info("Session created for user: {} from IP: {}", user.getEmail(), session.getIpAddress());

        return session;
    }

    /**
     * Find session by token
     */
    @Transactional(readOnly = true)
    public Optional<UserSession> findByToken(String token) {
        return sessionRepository.findByToken(token);
    }

    /**
     * Validate session token
     */
    @Transactional(readOnly = true)
    public boolean isTokenValid(String token) {
        return sessionRepository.isTokenValid(token, LocalDateTime.now());
    }

    /**
     * Update session activity
     */
    @Transactional
    public void updateActivity(String token) {
        sessionRepository.updateLastActivity(token, LocalDateTime.now());
    }

    /**
     * Invalidate a session
     */
    @Transactional
    public void invalidateSession(String token) {
        sessionRepository.findByToken(token).ifPresent(session -> {
            sessionRepository.delete(session);
            log.info("Session invalidated for user: {}", session.getUser().getEmail());
        });
    }

    /**
     * Invalidate all sessions for a user (logout from all devices)
     */
    @Transactional
    public void invalidateAllUserSessions(Long userId) {
        sessionRepository.deleteAllByUserId(userId);
        log.info("All sessions invalidated for user ID: {}", userId);
    }

    /**
     * Get all active sessions for a user
     */
    @Transactional(readOnly = true)
    public List<UserSession> getActiveSessions(User user) {
        return sessionRepository.findActiveSessionsByUser(user, LocalDateTime.now());
    }

    /**
     * Cleanup expired sessions (called by scheduler)
     */
    @Transactional
    public int cleanupExpiredSessions() {
        int deleted = sessionRepository.deleteExpiredSessions(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired sessions", deleted);
        }
        return deleted;
    }

    /**
     * Get client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Detect device type from User-Agent
     */
    private String detectDeviceType(String userAgent) {
        if (userAgent == null) {
            return "unknown";
        }
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return "mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "tablet";
        }
        return "desktop";
    }
}
