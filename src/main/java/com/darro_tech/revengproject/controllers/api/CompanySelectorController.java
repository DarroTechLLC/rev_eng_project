package com.darro_tech.revengproject.controllers.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.dto.CompanyDTO;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.UserRoleService;
import com.darro_tech.revengproject.services.UserService;

import jakarta.servlet.http.HttpSession;

/**
 * Consolidated API controller for company selection functionality. This
 * controller handles all company-related operations including listing available
 * companies and selecting the current company.
 */
@RestController
@RequestMapping("/api/companies")
public class CompanySelectorController {

    private static final Logger logger = Logger.getLogger(CompanySelectorController.class.getName());

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleService userRoleService;

    /**
     * Get all companies available for the current user
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserCompanies(HttpSession session) {
        logger.info("🏢 Getting companies for user");

        User user = getUserFromSession(session);
        if (user == null) {
            logger.warning("❌ User not authenticated when fetching companies");
            return ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "User not authenticated")
            );
        }

        boolean isSuperAdmin = userRoleService.isSuperAdmin(user);
        List<CompanyDTO> companies = companyService.getUserCompanies(user.getId(), isSuperAdmin);

        // Get selected company from session
        String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");

        if (selectedCompanyId == null && !companies.isEmpty()) {
            // Auto-select the first company if none is selected
            selectedCompanyId = companies.get(0).getCompanyId();
            session.setAttribute("selectedCompanyId", selectedCompanyId);
            logger.info("🔄 Auto-selected first company: " + selectedCompanyId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("companies", companies);
        response.put("selectedCompanyId", selectedCompanyId);

        logger.info("📋 Returning " + companies.size() + " companies for user: " + user.getId()
                + ", selected: " + (selectedCompanyId != null ? selectedCompanyId : "none"));
        return ResponseEntity.ok(response);
    }

    /**
     * Check if user has access to a specific company
     */
    @GetMapping("/{companyId}/access")
    public ResponseEntity<Map<String, Object>> checkCompanyAccess(
            @PathVariable String companyId,
            HttpSession session) {
        logger.info("🔍 Checking company access: " + companyId);

        User user = getUserFromSession(session);
        if (user == null) {
            logger.warning("❌ User not authenticated when checking company access");
            return ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "User not authenticated")
            );
        }

        boolean isSuperAdmin = userRoleService.isSuperAdmin(user);
        if (isSuperAdmin) {
            logger.info("✅ Super admin access granted to company: " + companyId);
            return ResponseEntity.ok(Map.of("success", true, "hasAccess", true));
        }

        boolean hasAccess = companyService.userHasCompanyAccess(user.getId(), companyId);
        logger.info((hasAccess ? "✅" : "❌") + " User " + user.getId()
                + (hasAccess ? " has access" : " denied access") + " to company: " + companyId);

        return ResponseEntity.ok(Map.of("success", true, "hasAccess", hasAccess));
    }

    /**
     * Select a company (store selection in session)
     */
    @PostMapping("/select/{companyId}")
    public ResponseEntity<Map<String, Object>> selectCompany(
            @PathVariable("companyId") String companyId,
            HttpSession session) {

        logger.info("🔄 Attempting to select company: " + companyId);

        User user = getUserFromSession(session);
        if (user == null) {
            logger.warning("❌ Company selection failed: user not authenticated");
            return ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "User not authenticated")
            );
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
            return ResponseEntity.status(403).body(
                    Map.of("success", false, "message", "Access denied to this company")
            );
        }

        // Get the company details
        Optional<Company> companyOpt = companyService.getCompanyById(companyId);
        if (companyOpt.isEmpty()) {
            logger.warning("❓ Company selection failed: company not found " + companyId);
            return ResponseEntity.status(404).body(
                    Map.of("success", false, "message", "Company not found")
            );
        }

        Company company = companyOpt.get();

        // Store the selected company in session
        String previousCompanyId = (String) session.getAttribute("selectedCompanyId");
        session.setAttribute("selectedCompanyId", companyId);

        logger.info("✅ Company selection successful: " + companyId + " (" + company.getName() + ") for user " + user.getId()
                + " (previous: " + previousCompanyId + ")");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("selectedCompanyId", companyId);
        response.put("companyName", company.getName());
        response.put("logoUrl", company.getLogoUrl());

        return ResponseEntity.ok(response);
    }

    /**
     * Get the currently selected company
     */
    @GetMapping("/selected")
    public ResponseEntity<Map<String, Object>> getSelectedCompany(HttpSession session) {
        logger.info("🔍 Getting selected company");

        User user = getUserFromSession(session);
        if (user == null) {
            logger.warning("❌ User not authenticated when fetching selected company");
            return ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "User not authenticated")
            );
        }

        String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
        if (selectedCompanyId == null) {
            logger.info("ℹ️ No company selected for user: " + user.getId());
            return ResponseEntity.ok(Map.of("success", true, "selected", false));
        }

        Optional<Company> companyOpt = companyService.getCompanyById(selectedCompanyId);
        if (companyOpt.isEmpty()) {
            // Clear the invalid selection
            session.removeAttribute("selectedCompanyId");
            logger.warning("⚠️ Invalid company ID in session: " + selectedCompanyId);
            return ResponseEntity.ok(Map.of("success", true, "selected", false));
        }

        Company company = companyOpt.get();
        logger.info("✅ Selected company retrieved: " + company.getName() + " (" + selectedCompanyId + ")");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("selected", true);
        response.put("companyId", company.getId());
        response.put("companyName", company.getName());
        response.put("logoUrl", company.getLogoUrl() != null ? company.getLogoUrl() : "");

        return ResponseEntity.ok(response);
    }

    /**
     * Alternative endpoint for company selection - ensures compatibility with frontend
     * This duplicates the functionality of /select/{companyId} but with a different URL
     * to accommodate JavaScript code that may be using the alternative URL pattern
     */
    @PostMapping("/companies-all/select/{companyId}")
    public ResponseEntity<Map<String, Object>> selectCompanyAlternative(
            @PathVariable("companyId") String companyId,
            HttpSession session) {
        
        logger.info("🔄 Alternative endpoint: Attempting to select company: " + companyId);
        
        // Simply delegate to the main endpoint method
        return selectCompany(companyId, session);
    }

    /**
     * Debug endpoint to check session state
     */
    @GetMapping("/debug/session")
    public ResponseEntity<Map<String, Object>> debugSession(HttpSession session) {
        logger.info("🔧 Debug: Getting session info");

        User user = getUserFromSession(session);

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("sessionId", session.getId());
        sessionData.put("selectedCompanyId", session.getAttribute("selectedCompanyId"));
        sessionData.put("isAuthenticated", user != null);

        if (user != null) {
            sessionData.put("userId", user.getId());
            sessionData.put("userName", user.getFirstName() + " " + user.getLastName());
            sessionData.put("isSuperAdmin", userRoleService.isSuperAdmin(user));
            sessionData.put("isAdmin", userRoleService.isAdmin(user));
        }

        logger.info("📊 Session debug info generated");
        return ResponseEntity.ok(sessionData);
    }

    /**
     * Debug endpoint to check user session details
     */
    @GetMapping("/debug/user-session")
    public ResponseEntity<Map<String, Object>> debugUserSession(HttpSession session) {
        logger.info("🔧 Debug: Getting user session details");

        Map<String, Object> sessionData = new HashMap<>();

        // Get raw session attributes
        String userId = (String) session.getAttribute("user");
        sessionData.put("rawUserId", userId);

        // Get user from session
        User user = getUserFromSession(session);
        sessionData.put("userFound", user != null);

        if (user != null) {
            sessionData.put("userId", user.getId());
            sessionData.put("username", user.getUsername());
            sessionData.put("firstName", user.getFirstName());
            sessionData.put("lastName", user.getLastName());
        }

        // Get session info
        sessionData.put("sessionId", session.getId());
        sessionData.put("sessionAttributes", getSessionAttributes(session));

        logger.info("📊 User session debug info generated");
        return ResponseEntity.ok(sessionData);
    }

    /**
     * Test endpoint for checking authentication and resolving issues
     */
    @GetMapping("/test-auth")
    public ResponseEntity<Map<String, Object>> testAuth(HttpSession session, @RequestParam(required = false) String username) {
        logger.info("🧪 Testing auth with username: " + username);

        User sessionUser = getUserFromSession(session);
        Map<String, Object> response = new HashMap<>();

        response.put("sessionActive", session != null);
        response.put("sessionId", session != null ? session.getId() : null);
        response.put("userInSession", sessionUser != null);

        if (sessionUser != null) {
            response.put("sessionUserId", sessionUser.getId());
            response.put("sessionUsername", sessionUser.getUsername());
        }

        if (username != null && !username.trim().isEmpty()) {
            User foundUser = userService.findUserByUsername(username);
            if (foundUser != null) {
                response.put("userFound", true);
                response.put("userId", foundUser.getId());
                response.put("userRoles", userRoleService.getUserRoles(foundUser));
                response.put("isAdmin", userRoleService.isAdmin(foundUser));
                response.put("isSuperAdmin", userRoleService.isSuperAdmin(foundUser));
            }
        }

        logger.info("🧪 Auth test complete");
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to get session attributes for debugging
     */
    private Map<String, Object> getSessionAttributes(HttpSession session) {
        Map<String, Object> attributes = new HashMap<>();

        java.util.Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            Object value = session.getAttribute(name);
            attributes.put(name, value != null ? value.toString() : "null");
        }

        return attributes;
    }

    /**
     * Helper method to get user from session
     */
    private User getUserFromSession(HttpSession session) {
        String userId = (String) session.getAttribute("user");
        if (userId == null) {
            return null;
        }

        return userService.getUserById(userId).orElse(null);
    }
}
