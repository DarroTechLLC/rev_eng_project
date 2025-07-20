package com.darro_tech.revengproject.controllers;

import com.darro_tech.revengproject.dto.ForgotPasswordRequest;
import com.darro_tech.revengproject.dto.ForgotPasswordResponse;
import com.darro_tech.revengproject.dto.ResetPasswordRequest;
import com.darro_tech.revengproject.dto.ResetPasswordResponse;
import com.darro_tech.revengproject.services.ForgotPasswordService;
import com.darro_tech.revengproject.services.ResetPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for password reset functionality.
 * Matches NextJS API endpoints exactly.
 */
@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);
    
    @Autowired
    private ForgotPasswordService forgotPasswordService;
    
    @Autowired
    private ResetPasswordService resetPasswordService;
    
    /**
     * POST /api/auth/forgot-password
     * Matches NextJS forgot-password route exactly.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        logger.info("📧 Forgot password request received for: {}", request.getUsernameOrEmail());
        
        ForgotPasswordResponse response = forgotPasswordService.processForgotPasswordRequest(request.getUsernameOrEmail());
        
        logger.info("📧 Forgot password response: success={}, error={}", response.isSuccess(), response.getError());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/auth/reset-password
     * Matches NextJS reset-password route exactly.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        logger.info("🔐 Reset password request received for user: {}", request.getUsername());
        
        ResetPasswordResponse response = resetPasswordService.processResetPassword(
            request.getUsername(),
            request.getResetKey(),
            request.getNewPassword(),
            request.getConfirmPassword()
        );
        
        logger.info("🔐 Reset password response: success={}, error={}", response.isSuccess(), response.getError());
        
        return ResponseEntity.ok(response);
    }
} 