package com.badmintonshop.security;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when user tries to login with unverified email
 */
public class EmailNotVerifiedException extends AuthenticationException {
    
    private final String email;
    
    public EmailNotVerifiedException(String message, String email) {
        super(message);
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }
}
