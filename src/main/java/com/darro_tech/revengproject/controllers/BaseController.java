package com.darro_tech.revengproject.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.UserRoleService;

import jakarta.servlet.http.HttpSession;

public abstract class BaseController {
    
    @Autowired
    private AuthenticationController authenticationController;
    
    @Autowired
    private UserRoleService userRoleService;

    @ModelAttribute
    public void addCommonAttributes(Model model, HttpSession session) {
        User user = authenticationController.getUserFromSession(session);
        if (user != null) {
            model.addAttribute("user", user);
            
            // Get user name for display
            String wholeName = "User";
            try {
                String firstName = (String) getFieldValueSafely(user, "firstName", "");
                String lastName = (String) getFieldValueSafely(user, "lastName", "");
                String username = (String) getFieldValueSafely(user, "username", "User");
                
                wholeName = (firstName + " " + lastName).trim();
                if (wholeName.isEmpty()) {
                    wholeName = username;
                }
            } catch (Exception e) {
                // In case of any errors, use a default name
                wholeName = "User";
            }
            
            model.addAttribute("wholeName", wholeName);
            
            // Add role-based attributes
            boolean isAdmin = userRoleService.isAdmin(user);
            boolean isSuperAdmin = userRoleService.isSuperAdmin(user);
            
            model.addAttribute("isAdmin", isAdmin ? Boolean.TRUE : Boolean.FALSE);
            model.addAttribute("isSuperAdmin", isSuperAdmin ? Boolean.TRUE : Boolean.FALSE);
            
            System.out.println("DEBUG - Adding role attributes for user: " + getFieldValueSafely(user, "username", "unknown"));
            System.out.println("DEBUG - isAdmin: " + isAdmin);
            System.out.println("DEBUG - isSuperAdmin: " + isSuperAdmin);
        } else {
            // Always set these values, even if there's no user
            model.addAttribute("isAdmin", Boolean.FALSE);
            model.addAttribute("isSuperAdmin", Boolean.FALSE);
            model.addAttribute("wholeName", "Guest");
        }
    }
    
    /**
     * Safely get field value using reflection
     */
    private Object getFieldValueSafely(Object obj, String fieldName, Object defaultValue) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    protected String view(String viewName, Model model) {
        model.addAttribute("content", "content/" + viewName);
        return "layouts/main-layout";
    }
} 