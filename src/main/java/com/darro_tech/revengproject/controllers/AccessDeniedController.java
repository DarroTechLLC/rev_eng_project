package com.darro_tech.revengproject.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class AccessDeniedController {

    private static final Logger logger = LoggerFactory.getLogger(AccessDeniedController.class);

    @GetMapping("/access-denied")
    public String handleAccessDenied(
            @ModelAttribute("errorMessage") String errorMessage,
            @ModelAttribute("username") String username,
            @ModelAttribute("userRoles") String userRoles,
            @ModelAttribute("requestedUrl") String requestedUrl,
            @ModelAttribute("debugInfo") String debugInfo,
            Model model) {

        // Log the access denied event
        logger.error("🚫 Access denied for user '{}' with roles [{}] attempting to access: {}",
                username, userRoles, requestedUrl);
        logger.debug("🔍 Debug info: {}", debugInfo);

        // Add attributes to model if they came from flash attributes
        if (errorMessage != null && !errorMessage.isEmpty()) {
            model.addAttribute("errorMessage", errorMessage);
        }
        if (username != null && !username.isEmpty()) {
            model.addAttribute("username", username);
        }
        if (userRoles != null && !userRoles.isEmpty()) {
            model.addAttribute("userRoles", userRoles);
        }
        if (requestedUrl != null && !requestedUrl.isEmpty()) {
            model.addAttribute("requestedUrl", requestedUrl);
        }
        if (debugInfo != null && !debugInfo.isEmpty()) {
            model.addAttribute("debugInfo", debugInfo);
        }

        return "error/access-denied";
    }
}
