package com.badmintonshop.dto.banner;

import com.badmintonshop.entity.enums.BannerPosition;
import com.badmintonshop.entity.enums.LinkTarget;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Banner - used for both public and admin APIs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerDTO {

    private Long bannerId;
    private String title;
    private String imageUrl;
    private String mobileImageUrl;
    private String linkUrl;
    private LinkTarget linkTarget;
    private BannerPosition position;
    private Integer displayOrder;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startsAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endsAt;

    private Boolean isActive;

    // Admin fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByName;
}
