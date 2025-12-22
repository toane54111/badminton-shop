package com.badmintonshop.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic DTO for authentication responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private boolean success;
    private String message;
    private String redirectUrl;
    
    // User info (optional, for successful login)
    private Long userId;
    private String email;
    private String fullName;
    private String avatarUrl;

    // Static factory methods for common responses
    public static AuthResponse success(String message) {
        return AuthResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static AuthResponse success(String message, String redirectUrl) {
        return AuthResponse.builder()
                .success(true)
                .message(message)
                .redirectUrl(redirectUrl)
                .build();
    }

    public static AuthResponse error(String message) {
        return AuthResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
