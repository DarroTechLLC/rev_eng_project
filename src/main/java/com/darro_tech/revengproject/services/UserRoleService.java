package com.darro_tech.revengproject.services;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darro_tech.revengproject.models.Role;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.UserRole;
import com.darro_tech.revengproject.repositories.UserRoleRepository;

@Service
@Transactional
public class UserRoleService {
    
    private static final Logger logger = Logger.getLogger(UserRoleService.class.getName());
    
    @Autowired
    private UserRoleRepository userRoleRepository;

    /**
     * Check if the user has the admin role
     * @param user the user to check
     * @return true if the user has admin role, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }
        
        // First check if user is a Super Admin (Super Admins should have all Admin privileges)
        boolean isSuperAdmin = isSuperAdmin(user);
        if (isSuperAdmin) {
            logger.info("User " + getUserUsername(user) + " is a Super Admin, so automatically has Admin privileges");
            return true;
        }
        
        // Otherwise check for any role containing "Admin"
        boolean result = getUserRoles(user).stream()
                .anyMatch(role -> 
                    role.getName().toLowerCase().contains("admin")
                );
        
        logger.info("Checking if user " + getUserUsername(user) + " is Admin: " + result);
        return result;
    }

    /**
     * Check if the user has the super admin role
     * @param user the user to check
     * @return true if the user has super admin role, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isSuperAdmin(User user) {
        if (user == null) {
            return false;
        }
        
        List<Role> roles = getUserRoles(user);
        logger.info("User " + getUserUsername(user) + " has roles: " + 
                   roles.stream().map(Role::getName).collect(Collectors.joining(", ")));
        
        boolean result = roles.stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase("Super Admin"));
        
        logger.info("Checking if user " + getUserUsername(user) + " is Super Admin: " + result);
        return result;
    }

    /**
     * Get all roles for a user
     * @param user the user
     * @return list of roles
     */
    @Transactional(readOnly = true)
    public List<Role> getUserRoles(User user) {
        if (user == null) {
            return List.of();
        }
        
        String userId = getUserId(user);
        logger.info("Getting roles for user ID: " + userId);
        
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        logger.info("Found " + userRoles.size() + " roles for user " + getUserUsername(user));
        
        // Force initialization of Role entities within the transaction
        return userRoles.stream()
                .map(userRole -> {
                    Role role = userRole.getRole();
                    // Access properties to force initialization
                    String roleName = role.getName();
                    logger.info("User " + getUserUsername(user) + " has role: " + roleName);
                    return role;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Safely get username using reflection
     */
    private String getUserUsername(User user) {
        try {
            Field usernameField = user.getClass().getDeclaredField("username");
            usernameField.setAccessible(true);
            Object value = usernameField.get(user);
            return value != null ? value.toString() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    /**
     * Safely get user ID using reflection
     */
    private String getUserId(User user) {
        try {
            Field idField = user.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return (String) idField.get(user);
        } catch (Exception e) {
            // Fallback to toString if we can't get the ID
            return user.toString();
        }
    }
} 