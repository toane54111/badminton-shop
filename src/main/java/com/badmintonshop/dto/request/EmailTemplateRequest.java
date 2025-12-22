package com.badmintonshop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating EmailTemplate
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateRequest {

    @NotBlank(message = "Template key is required")
    @Size(max = 100, message = "Template key must be at most 100 characters")
    private String templateKey;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @NotBlank(message = "Subject is required")
    @Size(max = 255, message = "Subject must be at most 255 characters")
    private String subject;

    @NotBlank(message = "Body HTML is required")
    private String bodyHtml;

    private String bodyText;

    private String variables; // JSON string: ["order_number", "customer_name", ...]

    @Builder.Default
    private Boolean isActive = true;
}
