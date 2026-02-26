package com.badmintonshop.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation to verify password and confirmPassword match
 */
@Documented
@Constraint(validatedBy = PasswordMatchValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatch {
    
    String message() default "Mật khẩu xác nhận không khớp";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
