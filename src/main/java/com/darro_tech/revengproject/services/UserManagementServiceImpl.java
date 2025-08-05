package com.darro_tech.revengproject.services;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darro_tech.revengproject.models.Role;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.UserContactType;
import com.darro_tech.revengproject.models.UserRole;
import com.darro_tech.revengproject.models.dto.UserDTO;
import com.darro_tech.revengproject.models.dto.UserManagementDTO;
import com.darro_tech.revengproject.models.dto.UserUpdateDTO;
import com.darro_tech.revengproject.repositories.RoleRepository;
import com.darro_tech.revengproject.repositories.UserContactTypeRepository;
import com.darro_tech.revengproject.repositories.UserRepository;
import com.darro_tech.revengproject.repositories.UserRoleRepository;
import com.darro_tech.revengproject.services.helpers.UserRoleUpdateHelper;

@Service
public class UserManagementServiceImpl implements UserManagementServiceInterface {

    private static final Logger logger = Logger.getLogger(UserManagementServiceImpl.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserContactTypeRepository userContactTypeRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Get all users with their roles and companies
     *
     * @return list of users with their roles and companies
     */
    @Override
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

    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementDTO> getUsersWithRolesAndCompaniesPaginated(Pageable pageable) {
        logger.info("🔍 Starting to fetch paginated users with roles and companies. Page: " + 
                    pageable.getPageNumber() + ", Size: " + pageable.getPageSize());
        try {
            // Get paginated users
            Page<User> userPage = userRepository.findAll(pageable);
            List<User> users = userPage.getContent();
            logger.info("📋 Retrieved " + users.size() + " users from database for current page");

            if (users.isEmpty()) {
                logger.info("📋 No users found for this page");
                return Page.empty(pageable);
            }

            // Extract all user IDs
            List<String> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());

            // Batch fetch emails for all users in this page
            Map<String, String> userEmails = batchGetUserEmails(userIds);
            logger.info("📧 Batch fetched " + userEmails.size() + " emails");

            // Batch fetch roles for all users in this page
            Map<String, List<UserManagementDTO.RoleDTO>> userRoles = batchGetUserRoles(userIds);
            logger.info("🔑 Batch fetched roles for " + userRoles.size() + " users");

            // Batch fetch companies for all users in this page
            Map<String, List<UserManagementDTO.CompanyDTO>> userCompanies = batchGetUserCompanies(userIds);
            logger.info("🏢 Batch fetched companies for " + userCompanies.size() + " users");

            // Create DTOs for all users
            List<UserManagementDTO> result = new ArrayList<>();
            for (User user : users) {
                UserManagementDTO dto = new UserManagementDTO();
                String userId = user.getId();

                dto.setId(userId);
                dto.setUsername(user.getUsername());
                dto.setFirstName(user.getFirstName());
                dto.setLastName(user.getLastName());

                // Set email from batch results
                dto.setEmail(userEmails.getOrDefault(userId, null));

                // Set roles from batch results
                dto.setRoles(userRoles.getOrDefault(userId, new ArrayList<>()));

                // Set companies from batch results
                dto.setCompanies(userCompanies.getOrDefault(userId, new ArrayList<>()));

                result.add(dto);
            }

            logger.info("✅ Successfully processed " + result.size() + " users for current page");
            return new PageImpl<>(result, pageable, userPage.getTotalElements());
        } catch (Exception e) {
            logger.severe("❌ Error getting paginated users: " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }

    /**
     * Batch fetch emails for multiple users
     * 
     * @param userIds list of user IDs
     * @return map of user ID to email
     */
    private Map<String, String> batchGetUserEmails(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }

        try {
            String emailTypeId = getEmailTypeId();
            Map<String, String> result = new HashMap<>();

            // Create placeholders for SQL IN clause
            String placeholders = String.join(",", Collections.nCopies(userIds.size(), "?"));

            // Query with IN clause
            String sql = "SELECT user_id, value as email FROM user_contact_info " +
                         "WHERE user_id IN (" + placeholders + ") AND type_id = ?";

            // Create parameters array with user IDs and email type ID
            Object[] params = new Object[userIds.size() + 1];
            for (int i = 0; i < userIds.size(); i++) {
                params[i] = userIds.get(i);
            }
            params[userIds.size()] = emailTypeId;

            // Execute query
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

            // Process results
            for (Map<String, Object> row : rows) {
                String userId = row.get("user_id").toString();
                String email = (String) row.get("email");
                result.put(userId, email);
            }

            return result;
        } catch (Exception e) {
            logger.warning("⚠️ Error batch fetching emails: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Batch fetch roles for multiple users
     * 
     * @param userIds list of user IDs
     * @return map of user ID to list of role DTOs
     */
    private Map<String, List<UserManagementDTO.RoleDTO>> batchGetUserRoles(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }

        try {
            Map<String, List<UserManagementDTO.RoleDTO>> result = new HashMap<>();

            // Initialize empty lists for all users
            for (String userId : userIds) {
                result.put(userId, new ArrayList<>());
            }

            // Create placeholders for SQL IN clause
            String placeholders = String.join(",", Collections.nCopies(userIds.size(), "?"));

            // Query with IN clause
            String sql = "SELECT ur.user_id, r.id as role_id, r.name as role_name " +
                         "FROM user_roles ur " +
                         "JOIN roles r ON ur.role_id = r.id " +
                         "WHERE ur.user_id IN (" + placeholders + ")";

            // Execute query
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userIds.toArray());

            // Process results
            for (Map<String, Object> row : rows) {
                String userId = row.get("user_id").toString();
                String roleId = row.get("role_id").toString();
                String roleName = (String) row.get("role_name");

                UserManagementDTO.RoleDTO roleDTO = new UserManagementDTO.RoleDTO(roleId, roleName);
                result.get(userId).add(roleDTO);
            }

            return result;
        } catch (Exception e) {
            logger.warning("⚠️ Error batch fetching roles: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Batch fetch companies for multiple users
     * 
     * @param userIds list of user IDs
     * @return map of user ID to list of company DTOs
     */
    private Map<String, List<UserManagementDTO.CompanyDTO>> batchGetUserCompanies(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }

        try {
            Map<String, List<UserManagementDTO.CompanyDTO>> result = new HashMap<>();

            // Initialize empty lists for all users
            for (String userId : userIds) {
                result.put(userId, new ArrayList<>());
            }

            // Create placeholders for SQL IN clause
            String placeholders = String.join(",", Collections.nCopies(userIds.size(), "?"));

            // Query with IN clause
            String sql = "SELECT cu.user_id, c.id as company_id, c.name as company_name " +
                         "FROM company_users cu " +
                         "JOIN companies c ON cu.company_id = c.id " +
                         "WHERE cu.user_id IN (" + placeholders + ")";

            // Execute query
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userIds.toArray());

            // Process results
            for (Map<String, Object> row : rows) {
                String userId = row.get("user_id").toString();
                String companyId = row.get("company_id").toString();
                String companyName = (String) row.get("company_name");

                UserManagementDTO.CompanyDTO companyDTO = new UserManagementDTO.CompanyDTO(companyId, companyName);
                result.get(userId).add(companyDTO);
            }

            return result;
        } catch (Exception e) {
            logger.warning("⚠️ Error batch fetching companies: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Get user by ID (entity)
     *
     * @param id the user ID
     * @return the user entity or null if not found
     */
    @Override
    @Transactional(readOnly = true)
    public User getUserById(String id) {
        logger.info("🔍 Fetching user entity with ID: " + id);

        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                logger.info("✅ Successfully fetched user entity with ID: " + id);
                return userOpt.get();
            } else {
                logger.warning("⚠️ User entity not found with ID: " + id);
                return null;
            }
        } catch (Exception e) {
            logger.severe("❌ Error fetching user entity with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get user DTO by ID for API responses
     *
     * @param id the user ID
     * @return the user DTO or null if not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserDTOById(String id) {
        logger.info("🔍 Fetching user DTO with ID: " + id);

        try {
            User user = userRepository.findById(id).orElse(null);

            if (user == null) {
                logger.warning("⚠️ User not found with ID: " + id);
                return null;
            }

            UserDTO dto = new UserDTO();

            // Set basic user info
            dto.setId(id);
            dto.setUsername(user.getUsername());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());

            // Get user email
            String email = getUserEmail(id);
            dto.setEmail(email);

            // Get user phone/SMS
            String phone = getUserPhone(id);
            dto.setPhone(phone);

            // Get user roles
            List<UserRole> userRoles = userRoleRepository.findByUserId(id);
            List<UserDTO.RoleDTO> roleDTOs = userRoles.stream()
                    .map(ur -> {
                        Role role = ur.getRole();
                        return new UserDTO.RoleDTO(role.getId(), role.getName());
                    })
                    .collect(Collectors.toList());
            dto.setRoles(roleDTOs);

            // Get user companies
            List<UserDTO.CompanyDTO> companyDTOs = getUserCompaniesForDTO(id);
            dto.setCompanies(companyDTOs);

            logger.info("✅ Successfully fetched user DTO with ID: " + id);
            return dto;
        } catch (Exception e) {
            logger.severe("❌ Error fetching user DTO with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a new user
     *
     * @param user the user entity
     * @param roleIds list of role IDs
     * @param companyIds list of company IDs
     * @return the created user entity
     */
    @Override
    @Transactional
    public User createUser(User user, List<String> roleIds, List<String> companyIds) {
        logger.info("🔄 Creating new user: " + user.getUsername());

        try {
            // Save the user
            User savedUser = userRepository.save(user);
            String userId = savedUser.getId();
            logger.info("✅ Saved basic user info for: " + userId);

            // Add roles
            if (roleIds != null && !roleIds.isEmpty()) {
                for (String roleId : roleIds) {
                    Role role = roleRepository.findById(roleId).orElse(null);
                    if (role != null) {
                        UserRole userRole = new UserRole();
                        userRole.setUser(savedUser);
                        userRole.setRole(role);
                        userRole.setTimestamp(java.time.Instant.now());
                        userRoleRepository.save(userRole);
                    }
                }
                logger.info("✅ Added " + roleIds.size() + " roles for user: " + userId);
            }

            // Add companies
            if (companyIds != null && !companyIds.isEmpty()) {
                for (String companyId : companyIds) {
                    jdbcTemplate.update(
                            "INSERT INTO company_users (user_id, company_id, timestamp) VALUES (?, ?, ?)",
                            userId, companyId, java.time.Instant.now());
                }
                logger.info("✅ Added " + companyIds.size() + " companies for user: " + userId);
            }

            logger.info("✅ Successfully created user: " + userId);
            return savedUser;
        } catch (Exception e) {
            logger.severe("❌ Error creating user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Updates an existing user
     *
     * @param userUpdateDTO data for updating the user
     * @return true if update was successful, false otherwise
     */
    @Override
    @Transactional
    public boolean updateUser(UserUpdateDTO userUpdateDTO) {
        String userId = userUpdateDTO.getId();
        logger.info("🔄 Updating user with ID: " + userId);

        try {
            // Update user basic info
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.warning("⚠️ User not found with ID: " + userId);
                return false;
            }

            // Update user fields
            user.setUsername(userUpdateDTO.getUsername());
            user.setFirstName(userUpdateDTO.getFirstName());
            user.setLastName(userUpdateDTO.getLastName());

            userRepository.save(user);
            logger.info("✅ Updated basic user info for user: " + userId);

            // Update user email - with exception handling
            try {
                updateUserEmail(userId, userUpdateDTO.getEmail());
                logger.info("✅ Updated email for user: " + userId);
            } catch (Exception e) {
                logger.severe("❌ Error updating email for user " + userId + ": " + e.getMessage());
                throw e; // Rethrow to roll back the entire transaction
            }

            // Update user phone/SMS - with exception handling
            try {
                updateUserPhone(userId, userUpdateDTO.getPhone());
                logger.info("✅ Updated phone for user: " + userId);
            } catch (Exception e) {
                logger.severe("❌ Error updating phone for user " + userId + ": " + e.getMessage());
                throw e; // Rethrow to roll back the entire transaction
            }

            // Update user roles - with exception handling
            try {
                updateUserRoles(userId, userUpdateDTO.getRoleIds());
                logger.info("✅ Updated roles for user: " + userId);
            } catch (Exception e) {
                logger.severe("❌ Error updating roles for user " + userId + ": " + e.getMessage());
                throw e; // Rethrow to roll back the entire transaction
            }

            // Update user companies - with exception handling
            try {
                updateUserCompanies(userId, userUpdateDTO.getCompanyIds());
                logger.info("✅ Updated companies for user: " + userId);
            } catch (Exception e) {
                logger.severe("❌ Error updating companies for user " + userId + ": " + e.getMessage());
                throw e; // Rethrow to roll back the entire transaction
            }

            // Handle password update if requested
            String passOption = userUpdateDTO.getPassOption();
            if ("manual".equals(passOption)) {
                String password = userUpdateDTO.getPassword();
                if (password != null && !password.isEmpty()) {
                    try {
                        setUserPassword(userId, password);
                    } catch (Exception e) {
                        logger.severe("❌ Error updating password for user " + userId + ": " + e.getMessage());
                        throw e; // Rethrow to roll back the entire transaction
                    }
                }
            } else if ("passlink".equals(passOption)) {
                // Send password reset link - not part of transaction
                try {
                    sendPasswordResetLink(userId);
                } catch (Exception e) {
                    // Log but don't fail transaction for email sending
                    logger.warning("⚠️ Error sending password reset link: " + e.getMessage());
                }
            }

            logger.info("✅ Successfully updated all user data for user: " + userId);
            return true;
        } catch (Exception e) {
            logger.severe("❌ Error updating user with ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a user
     *
     * @param userId the user ID
     * @return true if deletion was successful, false otherwise
     */
    @Override
    @Transactional
    public boolean deleteUser(String userId) {
        logger.info("❌ Deleting user with ID: " + userId);

        try {
            // Delete user roles
            List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
            for (UserRole role : userRoles) {
                userRoleRepository.delete(role);
            }
            logger.info("✅ Deleted user roles for: " + userId);

            // Delete user companies
            jdbcTemplate.update("DELETE FROM company_users WHERE user_id = ?", userId);
            logger.info("✅ Deleted company associations for user: " + userId);

            // Delete user contact info
            jdbcTemplate.update("DELETE FROM user_contact_info WHERE user_id = ?", userId);
            logger.info("✅ Deleted contact info for user: " + userId);

            // Finally delete the user
            userRepository.deleteById(userId);
            logger.info("✅ Successfully deleted user with ID: " + userId);

            return true;
        } catch (Exception e) {
            logger.severe("❌ Error deleting user with ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends a password reset link to a user
     *
     * @param userId the user ID
     * @return true if the link was sent successfully
     */
    @Override
    @Transactional
    public boolean sendPasswordResetLink(String userId) {
        try {
            // Get user email
            String email = getUserEmail(userId);
            if (email == null || email.isEmpty()) {
                logger.warning("⚠️ Cannot send password reset link - no email found for user: " + userId);
                return false;
            }

            // Get the user
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.warning("⚠️ Cannot send password reset link - user not found: " + userId);
                return false;
            }

            // Generate a reset key
            String resetKey = generateResetKey();

            // Set expiration date (7 days from now)
            Instant expirationDate = Instant.now().plusSeconds(604800); // 7 days in seconds

            // Update user record
            user.setResetPassKey(resetKey);
            user.setResetPassExpires(expirationDate);
            user.setResetPswd((byte) 1); // Flag that password reset is required
            userRepository.save(user);

            // Send email using EmailService
            boolean emailSent = emailService.sendForgotPasswordEmail(email, user.getUsername(), resetKey);

            if (!emailSent) {
                logger.warning("⚠️ Failed to send password reset email to: " + email);
                return false;
            }

            logger.info("📧 Password reset email sent to user: " + userId);
            logger.info("📧 Reset key: " + resetKey);
            logger.info("📧 Expiration date: " + expirationDate);

            return true;
        } catch (Exception e) {
            logger.severe("❌ Error sending password reset link for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generates a random reset key
     * 
     * @return a 6-character reset key
     */
    private String generateResetKey() {
        // Characters to use (excluding 1, 0, O to avoid ambiguity)
        String chars = "23456789ABCDEFGHIJKLMNPQRSTUVWXYZ";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }

        return sb.toString();
    }

    /**
     * Updates a user's password directly
     *
     * @param userId the user ID
     * @param newPassword the new password (plaintext)
     * @return true if the password was updated successfully
     */
    @Override
    @Transactional
    public boolean setUserPassword(String userId, String newPassword) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.warning("⚠️ Cannot update password - user not found: " + userId);
                return false;
            }

            // Set the password (User.setPassword already handles hashing)
            user.setPassword(newPassword);
            userRepository.save(user);

            logger.info("🔐 Updated password for user: " + userId);
            return true;
        } catch (Exception e) {
            logger.severe("❌ Error updating password for user " + userId + ": " + e.getMessage());
            return false;
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
            // Try method 1: Using known email type ID
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT value as email FROM user_contact_info "
                    + "WHERE user_id = ? AND type_id = ?",
                    userId, getEmailTypeId());

            if (!results.isEmpty() && results.get(0).get("email") != null) {
                String email = (String) results.get(0).get("email");
                logger.info("📧 Found email via type ID: " + email);
                return email;
            }

            // Try method 2: Joining with user_contact_types
            results = jdbcTemplate.queryForList(
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
     * Get user phone/SMS from contact info
     *
     * @param userId the user ID
     * @return the user's phone or null if not found
     */
    private String getUserPhone(String userId) {
        try {
            // Try using known SMS type ID
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT value as phone FROM user_contact_info "
                    + "WHERE user_id = ? AND type_id = ?",
                    userId, getSmsTypeId());

            if (!results.isEmpty() && results.get(0).get("phone") != null) {
                String phone = (String) results.get(0).get("phone");
                logger.info("📱 Found phone via type ID: " + phone);
                return phone;
            }

            // Try joining with user_contact_types
            results = jdbcTemplate.queryForList(
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
     * Get user companies for DTO
     *
     * @param userId the user ID
     * @return list of user's companies
     */
    private List<UserDTO.CompanyDTO> getUserCompaniesForDTO(String userId) {
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT c.id as company_id, c.name as company_name "
                    + "FROM company_users cu "
                    + "JOIN companies c ON cu.company_id = c.id "
                    + "WHERE cu.user_id = ?",
                    userId);

            return results.stream()
                    .map(row -> new UserDTO.CompanyDTO(
                    row.get("company_id").toString(),
                    (String) row.get("company_name")))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.warning("⚠️ Error getting companies for user " + userId + ": " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Update user email
     *
     * @param userId the user ID
     * @param email the new email
     */
    private void updateUserEmail(String userId, String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.info("⚠️ Skipping email update for user " + userId + " - empty value provided");
            return;
        }

        String emailTypeId = getEmailTypeId();
        if (emailTypeId == null) {
            logger.warning("⚠️ Cannot update email: email type ID is null");
            throw new RuntimeException("Email type ID is null");
        }

        try {
            // First delete existing email entries (to avoid any potential duplicates)
            int deletedCount = jdbcTemplate.update(
                    "DELETE FROM user_contact_info WHERE user_id = ? AND type_id = ?",
                    userId, emailTypeId);

            if (deletedCount > 0) {
                logger.info("🗑️ Deleted " + deletedCount + " existing email entries for user: " + userId);
            }

            // Insert new email
            jdbcTemplate.update(
                    "INSERT INTO user_contact_info (user_id, type_id, value, timestamp) VALUES (?, ?, ?, ?)",
                    userId, emailTypeId, email, java.time.Instant.now());

            logger.info("✅ Added new email for user: " + userId + " - " + email);
        } catch (Exception e) {
            logger.severe("❌ Error updating email for user " + userId + ": " + e.getMessage());
            throw e; // Rethrow to propagate the exception
        }
    }

    /**
     * Update user phone/SMS
     *
     * @param userId the user ID
     * @param phone the new phone number
     */
    private void updateUserPhone(String userId, String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            logger.info("⚠️ Skipping phone update for user " + userId + " - empty value provided");
            return;
        }

        String smsTypeId = getSmsTypeId();
        if (smsTypeId == null) {
            logger.warning("⚠️ Cannot update phone: SMS type ID is null");
            throw new RuntimeException("SMS type ID is null");
        }

        try {
            // First delete existing phone entries (to avoid any potential duplicates)
            int deletedCount = jdbcTemplate.update(
                    "DELETE FROM user_contact_info WHERE user_id = ? AND type_id = ?",
                    userId, smsTypeId);

            if (deletedCount > 0) {
                logger.info("🗑️ Deleted " + deletedCount + " existing phone entries for user: " + userId);
            }

            // Insert new phone
            jdbcTemplate.update(
                    "INSERT INTO user_contact_info (user_id, type_id, value, timestamp) VALUES (?, ?, ?, ?)",
                    userId, smsTypeId, phone, java.time.Instant.now());

            logger.info("✅ Added new phone for user: " + userId + " - " + phone);
        } catch (Exception e) {
            logger.severe("❌ Error updating phone for user " + userId + ": " + e.getMessage());
            throw e; // Rethrow to propagate the exception
        }
    }

    /**
     * Update user roles
     *
     * @param userId the user ID
     * @param roleIds the new role IDs
     */
    private void updateUserRoles(String userId, List<String> roleIds) {
        try {
            logger.info("🔄 Starting user role update for user: " + userId);

            // Use the new helper class to safely update roles
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
            // Delete existing company associations with direct SQL for immediate execution
            int deletedCount = jdbcTemplate.update("DELETE FROM company_users WHERE user_id = ?", userId);
            logger.info("🗑️ Deleted " + deletedCount + " existing company associations for user: " + userId);

            // Add new company associations
            if (companyIds != null && !companyIds.isEmpty()) {
                int addedCount = 0;
                for (String companyId : companyIds) {
                    try {
                        // Check if this company association already exists (safety check)
                        List<Map<String, Object>> existing = jdbcTemplate.queryForList(
                                "SELECT COUNT(*) as count FROM company_users WHERE user_id = ? AND company_id = ?",
                                userId, companyId);

                        if (existing.isEmpty() || ((Number) existing.get(0).get("count")).intValue() == 0) {
                            // Insert the new company association - let the database auto-generate the ID
                            jdbcTemplate.update(
                                    "INSERT INTO company_users (user_id, company_id, timestamp) VALUES (?, ?, ?)",
                                    userId, companyId, java.time.Instant.now());
                            addedCount++;
                            logger.info("➕ Added company association for company ID: " + companyId);
                        } else {
                            logger.warning("⚠️ Company " + companyId + " already associated with user: " + userId + " - skipping");
                        }
                    } catch (Exception e) {
                        logger.severe("❌ Error adding company " + companyId + " to user: " + e.getMessage());
                        throw e;
                    }
                }
                logger.info("✅ Added " + addedCount + " new company associations for user: " + userId);
            }
        } catch (Exception e) {
            logger.severe("❌ Error updating companies for user " + userId + ": " + e.getMessage());
            throw e; // Rethrow to propagate the exception
        }
    }

    /**
     * Get user management DTO by ID
     *
     * @param id the user ID
     * @return the user management DTO or null if not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserManagementDTO getUserManagementDTOById(String id) {
        logger.info("🔍 Fetching user management DTO with ID: " + id);

        try {
            User user = userRepository.findById(id).orElse(null);

            if (user == null) {
                logger.warning("⚠️ User not found with ID: " + id);
                return null;
            }

            UserManagementDTO dto = new UserManagementDTO();

            // Set basic user info using reflection safely
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
     * Get user phone number
     *
     * @return the user's phone number or null if not found
     */
    @Override
    public String getPhone() {
        logger.info("🔍 Getting user phone number");
        return null; // This method needs context - whose phone to get? Implement based on application requirements
    }

    /**
     * Set user phone number
     *
     * @param phone the phone number to set
     */
    @Override
    public void setPhone(String phone) {
        logger.info("📞 Setting user phone number: " + phone);
        // Implementation depends on your application context
        // For example, you might want to update the current user's phone
        // This method needs to be implemented based on your requirements
    }

    /**
     * Search for users by username, first name, last name, or email
     *
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of users matching the search term
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementDTO> searchUsers(String searchTerm, Pageable pageable) {
        logger.info("🔍 Searching for users with term: " + searchTerm);
        try {
            // Search users with pagination
            Page<User> userPage = userRepository.searchUsers(searchTerm, pageable);
            List<User> users = userPage.getContent();
            logger.info("📋 Found " + users.size() + " users matching search term: " + searchTerm);

            if (users.isEmpty()) {
                logger.info("📋 No users found matching search term: " + searchTerm);
                return Page.empty(pageable);
            }

            // Extract all user IDs
            List<String> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());

            // Batch fetch emails for all users in this page
            Map<String, String> userEmails = batchGetUserEmails(userIds);
            logger.info("📧 Batch fetched " + userEmails.size() + " emails");

            // Batch fetch roles for all users in this page
            Map<String, List<UserManagementDTO.RoleDTO>> userRoles = batchGetUserRoles(userIds);
            logger.info("🔑 Batch fetched roles for " + userRoles.size() + " users");

            // Batch fetch companies for all users in this page
            Map<String, List<UserManagementDTO.CompanyDTO>> userCompanies = batchGetUserCompanies(userIds);
            logger.info("🏢 Batch fetched companies for " + userCompanies.size() + " users");

            // Create DTOs for all users
            List<UserManagementDTO> result = new ArrayList<>();
            for (User user : users) {
                UserManagementDTO dto = new UserManagementDTO();
                String userId = user.getId();

                dto.setId(userId);
                dto.setUsername(user.getUsername());
                dto.setFirstName(user.getFirstName());
                dto.setLastName(user.getLastName());

                // Set email from batch results
                dto.setEmail(userEmails.getOrDefault(userId, null));

                // Set roles from batch results
                dto.setRoles(userRoles.getOrDefault(userId, new ArrayList<>()));

                // Set companies from batch results
                dto.setCompanies(userCompanies.getOrDefault(userId, new ArrayList<>()));

                result.add(dto);
            }

            logger.info("✅ Successfully processed " + result.size() + " users for search term: " + searchTerm);
            return new PageImpl<>(result, pageable, userPage.getTotalElements());
        } catch (Exception e) {
            logger.severe("❌ Error searching users: " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }

    /**
     * Get users with last name starting with the given letter
     *
     * @param letter the first letter of the last name
     * @param pageable pagination information
     * @return page of users with last name starting with the given letter
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementDTO> getUsersByLastNameStartingWith(String letter, Pageable pageable) {
        logger.info("🔍 Fetching users with last name starting with: " + letter);
        try {
            // Get users with last name starting with the given letter
            Page<User> userPage = userRepository.findByLastNameStartingWithIgnoreCase(letter, pageable);
            List<User> users = userPage.getContent();
            logger.info("📋 Found " + users.size() + " users with last name starting with: " + letter);

            if (users.isEmpty()) {
                logger.info("📋 No users found with last name starting with: " + letter);
                return Page.empty(pageable);
            }

            // Extract all user IDs
            List<String> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());

            // Batch fetch emails for all users in this page
            Map<String, String> userEmails = batchGetUserEmails(userIds);
            logger.info("📧 Batch fetched " + userEmails.size() + " emails");

            // Batch fetch roles for all users in this page
            Map<String, List<UserManagementDTO.RoleDTO>> userRoles = batchGetUserRoles(userIds);
            logger.info("🔑 Batch fetched roles for " + userRoles.size() + " users");

            // Batch fetch companies for all users in this page
            Map<String, List<UserManagementDTO.CompanyDTO>> userCompanies = batchGetUserCompanies(userIds);
            logger.info("🏢 Batch fetched companies for " + userCompanies.size() + " users");

            // Create DTOs for all users
            List<UserManagementDTO> result = new ArrayList<>();
            for (User user : users) {
                UserManagementDTO dto = new UserManagementDTO();
                String userId = user.getId();

                dto.setId(userId);
                dto.setUsername(user.getUsername());
                dto.setFirstName(user.getFirstName());
                dto.setLastName(user.getLastName());

                // Set email from batch results
                dto.setEmail(userEmails.getOrDefault(userId, null));

                // Set roles from batch results
                dto.setRoles(userRoles.getOrDefault(userId, new ArrayList<>()));

                // Set companies from batch results
                dto.setCompanies(userCompanies.getOrDefault(userId, new ArrayList<>()));

                result.add(dto);
            }

            logger.info("✅ Successfully processed " + result.size() + " users with last name starting with: " + letter);
            return new PageImpl<>(result, pageable, userPage.getTotalElements());
        } catch (Exception e) {
            logger.severe("❌ Error fetching users by last name letter: " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }

    /**
     * Get users with username starting with the given letter
     *
     * @param letter the first letter of the username
     * @param pageable pagination information
     * @return page of users with username starting with the given letter
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementDTO> getUsersByUsernameStartingWith(String letter, Pageable pageable) {
        logger.info("🔍 Fetching users with username starting with: " + letter);
        try {
            // Get users with username starting with the given letter
            Page<User> userPage = userRepository.findByUsernameStartingWithIgnoreCase(letter, pageable);
            List<User> users = userPage.getContent();
            logger.info("📋 Found " + users.size() + " users with username starting with: " + letter);

            if (users.isEmpty()) {
                logger.info("📋 No users found with username starting with: " + letter);
                return Page.empty(pageable);
            }

            // Extract all user IDs
            List<String> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());

            // Batch fetch emails for all users in this page
            Map<String, String> userEmails = batchGetUserEmails(userIds);
            logger.info("📧 Batch fetched " + userEmails.size() + " emails");

            // Batch fetch roles for all users in this page
            Map<String, List<UserManagementDTO.RoleDTO>> userRoles = batchGetUserRoles(userIds);
            logger.info("🔑 Batch fetched roles for " + userRoles.size() + " users");

            // Batch fetch companies for all users in this page
            Map<String, List<UserManagementDTO.CompanyDTO>> userCompanies = batchGetUserCompanies(userIds);
            logger.info("🏢 Batch fetched companies for " + userCompanies.size() + " users");

            // Create DTOs for all users
            List<UserManagementDTO> result = new ArrayList<>();
            for (User user : users) {
                UserManagementDTO dto = new UserManagementDTO();
                String userId = user.getId();

                dto.setId(userId);
                dto.setUsername(user.getUsername());
                dto.setFirstName(user.getFirstName());
                dto.setLastName(user.getLastName());

                // Set email from batch results
                dto.setEmail(userEmails.getOrDefault(userId, null));

                // Set roles from batch results
                dto.setRoles(userRoles.getOrDefault(userId, new ArrayList<>()));

                // Set companies from batch results
                dto.setCompanies(userCompanies.getOrDefault(userId, new ArrayList<>()));

                result.add(dto);
            }

            logger.info("✅ Successfully processed " + result.size() + " users with username starting with: " + letter);
            return new PageImpl<>(result, pageable, userPage.getTotalElements());
        } catch (Exception e) {
            logger.severe("❌ Error fetching users by username letter: " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }

    /**
     * Get all distinct first letters of user last names
     *
     * @return list of distinct first letters of user last names
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctLastNameFirstLetters() {
        logger.info("🔍 Fetching distinct first letters of user last names");
        try {
            // Use JDBC to get distinct first letters of last names
            String sql = "SELECT DISTINCT UPPER(SUBSTRING(lastName, 1, 1)) as letter " +
                         "FROM users " +
                         "WHERE lastName IS NOT NULL AND LENGTH(lastName) > 0 " +
                         "ORDER BY letter";

            List<String> letters = jdbcTemplate.queryForList(sql, String.class);

            // If no letters found, return default alphabet
            if (letters.isEmpty()) {
                logger.info("📋 No distinct letters found, returning default alphabet");
                return List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", 
                               "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
            }

            logger.info("✅ Found " + letters.size() + " distinct first letters of last names");
            return letters;
        } catch (Exception e) {
            logger.severe("❌ Error fetching distinct first letters: " + e.getMessage());
            e.printStackTrace();
            // Return default alphabet on error
            return List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", 
                           "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
        }
    }

    /**
     * Get all distinct first letters of usernames
     *
     * @return list of distinct first letters of usernames
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctUsernameFirstLetters() {
        logger.info("🔍 Fetching distinct first letters of usernames");
        try {
            // Use JDBC to get distinct first letters of usernames
            String sql = "SELECT DISTINCT UPPER(SUBSTRING(username, 1, 1)) as letter " +
                         "FROM users " +
                         "WHERE username IS NOT NULL AND LENGTH(username) > 0 " +
                         "ORDER BY letter";

            List<String> letters = jdbcTemplate.queryForList(sql, String.class);

            // If no letters found, return default alphabet
            if (letters.isEmpty()) {
                logger.info("📋 No distinct letters found, returning default alphabet");
                return List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", 
                               "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
            }

            logger.info("✅ Found " + letters.size() + " distinct first letters of usernames");
            return letters;
        } catch (Exception e) {
            logger.severe("❌ Error fetching distinct first letters: " + e.getMessage());
            e.printStackTrace();
            // Return default alphabet on error
            return List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", 
                           "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
        }
    }

    /**
     * Update user details from map
     *
     * @param userData map containing user update data
     * @return true if update was successful, false otherwise
     */
    @Override
    @Transactional
    public boolean updateUser(Map<String, Object> userData) {
        logger.info("🔄 Updating user with data from map");

        try {
            // Extract user ID from the map
            String userId = (String) userData.get("id");
            if (userId == null || userId.isBlank()) {
                logger.warning("⚠️ User ID is missing or invalid");
                return false;
            }

            // Check if user exists
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.warning("⚠️ User not found with ID: " + userId);
                return false;
            }

            // Update basic user properties through reflection
            if (userData.containsKey("username")) {
                setField(user, "username", userData.get("username"));
            }

            if (userData.containsKey("firstName")) {
                setField(user, "firstName", userData.get("firstName"));
            }

            if (userData.containsKey("lastName")) {
                setField(user, "lastName", userData.get("lastName"));
            }

            // Save updated user
            userRepository.save(user);
            logger.info("✅ Updated basic user info for: " + userId);

            // Update email if provided
            if (userData.containsKey("email")) {
                try {
                    String email = (String) userData.get("email");
                    updateUserEmail(userId, email);
                    logger.info("✅ Updated email for user: " + userId);
                } catch (Exception e) {
                    logger.severe("❌ Error updating email for user " + userId + ": " + e.getMessage());
                    throw e; // Rethrow to roll back the entire transaction
                }
            }

            // Update phone if provided
            if (userData.containsKey("sms")) {
                try {
                    String phone = (String) userData.get("sms");
                    updateUserPhone(userId, phone);
                    logger.info("✅ Updated phone for user: " + userId);
                } catch (Exception e) {
                    logger.severe("❌ Error updating phone for user " + userId + ": " + e.getMessage());
                    throw e; // Rethrow to roll back the entire transaction
                }
            }

            // Update roles if provided
            if (userData.containsKey("roleIds")) {
                try {
                    @SuppressWarnings("unchecked")
                    List<String> roleIds = (List<String>) userData.get("roleIds");
                    updateUserRoles(userId, roleIds);
                    logger.info("✅ Updated roles for user: " + userId);
                } catch (Exception e) {
                    logger.severe("❌ Error updating roles for user " + userId + ": " + e.getMessage());
                    throw e; // Rethrow to roll back the entire transaction
                }
            }

            // Update companies if provided
            if (userData.containsKey("companyIds")) {
                try {
                    @SuppressWarnings("unchecked")
                    List<String> companyIds = (List<String>) userData.get("companyIds");
                    updateUserCompanies(userId, companyIds);
                    logger.info("✅ Updated companies for user: " + userId);
                } catch (Exception e) {
                    logger.severe("❌ Error updating companies for user " + userId + ": " + e.getMessage());
                    throw e; // Rethrow to roll back the entire transaction
                }
            }

            logger.info("✅ Successfully updated user: " + userId);
            return true;
        } catch (Exception e) {
            logger.severe("❌ Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Set field value through reflection
     *
     * @param obj the object
     * @param fieldName the field name
     * @param value the value to set
     */
    private void setField(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            logger.warning("⚠️ Error setting field " + fieldName + ": " + e.getMessage());
        }
    }

    /**
     * Get email type ID dynamically
     *
     * @return the email type ID or null if not found
     */
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

    /**
     * Get SMS type ID dynamically
     *
     * @return the SMS type ID or null if not found
     */
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
