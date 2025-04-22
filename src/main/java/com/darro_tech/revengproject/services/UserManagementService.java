package com.darro_tech.revengproject.services;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.darro_tech.revengproject.repositories.UserRepository;
import com.darro_tech.revengproject.repositories.UserRoleRepository;

@Service
public class UserManagementService {
    
    private static final Logger logger = Logger.getLogger(UserManagementService.class.getName());
    
    // Email contact type ID from the Next.js application
    private static final String EMAIL_TYPE_ID = "92b85bd9-d3f4-11ed-b336-02c88e45a32e";

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
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
} 