package com.badmintonshop.dto.response;

import com.badmintonshop.entity.User;
import com.badmintonshop.entity.enums.Gender;
import com.badmintonshop.entity.enums.PlayingStyle;
import com.badmintonshop.entity.enums.SkillLevel;
import com.badmintonshop.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for User (Admin view)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long userId;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private Gender gender;
    
    private Boolean isEmailVerified;
    private LocalDateTime emailVerifiedAt;
    
    // Playing profile
    private PlayingStyle playingStyle;
    private SkillLevel skillLevel;
    private String preferredRacketWeight;
    
    // Status
    private UserStatus status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    
    // Stats (optional)
    private Integer totalOrders;
    private Integer totalReviews;

    /**
     * Convert from entity to response DTO
     */
    public static UserResponse fromEntity(User entity) {
        return UserResponse.builder()
                .userId(entity.getUserId())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .phone(entity.getPhone())
                .avatarUrl(entity.getAvatarUrl())
                .dateOfBirth(entity.getDateOfBirth())
                .gender(entity.getGender())
                .isEmailVerified(entity.getIsEmailVerified())
                .emailVerifiedAt(entity.getEmailVerifiedAt())
                .playingStyle(entity.getPlayingStyle())
                .skillLevel(entity.getSkillLevel())
                .preferredRacketWeight(entity.getPreferredRacketWeight())
                .status(entity.getStatus())
                .lastLoginAt(entity.getLastLoginAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Convert from entity with stats
     */
    public static UserResponse fromEntityWithStats(User entity, int totalOrders, int totalReviews) {
        UserResponse response = fromEntity(entity);
        response.setTotalOrders(totalOrders);
        response.setTotalReviews(totalReviews);
        return response;
    }
}
