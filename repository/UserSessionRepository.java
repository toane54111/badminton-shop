package com.badmintonshop.repository;

import com.badmintonshop.entity.User;
import com.badmintonshop.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing user sessions
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    /**
     * Find session by token
     */
    Optional<UserSession> findByToken(String token);

    /**
     * Find session by refresh token
     */
    Optional<UserSession> findByRefreshToken(String refreshToken);

    /**
     * Find all active sessions for a user
     */
    @Query("SELECT s FROM UserSession s WHERE s.user = :user AND s.expiresAt > :now ORDER BY s.createdAt DESC")
    List<UserSession> findActiveSessionsByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Find all sessions for a user
     */
    List<UserSession> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Count active sessions for a user
     */
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.user = :user AND s.expiresAt > :now")
    long countActiveSessionsByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Delete all sessions for a user (logout from all devices)
     */
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    /**
     * Delete expired sessions (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now")
    int deleteExpiredSessions(@Param("now") LocalDateTime now);

    /**
     * Update last activity time
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.lastActivityAt = :activityTime WHERE s.token = :token")
    void updateLastActivity(@Param("token") String token, @Param("activityTime") LocalDateTime activityTime);

    /**
     * Check if token exists and is valid
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM UserSession s WHERE s.token = :token AND s.expiresAt > :now")
    boolean isTokenValid(@Param("token") String token, @Param("now") LocalDateTime now);
}
