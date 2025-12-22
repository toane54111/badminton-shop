package com.badmintonshop.repository;

import com.badmintonshop.entity.User;
import com.badmintonshop.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (not soft deleted)
     */
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    /**
     * Find user by Google OAuth ID
     */
    Optional<User> findByGoogleOauthIdAndDeletedAtIsNull(String googleOauthId);

    /**
     * Find user by phone
     */
    Optional<User> findByPhoneAndDeletedAtIsNull(String phone);

    /**
     * Check if email exists
     */
    boolean existsByEmailAndDeletedAtIsNull(String email);

    /**
     * Check if phone exists
     */
    boolean existsByPhoneAndDeletedAtIsNull(String phone);

    /**
     * Find all active users
     */
    Page<User> findByStatusAndDeletedAtIsNull(UserStatus status, Pageable pageable);

    /**
     * Search users by name or email
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL " +
           "AND (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Update last login time
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.userId = :userId")
    void updateLastLoginTime(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    /**
     * Soft delete user
     */
    @Modifying
    @Query("UPDATE User u SET u.deletedAt = :deletedAt, u.status = 'BANNED' WHERE u.userId = :userId")
    void softDelete(@Param("userId") Long userId, @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * Count users by status
     */
    long countByStatusAndDeletedAtIsNull(UserStatus status);

    /**
     * Find users registered within date range
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL " +
           "AND u.createdAt BETWEEN :startDate AND :endDate")
    Page<User> findUsersRegisteredBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find users with filters (search, status) with pagination
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL " +
           "AND (:search IS NULL OR :search = '' " +
           "     OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR u.phone LIKE CONCAT('%', :search, '%')) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (:verified IS NULL OR u.isEmailVerified = :verified) " +
           "ORDER BY u.createdAt DESC")
    Page<User> findWithFilters(@Param("search") String search,
                               @Param("status") UserStatus status,
                               @Param("verified") Boolean verified,
                               Pageable pageable);

    /**
     * Count users registered between dates
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL " +
           "AND u.createdAt BETWEEN :startDate AND :endDate")
    long countRegisteredBetween(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);

    /**
     * Count users with verified email
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL AND u.isEmailVerified = true")
    long countVerifiedUsers();

    /**
     * Count active users (logged in between dates)
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL " +
           "AND u.lastLoginAt BETWEEN :startDate AND :endDate")
    long countActiveUsersBetween(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Daily registration count for last N days
     */
    @Query(value = "SELECT DATE(created_at) as date, COUNT(*) as count " +
                   "FROM users WHERE deleted_at IS NULL " +
                   "AND created_at >= :startDate " +
                   "GROUP BY DATE(created_at) " +
                   "ORDER BY date ASC", nativeQuery = true)
    List<Object[]> countDailyRegistrations(@Param("startDate") LocalDateTime startDate);

    /**
     * Count users by gender
     */
    @Query("SELECT u.gender, COUNT(u) FROM User u WHERE u.deletedAt IS NULL AND u.gender IS NOT NULL GROUP BY u.gender")
    List<Object[]> countByGender();

    /**
     * Count users by skill level
     */
    @Query("SELECT u.skillLevel, COUNT(u) FROM User u WHERE u.deletedAt IS NULL AND u.skillLevel IS NOT NULL GROUP BY u.skillLevel")
    List<Object[]> countBySkillLevel();

    /**
     * Find all users for export (no pagination)
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL " +
           "AND (:search IS NULL OR :search = '' " +
           "     OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "ORDER BY u.createdAt DESC")
    List<User> findAllForExport(@Param("search") String search,
                                @Param("status") UserStatus status);
}
