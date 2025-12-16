package com.badmintonshop.exception;

/**
 * Exception thrown when an email template is not found
 */
public class EmailTemplateNotFoundException extends RuntimeException {

    public EmailTemplateNotFoundException(String templateKey) {
        super("Email template not found: " + templateKey);
    }

    public EmailTemplateNotFoundException(Long id) {
        super("Email template not found with id: " + id);
    }
}
