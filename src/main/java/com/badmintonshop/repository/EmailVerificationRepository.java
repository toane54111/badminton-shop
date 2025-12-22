package com.badmintonshop.repository;

import com.badmintonshop.entity.EmailVerification;
import com.badmintonshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for managing email verification tokens
 */
@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    /**
     * Find verification by token
     */
    Optional<EmailVerification> findByToken(String token);

    /**
     * Find valid token (not verified and not expired)
     */
    @Query("SELECT v FROM EmailVerification v WHERE v.token = :token AND v.verifiedAt IS NULL AND v.expiresAt > :now")
    Optional<EmailVerification> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * Find pending verification for a user
     */
    @Query("SELECT v FROM EmailVerification v WHERE v.user = :user AND v.verifiedAt IS NULL ORDER BY v.createdAt DESC")
    Optional<EmailVerification> findPendingByUser(@Param("user") User user);

    /**
     * Find latest verification for a user
     */
    Optional<EmailVerification> findFirstByUserOrderByCreatedAtDesc(User user);

    /**
     * Check if user has pending verification
     */
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM EmailVerification v " +
           "WHERE v.user = :user AND v.verifiedAt IS NULL AND v.expiresAt > :now")
    boolean hasPendingVerification(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Delete expired verifications (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM EmailVerification v WHERE v.expiresAt < :now AND v.verifiedAt IS NULL")
    int deleteExpiredVerifications(@Param("now") LocalDateTime now);

    /**
     * Count verifications created in the last hour for a user (rate limiting)
     */
    @Query("SELECT COUNT(v) FROM EmailVerification v WHERE v.user = :user AND v.createdAt > :since")
    long countRecentByUser(@Param("user") User user, @Param("since") LocalDateTime since);
}
