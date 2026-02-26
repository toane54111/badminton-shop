package com.badmintonshop.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestockNotificationDTO {
    private Long notificationId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Long variantId;
    private String variantName;
    private Boolean isSent;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
