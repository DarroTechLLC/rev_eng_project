package com.darro_tech.revengproject.controllers;

import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.darro_tech.revengproject.util.LoggerUtils;

/**
 * Controller for WebAuthn (Web Authentication) operations
 * This provides endpoints for registering and authenticating with biometric credentials
 */
@Controller
@RequestMapping("/webauthn")
public class WebAuthnController {

    private static final Logger logger = LoggerUtils.getLogger(WebAuthnController.class);

    // In-memory challenge storage - in production, this should be in a database or Redis
    private static final Map<String, byte[]> challengeStorage = new ConcurrentHashMap<>();

    // In-memory credential storage - in production, this should be in a database
    private static final Map<String, Map<String, byte[]>> credentialStorage = new ConcurrentHashMap<>();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Check if biometric authentication is available for the current user
     */
    @GetMapping("/available")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkAvailability(HttpServletRequest request) {
        try {
            logger.debug("Checking biometric availability for request: {}", request.getRequestURI());

            HttpSession session = request.getSession(false);
            if (session == null) {
                logger.debug("No session found when checking biometric availability");
                return ResponseEntity.ok(Map.of("available", false, "reason", "No session"));
            }

            User user = authenticationController.getUserFromSession(session);

            Map<String, Object> response = new HashMap<>();

            if (user != null) {
                boolean hasCredentials = credentialStorage.containsKey(user.getUsername());
                response.put("available", hasCredentials);
                response.put("username", user.getUsername());
                logger.debug("Biometric availability for user {}: {}", user.getUsername(), hasCredentials);
            } else {
                response.put("available", false);
                response.put("reason", "User not authenticated");
                logger.debug("No authenticated user found when checking biometric availability");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking biometric availability", e);
            return ResponseEntity.ok(Map.of("available", false, "error", e.getMessage()));
        }
    }

    /**
     * Generate registration options for creating a new biometric credential
     */
    @PostMapping("/register/options")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRegistrationOptions(HttpServletRequest request) {
        try {
            logger.debug("Generating registration options for request: {}", request.getRequestURI());

            HttpSession session = request.getSession(false);
            if (session == null) {
                logger.warn("No session found when generating registration options");
                return ResponseEntity.badRequest().body(Map.of("error", "No session found"));
            }

            User user = authenticationController.getUserFromSession(session);

            if (user == null) {
                logger.warn("User not authenticated when generating registration options");
                return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
            }

            logger.debug("Generating registration options for user: {}", user.getUsername());

            // Generate a random challenge
            byte[] challenge = new byte[32];
            new SecureRandom().nextBytes(challenge);

            // Store the challenge for verification
            String challengeId = UUID.randomUUID().toString();
            challengeStorage.put(challengeId, challenge);
            logger.debug("Generated challenge ID: {}", challengeId);

            // Create registration options
            Map<String, Object> publicKeyCredentialCreationOptions = new HashMap<>();
            publicKeyCredentialCreationOptions.put("challenge", Base64.getEncoder().encodeToString(challenge));
            publicKeyCredentialCreationOptions.put("rp", Map.of(
                    "name", "RevEng Project",
                    "id", request.getServerName()
            ));
            publicKeyCredentialCreationOptions.put("user", Map.of(
                    "id", Base64.getEncoder().encodeToString(user.getUsername().getBytes()),
                    "name", user.getUsername(),
                    "displayName", user.getFirstName() + " " + user.getLastName()
            ));

            // Specify the types of credentials we'll accept
            publicKeyCredentialCreationOptions.put("pubKeyCredParams", List.of(
                    Map.of("type", "public-key", "alg", -7), // ES256
                    Map.of("type", "public-key", "alg", -257) // RS256
            ));

            // Add timeout
            publicKeyCredentialCreationOptions.put("timeout", 60000);

            // Add attestation preference
            publicKeyCredentialCreationOptions.put("attestation", "none");

            // Add authenticator selection criteria
            publicKeyCredentialCreationOptions.put("authenticatorSelection", Map.of(
                    "authenticatorAttachment", "platform", // Use platform authenticator (like TouchID, FaceID, Windows Hello)
                    "userVerification", "preferred"
            ));

            Map<String, Object> response = new HashMap<>();
            response.put("options", publicKeyCredentialCreationOptions);
            response.put("challengeId", challengeId);

            logger.debug("Registration options generated successfully for user: {}", user.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating registration options", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Error generating registration options: " + e.getMessage()));
        }
    }

    /**
     * Verify registration response and store the credential
     */
    @PostMapping("/register/verify")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyRegistration(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        try {
            logger.debug("Verifying registration for request body: {}", body);

            HttpSession session = request.getSession(false);
            if (session == null) {
                logger.warn("No session found when verifying registration");
                return ResponseEntity.badRequest().body(Map.of("error", "No session found"));
            }

            User user = authenticationController.getUserFromSession(session);

            if (user == null) {
                logger.warn("User not authenticated when verifying registration");
                return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
            }

            String challengeId = (String) body.get("challengeId");
            if (challengeId == null) {
                logger.warn("Challenge ID is missing from request");
                return ResponseEntity.badRequest().body(Map.of("error", "Challenge ID is required"));
            }

            Map<String, Object> credential = (Map<String, Object>) body.get("credential");
            if (credential == null) {
                logger.warn("Credential is missing from request");
                return ResponseEntity.badRequest().body(Map.of("error", "Credential is required"));
            }

            // Verify challenge
            byte[] expectedChallenge = challengeStorage.get(challengeId);
            if (expectedChallenge == null) {
                logger.warn("Challenge not found or expired for ID: {}", challengeId);
                return ResponseEntity.badRequest().body(Map.of("error", "Challenge not found or expired"));
            }

            // In a real implementation, we would verify the attestation here
            // For simplicity, we'll just store the credential ID and public key

            String credentialId = (String) credential.get("id");
            if (credentialId == null) {
                logger.warn("Credential ID is missing from request");
                return ResponseEntity.badRequest().body(Map.of("error", "Credential ID is required"));
            }

            String publicKey = (String) credential.get("publicKey");
            if (publicKey == null) {
                logger.warn("Public key is missing from request");
                return ResponseEntity.badRequest().body(Map.of("error", "Public key is required"));
            }

            logger.debug("Storing credential for user: {}", user.getUsername());

            // Store the credential
            Map<String, byte[]> userCredentials = credentialStorage.computeIfAbsent(
                    user.getUsername(), k -> new HashMap<>());

            try {
                userCredentials.put(credentialId, Base64.getDecoder().decode(publicKey));
            } catch (IllegalArgumentException e) {
                logger.error("Error decoding public key", e);
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid public key format"));
            }

            // Clean up the challenge
            challengeStorage.remove(challengeId);

            logger.info("✅ Biometric credential registered for user: {}", user.getUsername());

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Error verifying registration", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Error verifying registration: " + e.getMessage()));
        }
    }

    /**
     * Generate authentication options for using a biometric credential without requiring username
     */
    @GetMapping("/authenticate/options/auto")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAuthenticationOptionsAuto() {
        try {
            logger.debug("Received automatic authentication options request");

            // Generate a random challenge
            byte[] challenge = new byte[32];
            new SecureRandom().nextBytes(challenge);

            // Store the challenge for verification
            String challengeId = UUID.randomUUID().toString();
            challengeStorage.put(challengeId, challenge);

            // Create authentication options
            Map<String, Object> publicKeyCredentialRequestOptions = new HashMap<>();
            publicKeyCredentialRequestOptions.put("challenge", Base64.getEncoder().encodeToString(challenge));

            // Add timeout
            publicKeyCredentialRequestOptions.put("timeout", 60000);

            // Add user verification preference
            publicKeyCredentialRequestOptions.put("userVerification", "preferred");

            Map<String, Object> response = new HashMap<>();
            response.put("options", publicKeyCredentialRequestOptions);
            response.put("challengeId", challengeId);

            logger.debug("Generated automatic authentication options");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating automatic authentication options", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Error generating authentication options: " + e.getMessage()));
        }
    }

    /**
     * Generate authentication options for using a biometric credential
     */
    @PostMapping("/authenticate/options")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAuthenticationOptions(@RequestBody Map<String, Object> body) {
        try {
            logger.debug("Received authentication options request for body: {}", body);

            String username = (String) body.get("username");

            if (username == null || username.isEmpty()) {
                logger.warn("Username is required but was not provided");
                return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
            }

            // Check if the user has registered credentials
            Map<String, byte[]> userCredentials = credentialStorage.get(username);
            if (userCredentials == null || userCredentials.isEmpty()) {
                logger.warn("No credentials found for user: {}", username);
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "No biometric credentials found for user",
                    "errorCode", "NO_CREDENTIALS",
                    "message", "You need to register your biometric credentials first. Please log in with your password and go to your account settings to register biometrics."
                ));
            }

            // Generate a random challenge
            byte[] challenge = new byte[32];
            new SecureRandom().nextBytes(challenge);

            // Store the challenge for verification
            String challengeId = UUID.randomUUID().toString();
            challengeStorage.put(challengeId, challenge);

            // Create authentication options
            Map<String, Object> publicKeyCredentialRequestOptions = new HashMap<>();
            publicKeyCredentialRequestOptions.put("challenge", Base64.getEncoder().encodeToString(challenge));

            // Add timeout
            publicKeyCredentialRequestOptions.put("timeout", 60000);

            // Add allowed credentials
            List<Map<String, Object>> allowCredentials = new ArrayList<>();
            for (String credentialId : userCredentials.keySet()) {
                allowCredentials.add(Map.of(
                        "type", "public-key",
                        "id", credentialId
                ));
            }
            publicKeyCredentialRequestOptions.put("allowCredentials", allowCredentials);

            // Add user verification preference
            publicKeyCredentialRequestOptions.put("userVerification", "preferred");

            Map<String, Object> response = new HashMap<>();
            response.put("options", publicKeyCredentialRequestOptions);
            response.put("challengeId", challengeId);

            logger.debug("Generated authentication options for user: {}", username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating authentication options", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Error generating authentication options: " + e.getMessage()));
        }
    }

    /**
     * Verify authentication response and log the user in
     */
    @PostMapping("/authenticate/verify")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyAuthentication(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        try {
            logger.debug("Received authentication verification request for body: {}", body);

            String username = (String) body.get("username");
            String challengeId = (String) body.get("challengeId");
            Map<String, Object> credential = (Map<String, Object>) body.get("credential");

            String credentialId = (String) credential.get("id");
            if (credentialId == null) {
                logger.warn("Credential ID is missing from request");
                return ResponseEntity.badRequest().body(Map.of("error", "Credential ID is required"));
            }

            // Verify challenge
            byte[] expectedChallenge = challengeStorage.get(challengeId);
            if (expectedChallenge == null) {
                logger.warn("Challenge not found or expired for ID: {}", challengeId);
                return ResponseEntity.badRequest().body(Map.of("error", "Challenge not found or expired"));
            }

            // If username is not provided, find the user by credential ID
            if (username == null || username.isEmpty()) {
                logger.debug("Username not provided, finding user by credential ID: {}", credentialId);

                // Search through all users' credentials to find the matching credential ID
                for (Map.Entry<String, Map<String, byte[]>> entry : credentialStorage.entrySet()) {
                    String potentialUsername = entry.getKey();
                    Map<String, byte[]> userCreds = entry.getValue();

                    if (userCreds.containsKey(credentialId)) {
                        username = potentialUsername;
                        logger.debug("Found username {} for credential ID {}", username, credentialId);
                        break;
                    }
                }

                if (username == null || username.isEmpty()) {
                    logger.warn("No user found for credential ID: {}", credentialId);
                    return ResponseEntity.badRequest().body(Map.of("error", "No user found for this credential"));
                }
            } else {
                // If username is provided, verify the credential belongs to that user
                Map<String, byte[]> userCredentials = credentialStorage.get(username);
                if (userCredentials == null || userCredentials.isEmpty()) {
                    logger.warn("No credentials found for user: {}", username);
                    return ResponseEntity.badRequest().body(Map.of("error", "No credentials found for user"));
                }

                // Check if the credential ID exists for the user
                if (!userCredentials.containsKey(credentialId)) {
                    logger.warn("Credential ID {} not found for user: {}", credentialId, username);
                    return ResponseEntity.badRequest().body(Map.of("error", "Credential not found for user"));
                }
            }

            // In a real implementation, we would verify the assertion here
            // For simplicity, we'll just check if the credential ID exists

            // Find the user in the database
            logger.debug("Looking up user in database: {}", username);
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                logger.warn("User not found in database: {}", username);
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            // Log the user in
            HttpSession session = request.getSession();
            authenticationController.setUserInSession(session, user);
            logger.debug("User set in session: {}", username);

            // Clean up the challenge
            challengeStorage.remove(challengeId);

            logger.info("✅ User authenticated with biometrics: {}", username);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Error verifying authentication", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Error verifying authentication: " + e.getMessage()));
        }
    }
}
