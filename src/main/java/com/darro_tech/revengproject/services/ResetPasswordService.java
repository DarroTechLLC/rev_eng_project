package com.darro_tech.revengproject.services;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darro_tech.revengproject.dto.ResetPasswordResponse;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.UserContactInfo;
import com.darro_tech.revengproject.models.UserContactType;
import com.darro_tech.revengproject.repositories.UserContactInfoRepository;
import com.darro_tech.revengproject.repositories.UserContactTypeRepository;
import com.darro_tech.revengproject.repositories.UserRepository;

/**
 * Service for handling password reset functionality. Matches the NextJS
 * reset-password-command.ts logic exactly.
 */
@Service
public class ResetPasswordService {

    private static final Logger logger = LoggerFactory.getLogger(ResetPasswordService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserContactInfoRepository userContactInfoRepository;

    @Autowired
    private UserContactTypeRepository userContactTypeRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Process password reset request. Matches NextJS resetPasswordCommand logic
     * exactly.
     */
    public ResetPasswordResponse processResetPassword(String username, String resetKey, String newPassword, String confirmPassword) {
        logger.info("🔐 Processing password reset for user: {}", username);

        try {
            // Find user by username
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                logger.error("❌ User not found: {}", username);
                return new ResetPasswordResponse(false, "invalid-key");
            }

            User user = userOpt.get();
            String userId = user.getId();

            // Validate reset key
            if (resetKey == null || resetKey.trim().isEmpty()) {
                logger.error("❌ Empty reset key for user: {}", username);
                return new ResetPasswordResponse(false, "invalid-key");
            }

            String storedResetKey = user.getResetPassKey();
            if (storedResetKey == null || !storedResetKey.equals(resetKey)) {
                logger.error("❌ Invalid reset key for user: {} (provided: {}, stored: {})", username, resetKey, storedResetKey);
                return new ResetPasswordResponse(false, "invalid-key");
            }

            // Check expiration
            Instant resetPassExpires = user.getResetPassExpires();
            if (resetPassExpires != null && resetPassExpires.isBefore(Instant.now())) {
                logger.error("❌ Reset key expired for user: {}", username);
                return new ResetPasswordResponse(false, "expired-key");
            }

            // Validate new password
            if (newPassword == null || newPassword.trim().isEmpty()) {
                logger.error("❌ Empty password for user: {}", username);
                return new ResetPasswordResponse(false, "password-empty");
            }

            if (!newPassword.equals(confirmPassword)) {
                logger.error("❌ Password mismatch for user: {}", username);
                return new ResetPasswordResponse(false, "password-mismatch");
            }

            // Update password using the same hashing algorithm as User entity
            user.setPassword(newPassword);

            // Clear reset key and expiration
            user.setResetPassKey(null);
            user.setResetPassExpires(null);

            userRepository.save(user);
            logger.info("✅ Password updated successfully for user: {}", username);

            // Send confirmation email
            String email = getUserEmail(userId);
            if (email == null || email.trim().isEmpty()) {
                logger.error("❌ No email found for user: {}", username);
                return new ResetPasswordResponse(false, "no-email");
            }

            boolean emailSent = emailService.sendPasswordResetConfirmationEmail(email, username);
            if (!emailSent) {
                logger.error("❌ Failed to send confirmation email to: {}", email);
                return new ResetPasswordResponse(false, "send-email");
            }

            logger.info("📧 Password reset confirmation email sent successfully to: {}", email);
            return new ResetPasswordResponse(true);

        } catch (Exception e) {
            logger.error("❌ Internal error processing password reset", e);
            return new ResetPasswordResponse(false, "internal");
        }
    }

    /**
     * Get user's email address.
     */
    private String getUserEmail(String userId) {
        // Get email contact type
        UserContactType emailType = userContactTypeRepository.findByName("Email");
        if (emailType == null) {
            return null;
        }

        // Find user's email contact info
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return null;
        }

        UserContactInfo contactInfo = userContactInfoRepository.findByUserAndType(user.get(), emailType);
        if (contactInfo == null) {
            return null;
        }

        return contactInfo.getValue();
    }
}
