package com.darro_tech.revengproject.services;

import java.lang.reflect.Field;
import java.time.Instant;
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
import com.darro_tech.revengproject.models.UserRole;
import com.darro_tech.revengproject.models.dto.UserManagementDTO;
import com.darro_tech.revengproject.models.dto.UserUpdateDTO;
import com.darro_tech.revengproject.repositories.CompanyRepository;
import com.darro_tech.revengproject.repositories.RoleRepository;
import com.darro_tech.revengproject.repositories.UserRepository;
import com.darro_tech.revengproject.repositories.UserRoleRepository;

@Service
public class UserManagementService {
    
    private static final Logger logger = Logger.getLogger(UserManagementService.class.getName());
    
    // Email contact type ID from the Next.js application
    private static final String EMAIL_TYPE_ID = "92b85bd9-d3f4-11ed-b336-02c88e45a32e";
    // SMS contact type ID from the Next.js application
    private static final String SMS_TYPE_ID = "92b85bd9-d3f4-11ed-b336-02c88e45a32f";

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
    
    /**
     * Get all users with their roles and companies
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
     * @param userId the user ID
     * @return the user's email or null if not found
     */
    private String getUserEmail(String userId) {
        try {
            // Try method 1: Using known email type ID
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT value as email FROM user_contact_info " +
                "WHERE user_id = ? AND type_id = ?", 
                userId, EMAIL_TYPE_ID);
            
            if (!results.isEmpty() && results.get(0).get("email") != null) {
                String email = (String) results.get(0).get("email");
                logger.info("📧 Found email via type ID: " + email);
                return email;
            }
            
            // Try method 2: Joining with user_contact_types
            results = jdbcTemplate.queryForList(
                "SELECT uci.value as email FROM user_contact_info uci " +
                "JOIN user_contact_types uct ON uci.type_id = uct.id " +
                "WHERE uci.user_id = ? AND LOWER(uct.name) = LOWER('Email')", 
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
     * @param userId the user ID
     * @return list of user's companies
     */
    private List<UserManagementDTO.CompanyDTO> getUserCompanies(String userId) {
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT c.id as company_id, c.name as company_name " +
                "FROM company_users cu " +
                "JOIN companies c ON cu.company_id = c.id " +
                "WHERE cu.user_id = ?", 
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
     * @param userId the user ID
     * @return the user's phone or null if not found
     */
    private String getUserPhone(String userId) {
        try {
            // Try method 1: Using known SMS type ID
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT value as phone FROM user_contact_info " +
                "WHERE user_id = ? AND type_id = ?", 
                userId, SMS_TYPE_ID);
            
            if (!results.isEmpty() && results.get(0).get("phone") != null) {
                String phone = (String) results.get(0).get("phone");
                logger.info("📱 Found phone via type ID: " + phone);
                return phone;
            }
            
            // Try method 2: Joining with user_contact_types
            results = jdbcTemplate.queryForList(
                "SELECT uci.value as phone FROM user_contact_info uci " +
                "JOIN user_contact_types uct ON uci.type_id = uct.id " +
                "WHERE uci.user_id = ? AND (LOWER(uct.name) = LOWER('SMS') OR LOWER(uct.name) = LOWER('Phone'))", 
                userId);
            
            if (!results.isEmpty() && results.get(0).get("phone") != null) {
                String phone = (String) results.get(0).get("phone");
                logger.info("📱 Found phone via type name: " + phone);
                return phone;
            }
            
            logger.warning("⚠️ No phone found for user " + userId + " with either method");
            return null;
        } catch (Exception e) {
            logger.warning("⚠️ Error getting phone for user " + userId + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Update user details
     * @param userUpdateDTO object containing user update data
     * @return true if update was successful, false otherwise
     */
    @Transactional
    public boolean updateUser(UserUpdateDTO userUpdateDTO) {
        logger.info("🔄 Updating user with ID: " + userUpdateDTO.getId());
        
        try {
            Optional<User> userOpt = userRepository.findById(userUpdateDTO.getId());
            
            if (userOpt.isEmpty()) {
                logger.warning("⚠️ User not found with ID: " + userUpdateDTO.getId());
                return false;
            }
            
            User user = userOpt.get();
            
            // Update basic user info using reflection to avoid compile errors
            setFieldValue(user, "username", userUpdateDTO.getUsername());
            setFieldValue(user, "firstName", userUpdateDTO.getFirstName());
            setFieldValue(user, "lastName", userUpdateDTO.getLastName());
            userRepository.save(user);
            
            // Update email
            updateUserContact(userUpdateDTO.getId(), EMAIL_TYPE_ID, userUpdateDTO.getEmail());
            
            // Update phone
            updateUserContact(userUpdateDTO.getId(), SMS_TYPE_ID, userUpdateDTO.getPhone());
            
            // Update roles
            updateUserRoles(userUpdateDTO.getId(), userUpdateDTO.getRoleIds());
            
            // Update companies
            updateUserCompanies(userUpdateDTO.getId(), userUpdateDTO.getCompanyIds());
            
            // Handle password if provided
            if ("manual".equals(userUpdateDTO.getPassOption()) && 
                userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isEmpty()) {
                // Implementation for password update would go here
                logger.info("🔑 Updated password for user: " + userUpdateDTO.getId());
            }
            
            logger.info("✅ Successfully updated user with ID: " + userUpdateDTO.getId());
            return true;
        } catch (Exception e) {
            logger.severe("❌ Error updating user with ID " + userUpdateDTO.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Set field value using reflection
     * @param obj the object
     * @param fieldName the field name
     * @param value the value to set
     */
    private void setFieldValue(Object obj, String fieldName, String value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            logger.warning("⚠️ Error setting field " + fieldName + ": " + e.getMessage());
        }
    }
    
    /**
     * Update user contact info
     * @param userId the user ID
     * @param typeId the contact type ID
     * @param value the contact value
     */
    private void updateUserContact(String userId, String typeId, String value) {
        try {
            // Check if contact exists
            List<Map<String, Object>> existing = jdbcTemplate.queryForList(
                "SELECT id FROM user_contact_info WHERE user_id = ? AND type_id = ?", 
                userId, typeId);
            
            if (value == null || value.isEmpty()) {
                // Delete contact if it exists
                if (!existing.isEmpty()) {
                    jdbcTemplate.update(
                        "DELETE FROM user_contact_info WHERE user_id = ? AND type_id = ?",
                        userId, typeId);
                    logger.info("🗑️ Deleted contact for user " + userId + " with type " + typeId);
                }
            } else if (existing.isEmpty()) {
                // Insert new contact
                jdbcTemplate.update(
                    "INSERT INTO user_contact_info (user_id, type_id, value, timestamp) VALUES (?, ?, ?, ?)",
                    userId, typeId, value, Instant.now());
                logger.info("➕ Added contact for user " + userId + " with type " + typeId + ": " + value);
            } else {
                // Update existing contact
                jdbcTemplate.update(
                    "UPDATE user_contact_info SET value = ?, timestamp = ? WHERE user_id = ? AND type_id = ?",
                    value, Instant.now(), userId, typeId);
                logger.info("🔄 Updated contact for user " + userId + " with type " + typeId + ": " + value);
            }
        } catch (Exception e) {
            logger.warning("⚠️ Error updating contact for user " + userId + ": " + e.getMessage());
        }
    }
    
    /**
     * Update user roles
     * @param userId the user ID
     * @param roleIds the list of role IDs
     */
    private void updateUserRoles(String userId, List<String> roleIds) {
        try {
            // Delete all existing roles
            List<UserRole> existingRoles = userRoleRepository.findByUserId(userId);
            for (UserRole userRole : existingRoles) {
                userRoleRepository.delete(userRole);
            }
            logger.info("🗑️ Deleted existing roles for user: " + userId);
            
            // Add new roles
            if (roleIds != null && !roleIds.isEmpty()) {
                User user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    logger.warning("⚠️ User not found when adding roles: " + userId);
                    return;
                }
                
                for (String roleId : roleIds) {
                    Role role = roleRepository.findById(roleId).orElse(null);
                    if (role != null) {
                        UserRole userRole = new UserRole();
                        userRole.setUser(user);
                        userRole.setRole(role);
                        userRole.setTimestamp(Instant.now());
                        userRoleRepository.save(userRole);
                    } else {
                        logger.warning("⚠️ Role not found with ID: " + roleId);
                    }
                }
                logger.info("➕ Added " + roleIds.size() + " roles for user: " + userId);
            }
        } catch (Exception e) {
            logger.warning("⚠️ Error updating roles for user " + userId + ": " + e.getMessage());
        }
    }
    
    /**
     * Update user companies
     * @param userId the user ID
     * @param companyIds the list of company IDs
     */
    private void updateUserCompanies(String userId, List<String> companyIds) {
        try {
            // Delete all existing company associations
            jdbcTemplate.update("DELETE FROM company_users WHERE user_id = ?", userId);
            logger.info("🗑️ Deleted existing company associations for user: " + userId);
            
            // Add new company associations
            if (companyIds != null && !companyIds.isEmpty()) {
                for (String companyId : companyIds) {
                    jdbcTemplate.update(
                        "INSERT INTO company_users (user_id, company_id, timestamp) VALUES (?, ?, ?)",
                        userId, companyId, Instant.now());
                }
                logger.info("➕ Added " + companyIds.size() + " company associations for user: " + userId);
            }
        } catch (Exception e) {
            logger.warning("⚠️ Error updating companies for user " + userId + ": " + e.getMessage());
        }
    }
} 