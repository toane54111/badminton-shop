package com.badmintonshop.dto;

import com.badmintonshop.entity.enums.StringType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StringDTO {

    private Long stringId;

    private Long brandId;
    private String brandName;

    private String name;
    private String sku;
    private String description;

    private StringType stringType;
    private BigDecimal gauge;
    private String material;

    private Integer durabilityRating;
    private Integer repulsionRating;
    private Integer controlRating;
    private Integer hittingSoundRating;

    private BigDecimal recommendedTensionMin;
    private BigDecimal recommendedTensionMax;

    private BigDecimal retailPrice;

    private BigDecimal lengthPerRoll;
    private Integer quantityInStock;

    private String color;
    private String imageUrl;

    private Boolean isActive;
}
