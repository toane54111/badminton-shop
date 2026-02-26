package com.badmintonshop.repository;

import com.badmintonshop.entity.StaffActivityLog;
import com.badmintonshop.entity.enums.ActivityAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for StaffActivityLog entity
 * Supports dynamic filtering with JpaSpecificationExecutor
 */
@Repository
public interface StaffActivityLogRepository extends JpaRepository<StaffActivityLog, Long>, 
        JpaSpecificationExecutor<StaffActivityLog> {

    /**
     * Find logs by staff ID
     */
    Page<StaffActivityLog> findByStaffStaffIdOrderByCreatedAtDesc(Long staffId, Pageable pageable);

    /**
     * Find logs by entity type
     */
    Page<StaffActivityLog> findByEntityTypeOrderByCreatedAtDesc(String entityType, Pageable pageable);

    /**
     * Find logs by action
     */
    Page<StaffActivityLog> findByActionOrderByCreatedAtDesc(ActivityAction action, Pageable pageable);

    /**
     * Find logs within date range
     */
    Page<StaffActivityLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime from, LocalDateTime to, Pageable pageable);

    /**
     * Find logs by entity type and entity ID
     */
    List<StaffActivityLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, Long entityId);

    /**
     * Complex query with multiple filters
     */
    @Query("SELECT l FROM StaffActivityLog l " +
           "WHERE (:staffId IS NULL OR l.staff.staffId = :staffId) " +
           "AND (:entityType IS NULL OR l.entityType = :entityType) " +
           "AND (:action IS NULL OR l.action = :action) " +
           "AND (:fromDate IS NULL OR l.createdAt >= :fromDate) " +
           "AND (:toDate IS NULL OR l.createdAt <= :toDate) " +
           "ORDER BY l.createdAt DESC")
    Page<StaffActivityLog> findWithFilters(
            @Param("staffId") Long staffId,
            @Param("entityType") String entityType,
            @Param("action") ActivityAction action,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    /**
     * Count logs by staff in date range
     */
    @Query("SELECT COUNT(l) FROM StaffActivityLog l " +
           "WHERE l.staff.staffId = :staffId " +
           "AND l.createdAt >= :fromDate AND l.createdAt <= :toDate")
    long countByStaffInDateRange(
            @Param("staffId") Long staffId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
}
