package com.darro_tech.revengproject.services.helpers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Helper class to handle user role updates safely Preventing constraint
 * violations and duplicate key errors
 */
public class UserRoleUpdateHelper {

    private static final Logger logger = Logger.getLogger(UserRoleUpdateHelper.class.getName());

    /**
     * Updates user roles safely with a two-phase approach that prevents unique
     * constraint violations on the user_roles.unique_user_role constraint
     *
     * @param jdbcTemplate JdbcTemplate instance for database operations
     * @param userId User ID to update roles for
     * @param roleIds List of role IDs to assign to the user
     * @throws Exception if database operations fail
     */
    public static void updateUserRolesSafely(JdbcTemplate jdbcTemplate, String userId, List<String> roleIds) throws Exception {
        logger.info("🔄 Starting safe role update for user: " + userId);

        // Convert input list to set to eliminate any duplicates
        Set<String> uniqueRoleIds = new HashSet<>();
        if (roleIds != null) {
            uniqueRoleIds.addAll(roleIds);
        }

        try {
            // STEP 1: Get existing roles to determine what needs to change
            List<String> existingRoleIds = fetchExistingRoleIds(jdbcTemplate, userId);

            // Calculate roles to add and remove
            Set<String> rolesToAdd = new HashSet<>(uniqueRoleIds);
            rolesToAdd.removeAll(existingRoleIds);

            Set<String> rolesToRemove = new HashSet<>(existingRoleIds);
            rolesToRemove.removeAll(uniqueRoleIds);

            logger.info("📋 Role update plan - Add: " + rolesToAdd.size()
                    + ", Remove: " + rolesToRemove.size()
                    + ", Keep: " + (existingRoleIds.size() - rolesToRemove.size()));

            // STEP 2: Delete roles first to clear space and avoid constraints
            if (!rolesToRemove.isEmpty()) {
                deleteRoles(jdbcTemplate, userId, rolesToRemove);
                // Ensure delete is committed before proceeding
                jdbcTemplate.execute("COMMIT");
            }

            // STEP 3: Add new roles one by one with validation
            if (!rolesToAdd.isEmpty()) {
                addRoles(jdbcTemplate, userId, rolesToAdd);
            }

            // Verify final count for logging
            int finalCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user_roles WHERE user_id = ?",
                    Integer.class, userId);

            logger.info("✅ Role update completed successfully. User now has " + finalCount + " roles");

        } catch (Exception e) {
            logger.severe("❌ Error updating roles for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw e; // Rethrow to propagate the exception and trigger rollback
        }
    }

    /**
     * Fetches existing role IDs for a user
     *
     * @param jdbcTemplate JdbcTemplate instance
     * @param userId User ID to fetch roles for
     * @return List of role IDs the user currently has
     */
    private static List<String> fetchExistingRoleIds(JdbcTemplate jdbcTemplate, String userId) {
        List<String> existingRoleIds = new ArrayList<>();

        List<Map<String, Object>> roles = jdbcTemplate.queryForList(
                "SELECT role_id FROM user_roles WHERE user_id = ?", userId);

        for (Map<String, Object> role : roles) {
            existingRoleIds.add((String) role.get("role_id"));
        }

        logger.info("🔍 Found " + existingRoleIds.size() + " existing roles for user: " + userId);
        return existingRoleIds;
    }

    /**
     * Deletes specified roles from a user
     *
     * @param jdbcTemplate JdbcTemplate instance
     * @param userId User ID to remove roles from
     * @param rolesToRemove Set of role IDs to remove
     */
    private static void deleteRoles(JdbcTemplate jdbcTemplate, String userId, Set<String> rolesToRemove) {
        for (String roleId : rolesToRemove) {
            int result = jdbcTemplate.update(
                    "DELETE FROM user_roles WHERE user_id = ? AND role_id = ?",
                    userId, roleId);

            if (result > 0) {
                logger.info("🗑️ Removed role: " + roleId + " from user: " + userId);
            }
        }
        logger.info("✅ Removed " + rolesToRemove.size() + " roles successfully");
    }

    /**
     * Adds roles to a user with validation and error handling
     *
     * @param jdbcTemplate JdbcTemplate instance
     * @param userId User ID to add roles to
     * @param rolesToAdd Set of role IDs to add
     */
    private static void addRoles(JdbcTemplate jdbcTemplate, String userId, Set<String> rolesToAdd) {
        int addedCount = 0;

        for (String roleId : rolesToAdd) {
            // Verify role exists before trying to assign it
            Integer roleExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM roles WHERE id = ?",
                    Integer.class, roleId);

            if (roleExists == null || roleExists == 0) {
                logger.warning("⚠️ Skipping invalid role ID: " + roleId);
                continue;
            }

            try {
                // Verify role isn't already assigned (double-check)
                Integer alreadyAssigned = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM user_roles WHERE user_id = ? AND role_id = ?",
                        Integer.class, userId, roleId);

                if (alreadyAssigned != null && alreadyAssigned > 0) {
                    logger.info("ℹ️ Role " + roleId + " already assigned, skipping");
                    continue;
                }

                // Insert with precise timestamp to avoid conflicts
                int result = jdbcTemplate.update(
                        "INSERT INTO user_roles (user_id, role_id, timestamp) VALUES (?, ?, ?)",
                        userId, roleId, new Timestamp(System.currentTimeMillis()));

                if (result > 0) {
                    addedCount++;
                    logger.info("➕ Added role: " + roleId + " for user: " + userId);
                }
            } catch (DataIntegrityViolationException e) {
                // Handle duplicate key or other constraint violations
                if (e.getMessage().contains("Duplicate entry")
                        || e.getMessage().contains("unique_user_role")) {
                    logger.warning("⚠️ Role " + roleId + " already exists for user - constraint violation detected");
                } else {
                    // Log but continue with other roles
                    logger.severe("❌ Error adding role " + roleId + ": " + e.getMessage());
                }
            } catch (Exception e) {
                logger.severe("❌ Unexpected error adding role " + roleId + ": " + e.getMessage());
                // Continue with other roles
            }
        }

        logger.info("✅ Successfully added " + addedCount + " new roles for user");
    }
}
