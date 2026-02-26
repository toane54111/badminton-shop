package com.badmintonshop.repository;

import com.badmintonshop.entity.Staff;
import com.badmintonshop.entity.StaffPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for StaffPermission entity
 */
@Repository
public interface StaffPermissionRepository extends JpaRepository<StaffPermission, Long> {

    /**
     * Find all permissions for a staff
     */
    List<StaffPermission> findByStaff(Staff staff);

    /**
     * Find permissions by staff ID
     */
    List<StaffPermission> findByStaffStaffId(Long staffId);

    /**
     * Delete all permissions for a staff
     */
    @Modifying
    @Query("DELETE FROM StaffPermission p WHERE p.staff = :staff")
    void deleteByStaff(@Param("staff") Staff staff);

    /**
     * Delete all permissions by staff ID
     */
    @Modifying
    @Query("DELETE FROM StaffPermission p WHERE p.staff.staffId = :staffId")
    void deleteByStaffStaffId(@Param("staffId") Long staffId);

    /**
     * Check if permission exists for staff
     */
    boolean existsByStaffAndPermissionKey(Staff staff, String permissionKey);

    /**
     * Find permission keys by staff ID
     */
    @Query("SELECT p.permissionKey FROM StaffPermission p WHERE p.staff.staffId = :staffId")
    List<String> findPermissionKeysByStaffId(@Param("staffId") Long staffId);
}