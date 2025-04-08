package com.darro_tech.revengproject.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.darro_tech.revengproject.models.Role;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.UserRoleService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/fragments")
public class FragmentController {

    @Autowired
    private AuthenticationController authenticationController;
    
    @Autowired
    private UserRoleService userRoleService;
    
    @GetMapping("/topbar")
    public String topbar(Model model, HttpSession session) {
        User user = authenticationController.getUserFromSession(session);
        
        if (user != null) {
            model.addAttribute("user", user);
            
            // Get user name for display
            String wholeName = "User";
            try {
                String firstName = getFieldValueSafely(user, "firstName", "");
                String lastName = getFieldValueSafely(user, "lastName", "");
                String username = getFieldValueSafely(user, "username", "User");
                
                wholeName = (firstName + " " + lastName).trim();
                if (wholeName.isEmpty()) {
                    wholeName = username;
                }
            } catch (Exception e) {
                // In case of any errors, use a default name
                wholeName = "User";
            }
            
            model.addAttribute("wholeName", wholeName);
            
            // Add role-based attributes - FORCED TRUE for testing
            List<Role> roles = userRoleService.getUserRoles(user);
            System.out.println("FRAGMENT CONTROLLER - User roles: " + 
                roles.stream().map(Role::getName).collect(Collectors.joining(", ")));
            
            boolean isAdmin = userRoleService.isAdmin(user);
            boolean isSuperAdmin = userRoleService.isSuperAdmin(user);
            
            // IMPORTANT: Force admin privileges for debugging
            if (isSuperAdmin) {
                isAdmin = true; // Super admins should have all admin privileges
            }
            
            model.addAttribute("isAdmin", isAdmin ? Boolean.TRUE : Boolean.FALSE);
            model.addAttribute("isSuperAdmin", isSuperAdmin ? Boolean.TRUE : Boolean.FALSE);
            
            System.out.println("FRAGMENT CONTROLLER - isAdmin: " + isAdmin + " (Type: " + (isAdmin ? Boolean.TRUE : Boolean.FALSE).getClass().getName() + ")");
            System.out.println("FRAGMENT CONTROLLER - isSuperAdmin: " + isSuperAdmin + " (Type: " + (isSuperAdmin ? Boolean.TRUE : Boolean.FALSE).getClass().getName() + ")");
        }
        
        return "fragments/topbar :: topbar";
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