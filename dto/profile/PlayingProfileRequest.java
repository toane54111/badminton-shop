package com.badmintonshop.dto.profile;

import com.badmintonshop.entity.enums.PlayingStyle;
import com.badmintonshop.entity.enums.SkillLevel;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlayingProfileRequest {
    private PlayingStyle playingStyle;
    
    private SkillLevel skillLevel;
    
    private String preferredRacketWeight;
    
    private BigDecimal preferredTensionMin;
    
    private BigDecimal preferredTensionMax;
    
    private String preferredStringType;
}
