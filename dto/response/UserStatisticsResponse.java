package com.badmintonshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for User Statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {

    // Overview
    private long totalUsers;
    private long activeUsers;
    private long bannedUsers;
    private long lockedUsers;
    private long verifiedUsers;
    
    // Today stats
    private long newUsersToday;
    private long activeUsersToday;  // Logged in today
    
    // This week
    private long newUsersThisWeek;
    
    // This month
    private long newUsersThisMonth;
    
    // Registration trend (last 30 days)
    private List<DailyCount> registrationTrend;
    
    // Users by gender
    private Map<String, Long> usersByGender;
    
    // Users by skill level
    private Map<String, Long> usersBySkillLevel;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCount {
        private String date;
        private long count;
    }
}
