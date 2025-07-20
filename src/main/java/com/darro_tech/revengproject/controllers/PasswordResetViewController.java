package com.darro_tech.revengproject.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * View controller for password reset pages.
 * Handles the frontend forms for forgot password and reset password.
 */
@Controller
public class PasswordResetViewController {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetViewController.class);
    
    /**
     * GET /forgot-password
     * Matches NextJS forgot-password page exactly.
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(@RequestParam(value = "u", required = false) String username, Model model) {
        logger.info("📧 Forgot password page accessed with username: {}", username);
        
        if (username != null) {
            model.addAttribute("username", username);
        }
        
        return "auth/forgot-password";
    }
    
    /**
     * GET /reset-password
     * Matches NextJS reset-password page exactly.
     */
    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam(value = "u", required = false) String username,
            @RequestParam(value = "k", required = false) String resetKey,
            Model model) {
        
        logger.info("🔐 Reset password page accessed with username: {}, resetKey: {}", username, resetKey);
        
        if (username != null) {
            model.addAttribute("username", username);
        }
        
        if (resetKey != null) {
            model.addAttribute("resetKey", resetKey);
        }
        
        return "auth/reset-password";
    }
} 