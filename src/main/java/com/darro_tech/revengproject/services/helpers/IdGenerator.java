package com.darro_tech.revengproject.services.helpers;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Helper class for generating IDs that are compatible with database constraints
 */
public class IdGenerator {

    private static final Logger logger = Logger.getLogger(IdGenerator.class.getName());

    /**
     * Generates a shortened ID based on a UUID. This version truncates the UUID
     * to the first 20 characters which fits in smaller varchar columns.
     *
     * @return A shortened ID string that's database-friendly
     */
    public static String generateShortId() {
        String uuid = UUID.randomUUID().toString();
        // Get just the first 20 characters of the UUID to avoid truncation issues
        String shortId = uuid.substring(0, Math.min(uuid.length(), 20));
        logger.fine("🔑 Generated short ID: " + shortId);
        return shortId;
    }

    /**
     * Determines if a string is a valid ID format
     *
     * @param id The ID to validate
     * @return true if the ID is valid
     */
    public static boolean isValidId(String id) {
        return id != null && id.matches("[a-zA-Z0-9-_]{1,32}");
    }
}
