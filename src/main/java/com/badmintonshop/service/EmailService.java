package com.badmintonshop.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service for sending emails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.from:noreply@badmintonshop.com}")
    private String fromEmail;

    @Value("${app.email.from-name:Badminton Shop}")
    private String fromName;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Send email verification email
     */
    @Async("emailExecutor")
    public void sendVerificationEmail(String toEmail, String fullName, String token) {
        try {
            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("verificationLink", baseUrl + "/verify-email?token=" + token);
            context.setVariable("baseUrl", baseUrl);

            String htmlContent = templateEngine.process("email/verification", context);
            
            sendHtmlEmail(toEmail, "Xác thực email - Badminton Shop", htmlContent);
            log.info("Verification email sent to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
        }
    }

    /**
     * Send password reset email
     */
    @Async("emailExecutor")
    public void sendPasswordResetEmail(String toEmail, String fullName, String token) {
        try {
            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("resetLink", baseUrl + "/reset-password?token=" + token);
            context.setVariable("baseUrl", baseUrl);

            String htmlContent = templateEngine.process("email/password-reset", context);
            
            sendHtmlEmail(toEmail, "Đặt lại mật khẩu - Badminton Shop", htmlContent);
            log.info("Password reset email sent to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    /**
     * Send HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        // Format: "Badminton Shop <email@gmail.com>"
        String fromWithName = String.format("%s <%s>", fromName, fromEmail);
        helper.setFrom(fromWithName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
}
