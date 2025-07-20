package com.darro_tech.revengproject.services;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darro_tech.revengproject.dto.ForgotPasswordResponse;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.UserContactInfo;
import com.darro_tech.revengproject.models.UserContactType;
import com.darro_tech.revengproject.repositories.UserContactInfoRepository;
import com.darro_tech.revengproject.repositories.UserContactTypeRepository;
import com.darro_tech.revengproject.repositories.UserRepository;
import com.darro_tech.revengproject.utils.PasswordResetKeyGenerator;

/**
 * Service for handling forgot password functionality. Matches the NextJS
 * forgot-password-command.ts logic exactly.
 */
@Service
public class ForgotPasswordService {

    private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserContactInfoRepository userContactInfoRepository;

    @Autowired
    private UserContactTypeRepository userContactTypeRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Process forgot password request. Matches NextJS forgotPasswordCommand
     * logic exactly.
     */
    public ForgotPasswordResponse processForgotPasswordRequest(String usernameOrEmail) {
        logger.info("🔐 Processing forgot password request for: {}", usernameOrEmail);

        try {
            // Find user by username or email
            Optional<User> userOpt = findUserByUsernameOrEmail(usernameOrEmail);

            if (userOpt.isEmpty()) {
                // Don't reveal that no user is found (security best practice)
                logger.info("🔒 No user found for: {} (returning success for security)", usernameOrEmail);
                return new ForgotPasswordResponse(true);
            }

            User user = userOpt.get();
            String email = getUserEmail(user.getId());

            if (email == null || email.trim().isEmpty()) {
                logger.warn("❌ No email found for user: {}", user.getUsername());
                return new ForgotPasswordResponse(false, "no-email");
            }

            String username = user.getUsername();
            if (username == null || username.trim().isEmpty()) {
                logger.error("❌ No username found for user ID: {}", user.getId());
                return new ForgotPasswordResponse(false, "internal");
            }

            // Generate reset key and expiration
            String resetPassKey = PasswordResetKeyGenerator.generatePassKey();
            Instant resetPassExpires = Instant.now().plusSeconds(30 * 60); // 30 minutes

            // Update user with reset key and expiration
            user.setResetPassKey(resetPassKey);
            user.setResetPassExpires(resetPassExpires);
            userRepository.save(user);

            logger.info("✅ Reset key generated for user: {}", username);

            // Send email
            boolean emailSent = emailService.sendForgotPasswordEmail(email, username, resetPassKey);

            if (!emailSent) {
                logger.error("❌ Failed to send email to: {}", email);
                return new ForgotPasswordResponse(false, "send-email");
            }

            logger.info("📧 Password reset email sent successfully to: {}", email);
            return new ForgotPasswordResponse(true);

        } catch (Exception e) {
            logger.error("❌ Internal error processing forgot password request", e);
            return new ForgotPasswordResponse(false, "internal");
        }
    }

    /**
     * Find user by username or email. Matches NextJS
     * user_contact_view.findFirst logic.
     */
    private Optional<User> findUserByUsernameOrEmail(String usernameOrEmail) {
        // First try by username
        Optional<User> userByUsername = userRepository.findByUsername(usernameOrEmail);
        if (userByUsername.isPresent()) {
            return userByUsername;
        }

        // Then try by email
        return findUserByEmail(usernameOrEmail);
    }

    /**
     * Find user by email address.
     */
    private Optional<User> findUserByEmail(String email) {
        // Get email contact type
        UserContactType emailType = userContactTypeRepository.findByName("Email");
        if (emailType == null) {
            logger.warn("⚠️ Email contact type not found in database");
            return Optional.empty();
        }

        // Find user contact info by email - we need to use a different approach
        // since the repository doesn't have findByValueAndType
        return findUserByEmailValue(email, emailType);
    }

    /**
     * Find user by email value using the contact type.
     */
    private Optional<User> findUserByEmailValue(String email, UserContactType emailType) {
        // We'll need to use a custom query or iterate through results
        // For now, let's use a simple approach by getting all contact info for this type
        // and finding the one with matching email
        List<UserContactInfo> allEmailContacts = userContactInfoRepository.findAll().stream()
                .filter(contact -> contact.getType().getId().equals(emailType.getId()))
                .filter(contact -> contact.getValue().equals(email))
                .toList();

        if (allEmailContacts.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(allEmailContacts.get(0).getUser());
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
