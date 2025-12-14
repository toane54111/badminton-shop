package com.badmintonshop.repository;

import com.badmintonshop.entity.Staff;
import com.badmintonshop.entity.enums.StaffRole;
import com.badmintonshop.entity.enums.StaffStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Staff entity
 */
@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    /**
     * Find staff by email
     */
    Optional<Staff> findByEmail(String email);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find staff by role
     */
    List<Staff> findByRole(StaffRole role);

    /**
     * Find active staff
     */
    List<Staff> findByStatus(StaffStatus status);

    /**
     * Find active staff by role
     */
    List<Staff> findByRoleAndStatus(StaffRole role, StaffStatus status);

    /**
     * Find stringing staff with capacity
     */
    @Query("SELECT s FROM Staff s " +
           "WHERE s.role = 'STRINGING_STAFF' " +
           "AND s.status = 'ACTIVE' " +
           "ORDER BY s.stringingSkillLevel DESC")
    List<Staff> findActiveStringingStaff();

    /**
     * Search staff by name or email
     */
    @Query("SELECT s FROM Staff s " +
           "WHERE s.status = 'ACTIVE' " +
           "AND (LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Staff> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Count by status
     */
    long countByStatus(StaffStatus status);

    /**
     * Count by role
     */
    long countByRole(StaffRole role);
}
