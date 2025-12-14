package com.badmintonshop.dto.response;

import com.badmintonshop.entity.EmailTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for EmailTemplate
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateResponse {

    private Long templateId;
    private String templateKey;
    private String name;
    private String subject;
    private String bodyHtml;
    private String bodyText;
    private String variables; // JSON string of available variables
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert from entity to response DTO
     */
    public static EmailTemplateResponse fromEntity(EmailTemplate entity) {
        return EmailTemplateResponse.builder()
                .templateId(entity.getTemplateId())
                .templateKey(entity.getTemplateKey())
                .name(entity.getName())
                .subject(entity.getSubject())
                .bodyHtml(entity.getBodyHtml())
                .bodyText(entity.getBodyText())
                .variables(entity.getVariables())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
