package com.badmintonshop.repository;

import com.badmintonshop.entity.Notification;
import com.badmintonshop.entity.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find notifications by user with pagination
    Page<Notification> findByUserUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Find unread notifications by user
    List<Notification> findByUserUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // Count unread notifications
    long countByUserUserIdAndIsReadFalse(Long userId);

    // Find by user and type
    Page<Notification> findByUserUserIdAndType(Long userId, NotificationType type, Pageable pageable);

    // Mark all as read for user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    // Find recent notifications (since a given date)
    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId AND n.createdAt >= :sinceDate ORDER BY n.createdAt DESC")
    List<Notification> findRecentByUserId(@Param("userId") Long userId,
            @Param("sinceDate") java.time.LocalDateTime sinceDate);
}
