package com.badmintonshop.repository;

import com.badmintonshop.entity.PasswordResetToken;
import com.badmintonshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for managing password reset tokens
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Find valid token (not used and not expired)
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.token = :token AND t.isUsed = false AND t.expiresAt > :now")
    Optional<PasswordResetToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * Find token by token string
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Find latest token for a user
     */
    Optional<PasswordResetToken> findFirstByUserOrderByCreatedAtDesc(User user);

    /**
     * Find unused tokens for a user
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user = :user AND t.isUsed = false ORDER BY t.createdAt DESC")
    Optional<PasswordResetToken> findLatestUnusedByUser(@Param("user") User user);

    /**
     * Check if user has a valid token
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM PasswordResetToken t " +
           "WHERE t.user = :user AND t.isUsed = false AND t.expiresAt > :now")
    boolean hasValidToken(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Invalidate all tokens for a user (when password is reset)
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.isUsed = true WHERE t.user = :user AND t.isUsed = false")
    void invalidateAllTokensForUser(@Param("user") User user);

    /**
     * Delete expired tokens (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Count tokens created in the last hour for a user (rate limiting)
     */
    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.user = :user AND t.createdAt > :since")
    long countRecentTokensByUser(@Param("user") User user, @Param("since") LocalDateTime since);
}
