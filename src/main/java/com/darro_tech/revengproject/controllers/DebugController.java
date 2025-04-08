package com.darro_tech.revengproject.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.darro_tech.revengproject.models.Role;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.UserRoleService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private UserRoleService userRoleService;
    
    @Autowired
    private AuthenticationController authenticationController;
    
    @GetMapping("/roles-table")
    @ResponseBody
    public List<Map<String, Object>> showRoles() {
        return jdbcTemplate.queryForList("SELECT * FROM roles");
    }
    
    @GetMapping("/user-roles")
    @ResponseBody
    public List<Map<String, Object>> showUserRoles() {
        return jdbcTemplate.queryForList(
            "SELECT u.username, r.name as role_name " +
            "FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "JOIN roles r ON ur.role_id = r.id"
        );
    }
    
    @GetMapping("/fix-role-names")
    @ResponseBody
    public String fixRoleNames() {
        // Check and fix super admin role
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
            "SELECT id, name FROM roles WHERE LOWER(name) = LOWER('Super Admin')"
        );
        
        List<String> changes = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            String id = (String)row.get("id");
            String name = (String)row.get("name");
            
            if (!name.equals("Super Admin")) {
                jdbcTemplate.update("UPDATE roles SET name = 'Super Admin' WHERE id = ?", id);
                changes.add("Updated role '" + name + "' to 'Super Admin'");
            }
        }
        
        // Check and fix admin role
        results = jdbcTemplate.queryForList(
            "SELECT id, name FROM roles WHERE LOWER(name) = LOWER('Admin')"
        );
        
        for (Map<String, Object> row : results) {
            String id = (String)row.get("id");
            String name = (String)row.get("name");
            
            if (!name.equals("Admin")) {
                jdbcTemplate.update("UPDATE roles SET name = 'Admin' WHERE id = ?", id);
                changes.add("Updated role '" + name + "' to 'Admin'");
            }
        }
        
        if (changes.isEmpty()) {
            return "No role name fixes needed";
        } else {
            return String.join("<br>", changes);
        }
    }
    
    @GetMapping("/ensure-super-admin-role")
    @ResponseBody
    public String ensureSuperAdminRole() {
        StringBuilder result = new StringBuilder();
        
        // First check if Super Admin role exists
        List<Map<String, Object>> roles = jdbcTemplate.queryForList(
            "SELECT id, name FROM roles WHERE LOWER(name) = LOWER('Super Admin')"
        );
        
        String superAdminRoleId;
        
        if (roles.isEmpty()) {
            // Create Super Admin role
            superAdminRoleId = java.util.UUID.randomUUID().toString();
            jdbcTemplate.update(
                "INSERT INTO roles (id, name, timestamp) VALUES (?, 'Super Admin', CURRENT_TIMESTAMP)",
                superAdminRoleId
            );
            result.append("Created new Super Admin role with ID: ").append(superAdminRoleId).append("<br>");
        } else {
            // Get existing role ID
            superAdminRoleId = (String)roles.get(0).get("id");
            result.append("Found existing Super Admin role with ID: ").append(superAdminRoleId).append("<br>");
        }
        
        // Get the latest logged in user (or default to the first user)
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
            "SELECT id, username FROM users LIMIT 1"
        );
        
        if (!users.isEmpty()) {
            String userId = (String)users.get(0).get("id");
            String username = (String)users.get(0).get("username");
            
            // Check if user already has the Super Admin role
            List<Map<String, Object>> userRoles = jdbcTemplate.queryForList(
                "SELECT id FROM user_roles WHERE user_id = ? AND role_id = ?",
                userId, superAdminRoleId
            );
            
            if (userRoles.isEmpty()) {
                // Assign Super Admin role to the user
                jdbcTemplate.update(
                    "INSERT INTO user_roles (user_id, role_id, timestamp) VALUES (?, ?, CURRENT_TIMESTAMP)",
                    userId, superAdminRoleId
                );
                result.append("Assigned Super Admin role to user: ").append(username);
            } else {
                result.append("User ").append(username).append(" already has Super Admin role");
            }
        } else {
            result.append("No users found in the database");
        }
        
        return result.toString();
    }
    
    @GetMapping("/list-users")
    @ResponseBody
    public List<Map<String, Object>> listUsers() {
        return jdbcTemplate.queryForList("SELECT id, username FROM users");
    }
    
    @GetMapping("/make-super-admin")
    @ResponseBody
    public String makeSuperAdmin(String userId) {
        if (userId == null || userId.isEmpty()) {
            return "Please provide a userId parameter";
        }
        
        // First check if Super Admin role exists
        List<Map<String, Object>> roles = jdbcTemplate.queryForList(
            "SELECT id, name FROM roles WHERE LOWER(name) = LOWER('Super Admin')"
        );
        
        String superAdminRoleId;
        
        if (roles.isEmpty()) {
            // Create Super Admin role
            superAdminRoleId = java.util.UUID.randomUUID().toString();
            jdbcTemplate.update(
                "INSERT INTO roles (id, name, timestamp) VALUES (?, 'Super Admin', CURRENT_TIMESTAMP)",
                superAdminRoleId
            );
        } else {
            // Get existing role ID
            superAdminRoleId = (String)roles.get(0).get("id");
        }
        
        // Check if user exists
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
            "SELECT username FROM users WHERE id = ?", 
            userId
        );
        
        if (users.isEmpty()) {
            return "User not found with ID: " + userId;
        }
        
        String username = (String)users.get(0).get("username");
        
        // Check if user already has the Super Admin role
        List<Map<String, Object>> userRoles = jdbcTemplate.queryForList(
            "SELECT id FROM user_roles WHERE user_id = ? AND role_id = ?",
            userId, superAdminRoleId
        );
        
        if (userRoles.isEmpty()) {
            // Assign Super Admin role to the user
            jdbcTemplate.update(
                "INSERT INTO user_roles (user_id, role_id, timestamp) VALUES (?, ?, CURRENT_TIMESTAMP)",
                userId, superAdminRoleId
            );
            return "Assigned Super Admin role to user: " + username;
        } else {
            return "User " + username + " already has Super Admin role";
        }
    }
    
    @GetMapping("/admin-test")
    @ResponseBody
    public String adminTest(HttpSession session) {
        User user = authenticationController.getUserFromSession(session);
        
        if (user == null) {
            return "Not logged in";
        }
        
        StringBuilder result = new StringBuilder("<h1>Admin Privileges Test</h1>");
        
        try {
            // Use reflection to safely get ID
            java.lang.reflect.Field idField = user.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            String userId = (String) idField.get(user);
            
            result.append("<p>User ID: ").append(userId).append("</p>");
            
            boolean isAdmin = userRoleService.isAdmin(user);
            boolean isSuperAdmin = userRoleService.isSuperAdmin(user);
            
            result.append("<p>isAdmin: ").append(isAdmin).append("</p>");
            result.append("<p>isSuperAdmin: ").append(isSuperAdmin).append("</p>");
            
            result.append("<h2>User Roles:</h2><ul>");
            for (Role role : userRoleService.getUserRoles(user)) {
                result.append("<li>").append(role.getName()).append("</li>");
            }
            result.append("</ul>");
            
            // Add direct links to admin pages for testing
            result.append("<h2>Admin Links:</h2><ul>");
            result.append("<li><a href='/admin/users'>Manage Users</a></li>");
            result.append("<li><a href='/admin/companies'>Companies</a></li>");
            result.append("<li><a href='/admin/farms'>Farms</a></li>");
            result.append("<li><a href='/admin/alerts'>Website Alerts</a></li>");
            result.append("</ul>");
            
            // Add force role buttons
            result.append("<h2>Force Role Actions:</h2>");
            result.append("<p><a href='/debug/make-super-admin?userId=").append(userId)
                  .append("' style='padding:8px; background:green; color:white; text-decoration:none;'>")
                  .append("Make Super Admin</a></p>");
            result.append("<p><a href='/debug/ensure-super-admin-role' style='padding:8px; background:blue; color:white; text-decoration:none;'>")
                  .append("Add Super Admin Role to First User</a></p>");
            
        } catch (Exception e) {
            result.append("<p>Error getting roles: ").append(e.getMessage()).append("</p>");
        }
        
        result.append("<p><a href='/'>Back to Home</a></p>");
        
        return result.toString();
    }

    @GetMapping("/roles")
    @ResponseBody
    public String debugRoles(HttpSession session, Model model) {
        User user = authenticationController.getUserFromSession(session);
        
        if (user == null) {
            return "No user logged in";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Role Debug Info</h2>");
        
        // Get username
        String username = "";
        try {
            java.lang.reflect.Field field = user.getClass().getDeclaredField("username");
            field.setAccessible(true);
            username = (String) field.get(user);
        } catch (Exception e) {
            username = "unknown";
        }
        
        sb.append("<p>Username: ").append(username).append("</p>");
        
        // Check roles
        boolean isAdmin = userRoleService.isAdmin(user);
        boolean isSuperAdmin = userRoleService.isSuperAdmin(user);
        
        sb.append("<p>isAdmin: ").append(isAdmin).append(" (").append(isAdmin ? "TRUE" : "FALSE").append(")</p>");
        sb.append("<p>isSuperAdmin: ").append(isSuperAdmin).append(" (").append(isSuperAdmin ? "TRUE" : "FALSE").append(")</p>");
        
        // Check model attributes
        model.addAttribute("isAdmin", isAdmin ? Boolean.TRUE : Boolean.FALSE);
        model.addAttribute("isSuperAdmin", isSuperAdmin ? Boolean.TRUE : Boolean.FALSE);
        
        sb.append("<h3>Model Attributes</h3>");
        sb.append("<p>isAdmin: ").append(model.getAttribute("isAdmin"))
          .append(" (class: ").append(model.getAttribute("isAdmin") != null ? model.getAttribute("isAdmin").getClass().getName() : "null").append(")</p>");
        sb.append("<p>isSuperAdmin: ").append(model.getAttribute("isSuperAdmin"))
          .append(" (class: ").append(model.getAttribute("isSuperAdmin") != null ? model.getAttribute("isSuperAdmin").getClass().getName() : "null").append(")</p>");
        
        return sb.toString();
    }

    @GetMapping("/roles-api")
    @ResponseBody
    public Map<String, Object> getRolesJson(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = authenticationController.getUserFromSession(session);
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "Not logged in");
            return response;
        }
        
        boolean isAdmin = userRoleService.isAdmin(user);
        boolean isSuperAdmin = userRoleService.isSuperAdmin(user);
        
        response.put("success", true);
        response.put("isAdmin", isAdmin);
        response.put("isSuperAdmin", isSuperAdmin);
        response.put("username", getFieldValueSafely(user, "username", "unknown"));
        
        return response;
    }

    /**
     * Safely get field value using reflection
     */
    private String getFieldValueSafely(Object obj, String fieldName, String defaultValue) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            return value != null ? value.toString() : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
} 