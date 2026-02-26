package com.badmintonshop.security;

import com.badmintonshop.entity.enums.ActivityAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for audit logging
 * Used with AuditAspect for automatic activity logging
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * The type of entity being audited (e.g., "Product", "Order", "Coupon")
     */
    String entityType();

    /**
     * The action being performed
     */
    ActivityAction action();

    /**
     * Optional description template
     * Can use {0}, {1}, etc. for method parameters
     */
    String description() default "";
}
