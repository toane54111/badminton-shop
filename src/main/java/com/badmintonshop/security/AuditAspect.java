package com.badmintonshop.security;

import com.badmintonshop.entity.Staff;
import com.badmintonshop.entity.enums.ActivityAction;
import com.badmintonshop.repository.StaffRepository;
import com.badmintonshop.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * AOP Aspect for automatic audit logging
 * Intercepts methods annotated with @Auditable
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
    private final StaffRepository staffRepository;

    /**
     * Log activity after method returns successfully
     */
    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void logActivity(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            Staff staff = getCurrentStaff();
            HttpServletRequest request = getCurrentRequest();
            
            Long entityId = extractEntityId(joinPoint, result);
            String description = buildDescription(auditable, joinPoint);

            auditService.logActivity(
                    staff,
                    auditable.action(),
                    auditable.entityType(),
                    entityId,
                    description,
                    null, // oldValues - can be captured with @Before if needed
                    result,
                    request
            );
            
            log.debug("Audit logged: {} {} {}", auditable.action(), auditable.entityType(), entityId);
        } catch (Exception e) {
            log.error("Failed to log audit activity", e);
        }
    }

    /**
     * Get current authenticated staff
     */
    private Staff getCurrentStaff() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        
        String email = null;
        
        // Handle different authentication types
        if (auth.getPrincipal() instanceof CustomUserDetails) {
            email = ((CustomUserDetails) auth.getPrincipal()).getUsername(); // username is email
        } else if (auth.getPrincipal() instanceof String) {
            email = (String) auth.getPrincipal();
        } else {
            email = auth.getName();
        }
        
        return email != null ? staffRepository.findByEmail(email).orElse(null) : null;
    }

    /**
     * Get current HTTP request
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /**
     * Extract entity ID from method arguments or return value
     */
    private Long extractEntityId(JoinPoint joinPoint, Object result) {
        // Try to extract from result first
        if (result != null) {
            try {
                Method getIdMethod = result.getClass().getMethod("getTemplateId");
                Object id = getIdMethod.invoke(result);
                if (id instanceof Long) return (Long) id;
            } catch (Exception ignored) {}
            
            try {
                Method getIdMethod = result.getClass().getMethod("getSettingId");
                Object id = getIdMethod.invoke(result);
                if (id instanceof Long) return (Long) id;
            } catch (Exception ignored) {}
            
            try {
                Method getIdMethod = result.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(result);
                if (id instanceof Long) return (Long) id;
            } catch (Exception ignored) {}
        }
        
        // Try to extract from method arguments
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            if (args[0] instanceof Long) {
                return (Long) args[0];
            }
        }
        
        return null;
    }

    /**
     * Build description from annotation and method parameters
     */
    private String buildDescription(Auditable auditable, JoinPoint joinPoint) {
        String template = auditable.description();
        if (template.isEmpty()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            return String.format("%s %s via %s", 
                    auditable.action(), 
                    auditable.entityType(),
                    signature.getName());
        }
        
        // Replace {0}, {1}, etc. with actual arguments
        Object[] args = joinPoint.getArgs();
        String description = template;
        for (int i = 0; i < args.length; i++) {
            description = description.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return description;
    }
}
