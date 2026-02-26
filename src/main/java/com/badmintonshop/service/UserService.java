package com.badmintonshop.service;

import com.badmintonshop.dto.response.UserResponse;
import com.badmintonshop.dto.response.UserStatisticsResponse;
import com.badmintonshop.entity.User;
import com.badmintonshop.entity.enums.UserStatus;
import com.badmintonshop.exception.ResourceNotFoundException;
import com.badmintonshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for Admin User Management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * Get all users with filters and pagination
     */
    public Page<UserResponse> getAll(String search, UserStatus status, Boolean verified, Pageable pageable) {
        log.debug("Fetching users with filters - search: {}, status: {}, verified: {}", search, status, verified);

        return userRepository.findWithFilters(search, status, verified, pageable)
                .map(UserResponse::fromEntity);
    }

    /**
     * Get user by ID
     */
    public UserResponse getById(Long id) {
        log.debug("Fetching user by id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Get stats
        int totalOrders = user.getOrders() != null ? user.getOrders().size() : 0;
        int totalReviews = user.getReviews() != null ? user.getReviews().size() : 0;

        return UserResponse.fromEntityWithStats(user, totalOrders, totalReviews);
    }

    /**
     * Update user status (ban/unban/lock)
     */
    @Transactional
    public UserResponse updateStatus(Long id, UserStatus status) {
        log.info("Updating status for user {} to {}", id, status);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setStatus(status);
        User saved = userRepository.save(user);

        log.info("Updated status for user: {}", id);
        return UserResponse.fromEntity(saved);
    }

    /**
     * Get user statistics
     */
    public UserStatisticsResponse getStatistics() {
        log.debug("Generating user statistics");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfWeek = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime start30Days = now.minusDays(30);

        // Basic counts
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatusAndDeletedAtIsNull(UserStatus.ACTIVE);
        long bannedUsers = userRepository.countByStatusAndDeletedAtIsNull(UserStatus.BANNED);
        long lockedUsers = userRepository.countByStatusAndDeletedAtIsNull(UserStatus.LOCKED);
        long verifiedUsers = userRepository.countVerifiedUsers();

        // Today
        long newUsersToday = userRepository.countRegisteredBetween(startOfToday, now);
        long activeUsersToday = userRepository.countActiveUsersBetween(startOfToday, now);

        // This week and month
        long newUsersThisWeek = userRepository.countRegisteredBetween(startOfWeek, now);
        long newUsersThisMonth = userRepository.countRegisteredBetween(startOfMonth, now);

        // Registration trend (last 30 days)
        List<Object[]> dailyData = userRepository.countDailyRegistrations(start30Days);
        List<UserStatisticsResponse.DailyCount> registrationTrend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Object[] row : dailyData) {
            String date = row[0].toString();
            long count = ((Number) row[1]).longValue();
            registrationTrend.add(UserStatisticsResponse.DailyCount.builder()
                    .date(date)
                    .count(count)
                    .build());
        }

        // Users by gender
        Map<String, Long> usersByGender = new HashMap<>();
        for (Object[] row : userRepository.countByGender()) {
            String gender = row[0] != null ? row[0].toString() : "UNKNOWN";
            long count = ((Number) row[1]).longValue();
            usersByGender.put(gender, count);
        }

        // Users by skill level
        Map<String, Long> usersBySkillLevel = new HashMap<>();
        for (Object[] row : userRepository.countBySkillLevel()) {
            String skill = row[0] != null ? row[0].toString() : "UNKNOWN";
            long count = ((Number) row[1]).longValue();
            usersBySkillLevel.put(skill, count);
        }

        return UserStatisticsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .bannedUsers(bannedUsers)
                .lockedUsers(lockedUsers)
                .verifiedUsers(verifiedUsers)
                .newUsersToday(newUsersToday)
                .activeUsersToday(activeUsersToday)
                .newUsersThisWeek(newUsersThisWeek)
                .newUsersThisMonth(newUsersThisMonth)
                .registrationTrend(registrationTrend)
                .usersByGender(usersByGender)
                .usersBySkillLevel(usersBySkillLevel)
                .build();
    }

    /**
     * Export users to CSV
     */
    public byte[] exportToCsv(String search, UserStatus status) {
        log.info("Exporting users to CSV - search: {}, status: {}", search, status);

        List<User> users = userRepository.findAllForExport(search, status);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        // CSV Header
        writer.println(
                "ID,Email,Full Name,Phone,Gender,Status,Email Verified,Playing Style,Skill Level,Last Login,Created At");

        // CSV Data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (User user : users) {
            writer.println(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\",\"%s\",\"%s\"",
                    user.getUserId(),
                    escapeQuotes(user.getEmail()),
                    escapeQuotes(user.getFullName()),
                    escapeQuotes(user.getPhone()),
                    user.getGender() != null ? user.getGender().name() : "",
                    user.getStatus().name(),
                    user.getIsEmailVerified(),
                    user.getPlayingStyle() != null ? user.getPlayingStyle().name() : "",
                    user.getSkillLevel() != null ? user.getSkillLevel().name() : "",
                    user.getLastLoginAt() != null ? user.getLastLoginAt().format(formatter) : "",
                    user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : ""));
        }

        writer.flush();
        log.info("Exported {} users to CSV", users.size());
        return outputStream.toByteArray();
    }

    private String escapeQuotes(String value) {
        if (value == null)
            return "";
        return value.replace("\"", "\"\"");
    }

    /**
     * Count total users
     */
    public long countTotal() {
        return userRepository.count();
    }

    /**
     * Count active users
     */
    public long countActive() {
        return userRepository.countByStatusAndDeletedAtIsNull(UserStatus.ACTIVE);
    }

    /**
     * Get all active users for notification selection
     */
    public List<UserResponse> getAllUsersForNotification() {
        log.debug("Fetching all active users for notification");
        return userRepository.findByStatusAndDeletedAtIsNull(UserStatus.ACTIVE)
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }
}
