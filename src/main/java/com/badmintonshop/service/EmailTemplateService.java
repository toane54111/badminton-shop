package com.badmintonshop.service;

import com.badmintonshop.dto.request.EmailTemplateRequest;
import com.badmintonshop.dto.response.EmailTemplateResponse;
import com.badmintonshop.entity.EmailTemplate;
import com.badmintonshop.exception.EmailTemplateNotFoundException;
import com.badmintonshop.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for managing email templates
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmailTemplateService {

    private final EmailTemplateRepository templateRepository;
    private final TemplateEngine templateEngine;
    
    // Pattern to match {{variable}} placeholders
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");

    /**
     * Get all email templates
     */
    public List<EmailTemplateResponse> getAll() {
        log.debug("Fetching all email templates");
        return templateRepository.findAll().stream()
                .map(EmailTemplateResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all active email templates
     */
    public List<EmailTemplateResponse> getAllActive() {
        log.debug("Fetching all active email templates");
        return templateRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(EmailTemplateResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get template by ID
     */
    public EmailTemplateResponse getById(Long id) {
        log.debug("Fetching email template by id: {}", id);
        return templateRepository.findById(id)
                .map(EmailTemplateResponse::fromEntity)
                .orElseThrow(() -> new EmailTemplateNotFoundException(id));
    }

    /**
     * Get template by key
     */
    public EmailTemplateResponse getByKey(String key) {
        log.debug("Fetching email template by key: {}", key);
        return templateRepository.findByTemplateKey(key)
                .map(EmailTemplateResponse::fromEntity)
                .orElseThrow(() -> new EmailTemplateNotFoundException(key));
    }

    /**
     * Create new email template
     */
    @Transactional
    public EmailTemplateResponse create(EmailTemplateRequest request) {
        log.info("Creating new email template: {}", request.getTemplateKey());
        
        // Check for duplicate key
        if (templateRepository.existsByTemplateKey(request.getTemplateKey())) {
            throw new IllegalArgumentException("Template key already exists: " + request.getTemplateKey());
        }
        
        EmailTemplate template = EmailTemplate.builder()
                .templateKey(request.getTemplateKey())
                .name(request.getName())
                .subject(request.getSubject())
                .bodyHtml(request.getBodyHtml())
                .bodyText(request.getBodyText())
                .variables(request.getVariables())
                .isActive(request.getIsActive())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        EmailTemplate saved = templateRepository.save(template);
        log.info("Created email template with id: {}", saved.getTemplateId());
        return EmailTemplateResponse.fromEntity(saved);
    }

    /**
     * Update existing email template
     */
    @Transactional
    public EmailTemplateResponse update(Long id, EmailTemplateRequest request) {
        log.info("Updating email template: {}", id);
        
        EmailTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new EmailTemplateNotFoundException(id));
        
        // Check for duplicate key (excluding current template)
        if (!template.getTemplateKey().equals(request.getTemplateKey()) &&
                templateRepository.existsByTemplateKeyAndTemplateIdNot(request.getTemplateKey(), id)) {
            throw new IllegalArgumentException("Template key already exists: " + request.getTemplateKey());
        }
        
        template.setTemplateKey(request.getTemplateKey());
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setBodyHtml(request.getBodyHtml());
        template.setBodyText(request.getBodyText());
        template.setVariables(request.getVariables());
        template.setIsActive(request.getIsActive());
        template.setUpdatedAt(LocalDateTime.now());
        
        EmailTemplate saved = templateRepository.save(template);
        log.info("Updated email template: {}", saved.getTemplateKey());
        return EmailTemplateResponse.fromEntity(saved);
    }

    /**
     * Delete email template
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting email template: {}", id);
        
        if (!templateRepository.existsById(id)) {
            throw new EmailTemplateNotFoundException(id);
        }
        
        templateRepository.deleteById(id);
        log.info("Deleted email template with id: {}", id);
    }

    /**
     * Render template with variables
     * Replaces {{variable}} with actual values
     */
    public String renderTemplate(String templateKey, Map<String, Object> variables) {
        log.debug("Rendering template: {} with {} variables", templateKey, variables.size());
        
        EmailTemplate template = templateRepository.findByTemplateKey(templateKey)
                .orElseThrow(() -> new EmailTemplateNotFoundException(templateKey));
        
        String content = template.getBodyHtml();
        
        // Replace {{variable}} placeholders
        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = variables.getOrDefault(variableName, "");
            matcher.appendReplacement(result, Matcher.quoteReplacement(String.valueOf(value)));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    /**
     * Render template subject with variables
     */
    public String renderSubject(String templateKey, Map<String, Object> variables) {
        EmailTemplate template = templateRepository.findByTemplateKey(templateKey)
                .orElseThrow(() -> new EmailTemplateNotFoundException(templateKey));
        
        String subject = template.getSubject();
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(subject);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = variables.getOrDefault(variableName, "");
            matcher.appendReplacement(result, Matcher.quoteReplacement(String.valueOf(value)));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    /**
     * Preview template with sample data
     */
    public Map<String, String> preview(String templateKey, Map<String, Object> sampleData) {
        return Map.of(
                "subject", renderSubject(templateKey, sampleData),
                "body", renderTemplate(templateKey, sampleData)
        );
    }

    /**
     * Toggle template active status
     */
    @Transactional
    public EmailTemplateResponse toggleActive(Long id) {
        EmailTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new EmailTemplateNotFoundException(id));
        
        template.setIsActive(!template.getIsActive());
        template.setUpdatedAt(LocalDateTime.now());
        
        return EmailTemplateResponse.fromEntity(templateRepository.save(template));
    }
}
