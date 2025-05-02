package com.darro_tech.revengproject.controllers.api;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.UserRoleService;
import com.darro_tech.revengproject.services.UserService;

import jakarta.servlet.http.HttpSession;

/**
 * REST API controller for user role-related functionality
 */
@RestController
@RequestMapping("/api/user-roles")
public class UserRolesApiController {

    private static final Logger logger = Logger.getLogger(UserRolesApiController.class.getName());

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleService userRoleService;

    /**
     * Get role information for the current user
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getUserRoles(HttpSession session) {
        logger.info("🔑 Getting user roles from session");

        // Get the user from session
        User user = getUserFromSession(session);

        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            logger.warning("❌ No user found in session");
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.ok(response);
        }

        boolean isAdmin = userRoleService.isAdmin(user);
        boolean isSuperAdmin = userRoleService.isSuperAdmin(user);

        response.put("success", true);
        response.put("userId", user.getId());
        response.put("userName", user.getUsername());
        response.put("fullName", user.getFirstName() + " " + user.getLastName());
        response.put("isAdmin", isAdmin);
        response.put("isSuperAdmin", isSuperAdmin);

        logger.info("✅ User roles retrieved successfully: admin=" + isAdmin + ", superAdmin=" + isSuperAdmin);

        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to get the user from session
     */
    private User getUserFromSession(HttpSession session) {
        String userId = (String) session.getAttribute("user");
        if (userId == null) {
            return null;
        }

        return userService.getUserById(userId).orElse(null);
    }
}
