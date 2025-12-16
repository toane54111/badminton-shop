package com.badmintonshop.dto.profile;

import com.badmintonshop.entity.enums.Gender;
import com.badmintonshop.entity.enums.PlayingStyle;
import com.badmintonshop.entity.enums.SkillLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class UserProfileResponse {
    private Long userId;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private Gender gender;
    
    // Playing Profile
    private PlayingStyle playingStyle;
    private SkillLevel skillLevel;
    private String preferredRacketWeight;
    private BigDecimal preferredTensionMin;
    private BigDecimal preferredTensionMax;
    private String preferredStringType;
}
