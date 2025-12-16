package com.badmintonshop.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlertDTO {
    private Long alertId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal targetPrice;
    private BigDecimal currentPrice;
    private Boolean isActive;
    private Boolean isTriggered;
    private LocalDateTime triggeredAt;
    private LocalDateTime createdAt;
}
