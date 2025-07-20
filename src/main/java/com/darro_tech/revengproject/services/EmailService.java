package com.darro_tech.revengproject.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

/**
 * Email service for sending password reset emails.
 * Uses anthony.millersr82@gmail.com as the sender email address.
 */
@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Value("${spring.mail.username:anthony.millersr82@gmail.com}")
    private String fromEmail;
    
    @Value("${app.url:http://localhost:8080}")
    private String appUrl;
    
    @Value("${spring.mail.password:}")
    private String emailPassword;
    
    /**
     * Check if email is properly configured
     */
    private boolean isEmailConfigured() {
        return emailPassword != null && !emailPassword.trim().isEmpty();
    }
    
    /**
     * Send a simple text email
     */
    public boolean sendSimpleEmail(String to, String subject, String text) {
        if (!isEmailConfigured()) {
            logger.warn("📧 Email not configured - logging instead of sending");
            logger.info("📧 EMAIL CONTENT (not sent):");
            logger.info("📧 To: {}", to);
            logger.info("📧 Subject: {}", subject);
            logger.info("📧 Text: {}", text);
            return true; // Return true to avoid breaking the flow
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            logger.info("📧 Email sent successfully to: {}", to);
            return true;
        } catch (Exception e) {
            logger.error("❌ Failed to send email to: {}", to, e);
            return false;
        }
    }
    
    /**
     * Send an HTML email using Thymeleaf template
     */
    public boolean sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        if (!isEmailConfigured()) {
            logger.warn("📧 Email not configured - logging instead of sending");
            logger.info("📧 EMAIL CONTENT (not sent):");
            logger.info("📧 To: {}", to);
            logger.info("📧 Subject: {}", subject);
            logger.info("📧 Template: {}", templateName);
            logger.info("📧 Variables: {}", variables);
            return true; // Return true to avoid breaking the flow
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            
            // Process Thymeleaf template
            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }
            String htmlContent = templateEngine.process(templateName, context);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("📧 HTML email sent successfully to: {}", to);
            return true;
        } catch (MessagingException e) {
            logger.error("❌ Failed to send HTML email to: {}", to, e);
            return false;
        }
    }
    
    /**
     * Send forgot password email
     */
    public boolean sendForgotPasswordEmail(String to, String username, String resetKey) {
        String subject = "Roeslein / Forgot your password?";
        
        // Create variables for template
        Map<String, Object> variables = Map.of(
            "username", username,
            "resetKey", resetKey,
            "resetUrl", appUrl + "/reset-password?u=" + username + "&k=" + resetKey
        );
        
        return sendHtmlEmail(to, subject, "emails/forgot-password-email", variables);
    }
    
    /**
     * Send password reset confirmation email
     */
    public boolean sendPasswordResetConfirmationEmail(String to, String username) {
        String subject = "Roeslein / Password Reset Confirmation";
        
        Map<String, Object> variables = Map.of(
            "username", username
        );
        
        return sendHtmlEmail(to, subject, "emails/password-reset-confirmation", variables);
    }
}
