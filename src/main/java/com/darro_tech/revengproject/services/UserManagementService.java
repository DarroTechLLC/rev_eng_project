package com.darro_tech.revengproject.services;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darro_tech.revengproject.models.Role;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.UserContactType;
import com.darro_tech.revengproject.models.UserRole;
import com.darro_tech.revengproject.models.dto.UserManagementDTO;
import com.darro_tech.revengproject.models.dto.UserUpdateDTO;
import com.darro_tech.revengproject.repositories.CompanyRepository;
import com.darro_tech.revengproject.repositories.RoleRepository;
import com.darro_tech.revengproject.repositories.UserContactTypeRepository;
import com.darro_tech.revengproject.repositories.UserRepository;
import com.darro_tech.revengproject.repositories.UserRoleRepository;
import com.darro_tech.revengproject.services.helpers.UserRoleUpdateHelper;

@Service
public class UserManagementService {

    private static final Logger logger = Logger.getLogger(UserManagementService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserContactTypeRepository userContactTypeRepository;

    /**
     * Get all users with their roles and companies
     *
     * @return list of users with their roles and companies
     */
    @Transactional(readOnly = true)
    public List<UserManagementDTO> getAllUsersWithRolesAndCompanies() {
        logger.info("🔍 Starting to fetch all users with roles and companies");
        try {
            // Get all users
            List<User> users = userRepository.findAll();
            logger.info("📋 Retrieved " + users.size() + " users from database");

            List<UserManagementDTO> result = new ArrayList<>();

            for (User user : users) {
                UserManagementDTO dto = new UserManagementDTO();

                // Use reflection to access User fields safely
                String userId = getFieldValue(user, "id", "");
                dto.setId(userId);
                dto.setUsername(getFieldValue(user, "username", ""));
                dto.setFirstName(getFieldValue(user, "firstName", ""));
                dto.setLastName(getFieldValue(user, "lastName", ""));

                logger.info("👤 Processing user: " + dto.getUsername() + " (ID: " + userId + ")");

                // Get user email from contact info
                String email = getUserEmail(userId);
                dto.setEmail(email);
                if (email != null) {
                    logger.info("📧 Found email for user: " + userId);
                } else {
                    logger.warning("⚠️ No email found for user: " + userId);
                }

                // Get user roles
                List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
                List<UserManagementDTO.RoleDTO> roleDTOs = userRoles.stream()
                        .map(ur -> {
                            Role role = ur.getRole();
                            return new UserManagementDTO.RoleDTO(role.getId(), role.getName());
                        })
                        .collect(Collectors.toList());
                dto.setRoles(roleDTOs);
                logger.info("🔑 Found " + roleDTOs.size() + " roles for user: " + userId);

                // Get user companies
                List<UserManagementDTO.CompanyDTO> companyDTOs = getUserCompanies(userId);
                dto.setCompanies(companyDTOs);
                logger.info("🏢 Found " + companyDTOs.size() + " companies for user: " + userId);

                result.add(dto);
                logger.info("✅ Successfully processed user: " + dto.getUsername());
            }

            logger.info("✅ Successfully retrieved all users with roles and companies. Total: " + result.size());
            return result;
        } catch (Exception e) {
            logger.severe("❌ Error getting users with roles and companies: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // Return empty list on error
        }
    }

    /**
     * Get field value from an object using reflection
     *
     * @param obj the object
     * @param fieldName the field name
     * @param defaultValue the default value if the field is not found
     * @return the field value as a string or the default value
     */
    private String getFieldValue(Object obj, String fieldName, String defaultValue) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            return value != null ? value.toString() : defaultValue;
        } catch (Exception e) {
            logger.warning("⚠️ Error getting field " + fieldName + ": " + e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Get user email from contact info
     *
     * @param userId the user ID
     * @return the user's email or null if not found
     */
    private String getUserEmail(String userId) {
        try {
            String emailTypeId = getEmailTypeId();

            // Skip the type ID check if we can't find the type
            if (emailTypeId != null) {
                // Try method 1: Using known email type ID
                List<Map<String, Object>> results = jdbcTemplate.queryForList(
                        "SELECT value as email FROM user_contact_info "
                        + "WHERE user_id = ? AND type_id = ?",
                        userId, emailTypeId);

                if (!results.isEmpty() && results.get(0).get("email") != null) {
                    String email = (String) results.get(0).get("email");
                    logger.info("📧 Found email via type ID: " + email);
                    return email;
                }
            } else {
                logger.warning("⚠️ Skipping email lookup by type ID - email type not found in database");
            }

            // Try method 2: Joining with user_contact_types
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT uci.value as email FROM user_contact_info uci "
                    + "JOIN user_contact_types uct ON uci.type_id = uct.id "
                    + "WHERE uci.user_id = ? AND LOWER(uct.name) = LOWER('Email')",
                    userId);

            if (!results.isEmpty() && results.get(0).get("email") != null) {
                String email = (String) results.get(0).get("email");
                logger.info("📧 Found email via type name: " + email);
                return email;
            }

            logger.warning("⚠️ No email found for user " + userId + " with either method");
            return null;
        } catch (Exception e) {
            logger.warning("⚠️ Error getting email for user " + userId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Get user companies
     *
     * @param userId the user ID
     * @return list of user's companies
     */
    private List<UserManagementDTO.CompanyDTO> getUserCompanies(String userId) {
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT c.id as company_id, c.name as company_name "
                    + "FROM company_users cu "
                    + "JOIN companies c ON cu.company_id = c.id "
                    + "WHERE cu.user_id = ?",
                    userId);

            return results.stream()
                    .map(row -> new UserManagementDTO.CompanyDTO(
                    row.get("company_id").toString(),
                    (String) row.get("company_name")))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.warning("⚠️ Error getting companies for user " + userId + ": " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get user management DTO by ID
     *
     * @param id the user ID
     * @return the user management DTO or null if not found
     */
    @Transactional(readOnly = true)
    public UserManagementDTO getUserManagementDTOById(String id) {
        logger.info("🔍 Fetching user management DTO with ID: " + id);

        try {
            Optional<User> userOpt = userRepository.findById(id);

            if (userOpt.isEmpty()) {
                logger.warning("⚠️ User not found with ID: " + id);
                return null;
            }

            User user = userOpt.get();
            UserManagementDTO dto = new UserManagementDTO();

            // Set basic user info
            dto.setId(id);
            dto.setUsername(getFieldValue(user, "username", ""));
            dto.setFirstName(getFieldValue(user, "firstName", ""));
            dto.setLastName(getFieldValue(user, "lastName", ""));

            // Get user email
            String email = getUserEmail(id);
            dto.setEmail(email);

            // Get user phone/SMS
            String phone = getUserPhone(id);
            dto.setPhone(phone);

            // Get user roles
            List<UserRole> userRoles = userRoleRepository.findByUserId(id);
            List<UserManagementDTO.RoleDTO> roleDTOs = userRoles.stream()
                    .map(ur -> {
                        Role role = ur.getRole();
                        return new UserManagementDTO.RoleDTO(role.getId(), role.getName());
                    })
                    .collect(Collectors.toList());
            dto.setRoles(roleDTOs);

            // Get user companies
            List<UserManagementDTO.CompanyDTO> companyDTOs = getUserCompanies(id);
            dto.setCompanies(companyDTOs);

            logger.info("✅ Successfully fetched user management DTO with ID: " + id);
            return dto;
        } catch (Exception e) {
            logger.severe("❌ Error fetching user management DTO with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get user phone from contact info
     *
     * @param userId the user ID
     * @return the user's phone or null if not found
     */
    private String getUserPhone(String userId) {
        try {
            String smsTypeId = getSmsTypeId();

            // Skip the type ID check if we can't find the type
            if (smsTypeId != null) {
                // Try using known SMS type ID
                List<Map<String, Object>> results = jdbcTemplate.queryForList(
                        "SELECT value as phone FROM user_contact_info "
                        + "WHERE user_id = ? AND type_id = ?",
                        userId, smsTypeId);

                if (!results.isEmpty() && results.get(0).get("phone") != null) {
                    String phone = (String) results.get(0).get("phone");
                    logger.info("📱 Found phone via type ID: " + phone);
                    return phone;
                }
            } else {
                logger.warning("⚠️ Skipping phone lookup by type ID - SMS type not found in database");
            }

            // Try joining with user_contact_types
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT uci.value as phone FROM user_contact_info uci "
                    + "JOIN user_contact_types uct ON uci.type_id = uct.id "
                    + "WHERE uci.user_id = ? AND (LOWER(uct.name) = LOWER('SMS') OR LOWER(uct.name) = LOWER('Phone'))",
                    userId);

            if (!results.isEmpty() && results.get(0).get("phone") != null) {
                String phone = (String) results.get(0).get("phone");
                logger.info("📱 Found phone via type name: " + phone);
                return phone;
            }

            logger.warning("⚠️ No phone found for user " + userId);
            return null;
        } catch (Exception e) {
            logger.warning("⚠️ Error getting phone for user " + userId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Update a user with the given DTO
     *
     * @param userUpdateDTO the user update DTO
     * @return UpdateResult containing success status and error message if any
     */
    @Transactional
    public UpdateResult updateUser(UserUpdateDTO userUpdateDTO) {
        logger.info("🛠️ Updating user with ID: " + userUpdateDTO.getId());

        try {
            // Find user by ID
            Optional<User> userOptional = userRepository.findById(userUpdateDTO.getId());
            if (!userOptional.isPresent()) {
                logger.warning("⚠️ User not found with ID: " + userUpdateDTO.getId());
                return new UpdateResult(false, "User not found with ID: " + userUpdateDTO.getId());
            }

            User user = userOptional.get();

            // Log initial user state for debugging
            logger.info("📋 Initial user state - Username: " + user.getUsername()
                    + ", First Name: " + user.getFirstName()
                    + ", Last Name: " + user.getLastName());

            // Update basic user information
            user.setUsername(userUpdateDTO.getUsername());
            user.setFirstName(userUpdateDTO.getFirstName());
            user.setLastName(userUpdateDTO.getLastName());

            // Save the user entity with explicit flush
            userRepository.save(user);
            logger.info("✅ Basic user info updated for user: " + user.getUsername());

            try {
                // Update user email
                updateUserContact(userUpdateDTO.getId(), getEmailTypeId(), userUpdateDTO.getEmail());
                logger.info("✅ User email updated to: " + userUpdateDTO.getEmail());
            } catch (Exception e) {
                logger.severe("❌ Error updating user email: " + e.getMessage());
                return new UpdateResult(false, "Error updating email: " + e.getMessage());
            }

            try {
                // Update user phone/SMS
                updateUserContact(userUpdateDTO.getId(), getSmsTypeId(), userUpdateDTO.getPhone());
                logger.info("✅ User phone updated to: " + userUpdateDTO.getPhone());
            } catch (Exception e) {
                logger.severe("❌ Error updating user phone: " + e.getMessage());
                return new UpdateResult(false, "Error updating phone: " + e.getMessage());
            }

            try {
                // Update user roles
                updateUserRoles(userUpdateDTO.getId(), userUpdateDTO.getRoleIds());
                logger.info("✅ User roles updated. Number of roles: "
                        + (userUpdateDTO.getRoleIds() != null ? userUpdateDTO.getRoleIds().size() : 0));
            } catch (Exception e) {
                logger.severe("❌ Error updating user roles: " + e.getMessage());
                e.printStackTrace(); // Add stack trace for more detail
                return new UpdateResult(false, "Error updating roles: " + e.getMessage());
            }

            try {
                // Update user companies
                updateUserCompanies(userUpdateDTO.getId(), userUpdateDTO.getCompanyIds());
                logger.info("✅ User companies updated. Number of companies: "
                        + (userUpdateDTO.getCompanyIds() != null ? userUpdateDTO.getCompanyIds().size() : 0));
            } catch (Exception e) {
                logger.severe("❌ Error updating user companies: " + e.getMessage());
                e.printStackTrace(); // Add stack trace for more detail
                return new UpdateResult(false, "Error updating companies: " + e.getMessage());
            }

            // Handle password if set manually
            if ("manual".equals(userUpdateDTO.getPassOption()) && userUpdateDTO.getPassword() != null
                    && !userUpdateDTO.getPassword().isEmpty()) {
                try {
                    user.setPassword(userUpdateDTO.getPassword());
                    userRepository.save(user);
                    logger.info("✅ User password updated manually");
                } catch (Exception e) {
                    logger.severe("❌ Error updating user password: " + e.getMessage());
                    e.printStackTrace(); // Add stack trace for more detail
                    return new UpdateResult(false, "Error updating password: " + e.getMessage());
                }
            } else if ("passlink".equals(userUpdateDTO.getPassOption())) {
                try {
                    // Set reset password flag
                    user.setResetPswd((byte) 1);
                    userRepository.save(user);
                    logger.info("✅ User set to receive password reset link");
                } catch (Exception e) {
                    logger.severe("❌ Error setting password reset flag: " + e.getMessage());
                    e.printStackTrace(); // Add stack trace for more detail
                    return new UpdateResult(false, "Error setting password reset: " + e.getMessage());
                }
            }

            logger.info("✅ User updated successfully: " + user.getUsername());
            return new UpdateResult(true, null);
        } catch (Exception e) {
            logger.severe("❌ Error updating user: " + e.getMessage());
            e.printStackTrace();
            return new UpdateResult(false, "Error updating user: " + e.getMessage());
        }
    }

    /**
     * Update user contact info with additional error handling
     *
     * @param userId the user ID
     * @param typeId the contact type ID
     * @param value the contact value
     */
    private void updateUserContact(String userId, String typeId, String value) {
        if (value == null || value.trim().isEmpty()) {
            logger.info("⚠️ Skipping contact update for user " + userId + " - empty value provided");
            return;
        }

        if (typeId == null) {
            logger.warning("⚠️ Cannot update contact info: contact type ID is null");
            return;
        }

        logger.info("📞 Updating " + (typeId.equals(getEmailTypeId()) ? "email" : "phone") + " for user: " + userId);

        try {
            // Check if contact info exists
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT id FROM user_contact_info WHERE user_id = ? AND type_id = ?",
                    userId, typeId);

            if (!results.isEmpty()) {
                // Update existing contact info
                jdbcTemplate.update(
                        "UPDATE user_contact_info SET value = ? WHERE user_id = ? AND type_id = ?",
                        value, userId, typeId);
                logger.info("✅ Updated contact info for user: " + userId);
            } else {
                // Insert new contact info
                jdbcTemplate.update(
                        "INSERT INTO user_contact_info (id, user_id, type_id, value) VALUES (?, ?, ?, ?)",
                        java.util.UUID.randomUUID().toString(), userId, typeId, value);
                logger.info("✅ Added new contact info for user: " + userId);
            }
        } catch (Exception e) {
            logger.warning("⚠️ Error updating contact for user " + userId + ": " + e.getMessage());
            throw e; // Rethrow to properly handle in parent transaction
        }
    }

    /**
     * Update user roles with additional error handling
     *
     * @param userId the user ID
     * @param roleIds the list of role IDs
     */
    private void updateUserRoles(String userId, List<String> roleIds) {
        try {
            logger.info("🔄 Starting user role update for user: " + userId);

            // Use the dedicated helper class to safely update roles
            UserRoleUpdateHelper.updateUserRolesSafely(jdbcTemplate, userId, roleIds);

            logger.info("✅ User roles updated successfully");
        } catch (Exception e) {
            logger.severe("❌ Error updating roles for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update user roles: " + e.getMessage(), e);
        }
    }

    /**
     * Update user companies
     *
     * @param userId the user ID
     * @param companyIds list of company IDs
     */
    private void updateUserCompanies(String userId, List<String> companyIds) {
        try {
            // Delete existing company associations
            jdbcTemplate.update("DELETE FROM company_users WHERE user_id = ?", userId);
            logger.info("✅ Deleted existing company associations for user: " + userId);

            // Add new company associations
            if (companyIds != null && !companyIds.isEmpty()) {
                for (String companyId : companyIds) {
                    jdbcTemplate.update(
                            "INSERT INTO company_users (id, user_id, company_id) VALUES (?, ?, ?)",
                            java.util.UUID.randomUUID().toString(), userId, companyId);
                }
                logger.info("✅ Added " + companyIds.size() + " new company associations for user: " + userId);
            }
        } catch (Exception e) {
            logger.warning("⚠️ Error updating companies for user " + userId + ": " + e.getMessage());
            throw e; // Rethrow to properly handle in parent transaction
        }
    }

    /**
     * Result class for update operations
     */
    public static class UpdateResult {

        private final boolean success;
        private final String errorMessage;

        public UpdateResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    private String getEmailTypeId() {
        logger.info("🔍 Looking up Email contact type from database");
        UserContactType emailType = userContactTypeRepository.findByName("Email");
        if (emailType == null) {
            logger.warning("⚠️ Email contact type not found in database");
            return null;
        }
        logger.info("✅ Found Email contact type: " + emailType.getId());
        return emailType.getId();
    }

    private String getSmsTypeId() {
        logger.info("🔍 Looking up SMS contact type from database");
        UserContactType smsType = userContactTypeRepository.findByName("SMS");
        if (smsType == null) {
            logger.warning("⚠️ SMS contact type not found in database");
            return null;
        }
        logger.info("✅ Found SMS contact type: " + smsType.getId());
        return smsType.getId();
    }
}
