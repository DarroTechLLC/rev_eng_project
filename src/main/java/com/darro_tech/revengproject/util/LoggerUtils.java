package com.darro_tech.revengproject.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for standardized logging throughout the application. This
 * provides consistent formatting and emoji usage for better log readability.
 */
public class LoggerUtils {

    /**
     * Get a logger for the specified class.
     *
     * @param clazz The class to get a logger for
     * @return The logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * Log application startup information.
     *
     * @param logger The logger instance
     * @param componentName The name of the component that is starting
     */
    public static void logStartup(Logger logger, String componentName) {
        logger.info("🚀 {} starting...", componentName);
    }

    /**
     * Log application initialization complete.
     *
     * @param logger The logger instance
     * @param componentName The name of the component that was initialized
     */
    public static void logInitialized(Logger logger, String componentName) {
        logger.info("✅ {} initialized successfully", componentName);
    }

    /**
     * Log controller request received.
     *
     * @param logger The logger instance
     * @param method The HTTP method
     * @param endpoint The endpoint being accessed
     */
    public static void logRequest(Logger logger, String method, String endpoint) {
        logger.info("📥 {} request received: {}", method, endpoint);
    }

    /**
     * Log successful response.
     *
     * @param logger The logger instance
     * @param endpoint The endpoint being accessed
     * @param details Optional details about the response
     */
    public static void logResponse(Logger logger, String endpoint, String details) {
        if (details != null && !details.isEmpty()) {
            logger.info("📤 Response sent for {}: {}", endpoint, details);
        } else {
            logger.info("📤 Response sent for {}", endpoint);
        }
    }

    /**
     * Log database operation.
     *
     * @param logger The logger instance
     * @param operation The database operation (e.g., "SELECT", "INSERT",
     * "UPDATE", "DELETE")
     * @param entity The entity being operated on
     * @param details Optional details about the operation
     */
    public static void logDatabase(Logger logger, String operation, String entity, String details) {
        if (details != null && !details.isEmpty()) {
            logger.debug("🔍 DB {}: {} - {}", operation, entity, details);
        } else {
            logger.debug("🔍 DB {}: {}", operation, entity);
        }
    }

    /**
     * Log authentication events.
     *
     * @param logger The logger instance
     * @param success Whether authentication was successful
     * @param username The username attempting to authenticate
     * @param details Optional details about the authentication
     */
    public static void logAuthentication(Logger logger, boolean success, String username, String details) {
        if (success) {
            if (details != null && !details.isEmpty()) {
                logger.info("🔐 User '{}' authenticated successfully: {}", username, details);
            } else {
                logger.info("🔐 User '{}' authenticated successfully", username);
            }
        } else {
            if (details != null && !details.isEmpty()) {
                logger.warn("⚠️ Authentication failed for user '{}': {}", username, details);
            } else {
                logger.warn("⚠️ Authentication failed for user '{}'", username);
            }
        }
    }

    /**
     * Log authorization events.
     *
     * @param logger The logger instance
     * @param success Whether authorization was successful
     * @param username The username being authorized
     * @param resource The resource being accessed
     */
    public static void logAuthorization(Logger logger, boolean success, String username, String resource) {
        if (success) {
            logger.info("✅ User '{}' authorized for resource: {}", username, resource);
        } else {
            logger.warn("🚫 Access denied for user '{}' to resource: {}", username, resource);
        }
    }

    /**
     * Log validation events.
     *
     * @param logger The logger instance
     * @param success Whether validation was successful
     * @param entity The entity being validated
     * @param details Optional details about the validation
     */
    public static void logValidation(Logger logger, boolean success, String entity, String details) {
        if (success) {
            logger.debug("✅ {} validation successful", entity);
        } else {
            logger.warn("❌ {} validation failed: {}", entity, details);
        }
    }

    /**
     * Log exceptions with appropriate level.
     *
     * @param logger The logger instance
     * @param ex The exception that occurred
     * @param context Context information about where the exception occurred
     */
    public static void logException(Logger logger, Exception ex, String context) {
        logger.error("❌ Exception in {}: {} - {}", context, ex.getClass().getSimpleName(), ex.getMessage(), ex);
    }

    /**
     * Log business events.
     *
     * @param logger The logger instance
     * @param event The event name
     * @param details Details about the event
     */
    public static void logBusinessEvent(Logger logger, String event, String details) {
        logger.info("📊 Business event '{}': {}", event, details);
    }

    /**
     * Log user actions.
     *
     * @param logger The logger instance
     * @param username The user performing the action
     * @param action The action being performed
     * @param details Optional details about the action
     */
    public static void logUserAction(Logger logger, String username, String action, String details) {
        if (details != null && !details.isEmpty()) {
            logger.info("👤 User '{}' {}: {}", username, action, details);
        } else {
            logger.info("👤 User '{}' {}", username, action);
        }
    }

    /**
     * Log system performance metrics.
     *
     * @param logger The logger instance
     * @param operation The operation being measured
     * @param durationMs The duration in milliseconds
     */
    public static void logPerformance(Logger logger, String operation, long durationMs) {
        logger.info("⏱️ {} completed in {} ms", operation, durationMs);
    }

    /**
     * Log integration with external services.
     *
     * @param logger The logger instance
     * @param service The external service name
     * @param operation The operation being performed
     * @param status The status of the operation (success/failure)
     * @param details Optional details about the integration
     */
    public static void logIntegration(Logger logger, String service, String operation, boolean status, String details) {
        if (status) {
            logger.info("🔄 Integration with {} - {} succeeded: {}", service, operation, details);
        } else {
            logger.warn("⚠️ Integration with {} - {} failed: {}", service, operation, details);
        }
    }
}
