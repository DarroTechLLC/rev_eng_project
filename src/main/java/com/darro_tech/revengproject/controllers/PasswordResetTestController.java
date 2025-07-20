package com.darro_tech.revengproject.controllers;

import com.darro_tech.revengproject.dto.ForgotPasswordResponse;
import com.darro_tech.revengproject.dto.ResetPasswordResponse;
import com.darro_tech.revengproject.services.ForgotPasswordService;
import com.darro_tech.revengproject.services.ResetPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for password reset functionality.
 * This is for testing purposes only.
 */
@RestController
@RequestMapping("/api/test")
public class PasswordResetTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetTestController.class);
    
    @Autowired
    private ForgotPasswordService forgotPasswordService;
    
    @Autowired
    private ResetPasswordService resetPasswordService;
    
    /**
     * Test forgot password functionality
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> testForgotPassword(@RequestBody Map<String, String> request) {
        String usernameOrEmail = request.get("usernameOrEmail");
        
        logger.info("🧪 Testing forgot password for: {}", usernameOrEmail);
        
        ForgotPasswordResponse response = forgotPasswordService.processForgotPasswordRequest(usernameOrEmail);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", response.isSuccess());
        result.put("error", response.getError());
        result.put("message", response.isSuccess() ? 
            "Password reset request processed successfully. Check logs for email content." :
            "Password reset request failed: " + response.getError());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Test reset password functionality
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> testResetPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String resetKey = request.get("resetKey");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");
        
        logger.info("🧪 Testing reset password for user: {}", username);
        
        ResetPasswordResponse response = resetPasswordService.processResetPassword(
            username, resetKey, newPassword, confirmPassword
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", response.isSuccess());
        result.put("error", response.getError());
        result.put("message", response.isSuccess() ? 
            "Password reset completed successfully." :
            "Password reset failed: " + response.getError());
        
        return ResponseEntity.ok(result);
    }
} 