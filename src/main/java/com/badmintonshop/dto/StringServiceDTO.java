package com.badmintonshop.dto;

import com.badmintonshop.entity.enums.StringingServiceType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StringServiceDTO {

    private Long serviceId;

    private String serviceName;
    private StringingServiceType serviceType;
    private String description;

    private BigDecimal basePrice;
    private Integer estimatedTimeMinutes;

    private Boolean isActive;
}
