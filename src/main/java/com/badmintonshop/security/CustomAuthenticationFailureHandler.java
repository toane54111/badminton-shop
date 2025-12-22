package com.badmintonshop.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Custom authentication failure handler
 * Handles different types of authentication failures including email verification
 */
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        
        String redirectUrl = "/login?error=true";

        // Check if the exception or its cause is EmailNotVerifiedException
        if (exception instanceof EmailNotVerifiedException || (exception.getCause() instanceof EmailNotVerifiedException)) {
            EmailNotVerifiedException emailException = (exception instanceof EmailNotVerifiedException) 
                    ? (EmailNotVerifiedException) exception 
                    : (EmailNotVerifiedException) exception.getCause();
            
            String email = URLEncoder.encode(emailException.getEmail(), StandardCharsets.UTF_8);
            redirectUrl = "/verification-required?email=" + email;
        }
        
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
