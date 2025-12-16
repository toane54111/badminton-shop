package com.badmintonshop.dto.coupon;

import com.badmintonshop.entity.enums.CouponType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CouponValidationResponse {
    private boolean valid;
    private String code;
    private String name;
    private String description;
    private CouponType type;
    private BigDecimal discountAmount;
    private BigDecimal newTotal;
    private String errorMessage;
    
    // Static factory methods
    public static CouponValidationResponse valid(String code, String name, String description, 
            CouponType type, BigDecimal discountAmount, BigDecimal newTotal) {
        return CouponValidationResponse.builder()
                .valid(true)
                .code(code)
                .name(name)
                .description(description)
                .type(type)
                .discountAmount(discountAmount)
                .newTotal(newTotal)
                .build();
    }
    
    public static CouponValidationResponse invalid(String errorMessage) {
        return CouponValidationResponse.builder()
                .valid(false)
                .errorMessage(errorMessage)
                .build();
    }
}
