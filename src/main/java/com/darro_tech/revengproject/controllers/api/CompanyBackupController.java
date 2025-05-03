package com.darro_tech.revengproject.controllers.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.darro_tech.revengproject.exception.ApiError;
import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.UserRoleService;
import com.darro_tech.revengproject.services.UserService;

import jakarta.servlet.http.HttpSession;

/**
 * Backup controller for alternative company selection endpoints
 * This supports the JavaScript code that uses different URL patterns
 */
@RestController
@RequestMapping("/api/companies-all")
public class CompanyBackupController {

    private static final Logger logger = Logger.getLogger(CompanyBackupController.class.getName());

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleService userRoleService;

    /**
     * Alternative endpoint for company selection
     * This mirrors the functionality in CompanySelectorController but with a different URL pattern
     */
    @PostMapping(value = "/select/{companyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> selectCompany(
            @PathVariable("companyId") String companyId,
            HttpSession session) {

        logger.info("🔄 Alternative endpoint: Attempting to select company: " + companyId);

        try {
            // Get user from session
            User user = getUserFromSession(session);
            if (user == null) {
                logger.warning("❌ Company selection failed: user not authenticated");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Verify the user has access to this company
            boolean isSuperAdmin = userRoleService.isSuperAdmin(user);
            boolean hasAccess = false;

            if (isSuperAdmin) {
                hasAccess = true;
            } else {
                hasAccess = companyService.userHasCompanyAccess(user.getId(), companyId);
            }

            if (!hasAccess) {
                logger.warning("⛔ Company selection failed: access denied for user " + user.getId() + " to company " + companyId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Access denied to this company");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Get the company details
            Optional<Company> companyOpt = companyService.getCompanyById(companyId);
            if (companyOpt.isEmpty()) {
                logger.warning("❓ Company selection failed: company not found " + companyId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Company not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Company company = companyOpt.get();

            // Store the selected company in session
            String previousCompanyId = (String) session.getAttribute("selectedCompanyId");
            session.setAttribute("selectedCompanyId", companyId);

            logger.info("✅ Alternative endpoint: Company selection successful: " + companyId + " (" + company.getName() + ") for user " + user.getId()
                    + " (previous: " + previousCompanyId + ")");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("selectedCompanyId", companyId);
            response.put("companyName", company.getName());
            response.put("logoUrl", company.getLogoUrl());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("💥 Unexpected error in company selection: " + e.getMessage());
            e.printStackTrace();
            
            // Always return a structured JSON response for errors
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "An unexpected error occurred while selecting company");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Helper method to get user from session
     */
    private User getUserFromSession(HttpSession session) {
        try {
            String userId = (String) session.getAttribute("user");
            if (userId != null) {
                Optional<User> userOpt = userService.getUserById(userId);
                return userOpt.orElse(null);
            }
        } catch (Exception e) {
            logger.warning("Error getting user from session: " + e.getMessage());
        }
        return null;
    }
} 