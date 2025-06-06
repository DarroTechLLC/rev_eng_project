package com.darro_tech.revengproject.controllers.api;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.darro_tech.revengproject.controllers.AuthenticationController;
import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.dto.CompanyDTO;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.FileStorageService;
import com.darro_tech.revengproject.services.UserRoleService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/companies")
public class CompanyApiController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyApiController.class);

    @Autowired
    private CompanyService companyService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private UserRoleService userRoleService;

    @PostMapping("/list")
    public ResponseEntity<?> listCompanies() {
        return listAllCompanies();
    }

    @PostMapping("/list-all")
    public ResponseEntity<?> listAllCompanies() {
        logger.info("🏢 API request for company list");
        try {
            List<Company> companies = companyService.getAllCompanies();
            List<Map<String, String>> companyList = companies.stream()
                    .map(company -> {
                        Map<String, String> companyMap = new HashMap<>();
                        companyMap.put("companyId", company.getId());
                        companyMap.put("companyName", company.getName());
                        return companyMap;
                    })
                    .sorted(Comparator.comparing(map -> map.get("companyName")))
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("data", companyList);
            logger.info("🏢 Returning {} companies", companyList.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error fetching company list", e);
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to fetch companies: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/list-with-access")
    public ResponseEntity<?> getCompanies(HttpSession session) {
        logger.info("🏢 Fetching all companies");
        try {
            // Get current user from session
            User currentUser = authenticationController.getUserFromSession(session);
            if (currentUser == null) {
                logger.error("❌ User not authenticated when accessing companies");
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "User not authenticated"
                ));
            }

            // Check if user is super admin
            boolean isSuperAdmin = userRoleService.isSuperAdmin(currentUser);
            logger.debug("👑 User super admin status: {}", isSuperAdmin);

            // Get companies based on user access
            List<CompanyDTO> companyDTOs = companyService.getUserCompanies(currentUser.getId(), isSuperAdmin);
            logger.info("📋 Found {} companies for user", companyDTOs.size());

            // Sort companies alphabetically by name
            companyDTOs = companyDTOs.stream()
                    .sorted(Comparator.comparing(CompanyDTO::getCompanyName))
                    .collect(Collectors.toList());

            // Get selected company ID from session
            String initialSelectedCompanyId = (String) session.getAttribute("selectedCompanyId");

            // Create final variable for use in lambda expressions
            final String[] selectedCompanyIdHolder = new String[1];
            selectedCompanyIdHolder[0] = initialSelectedCompanyId;

            // If no company is selected, default to first alphabetical
            if (selectedCompanyIdHolder[0] == null && !companyDTOs.isEmpty()) {
                selectedCompanyIdHolder[0] = companyDTOs.get(0).getCompanyId();
                session.setAttribute("selectedCompanyId", selectedCompanyIdHolder[0]);
                logger.info("🔄 No company selected, defaulting to first company: {}", selectedCompanyIdHolder[0]);
            }

            // If selected company is not in user's allowed list, reset to first available
            boolean hasAccessToSelected = companyDTOs.stream()
                    .anyMatch(c -> c.getCompanyId().equals(selectedCompanyIdHolder[0]));

            if (!hasAccessToSelected && !companyDTOs.isEmpty()) {
                selectedCompanyIdHolder[0] = companyDTOs.get(0).getCompanyId();
                session.setAttribute("selectedCompanyId", selectedCompanyIdHolder[0]);
                logger.info("🔐 User has no access to selected company, resetting to: {}", selectedCompanyIdHolder[0]);
            }

            // Add more detailed company information including logo URLs
            List<Map<String, Object>> companyDetails = companyDTOs.stream()
                    .map(company -> {
                        Map<String, Object> details = new HashMap<>();
                        details.put("id", company.getCompanyId());
                        details.put("name", company.getCompanyName());
                        details.put("displayName", company.getDisplayName());
                        details.put("logoUrl", company.getLogoUrl());
                        return details;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("companies", companyDetails);
            response.put("selectedCompanyId", selectedCompanyIdHolder[0]);

            logger.info("✅ Successfully returned all companies");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error fetching all companies: {}", e.getMessage(), e);
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/select/{id}")
    public ResponseEntity<?> selectCompany(@PathVariable String id, HttpSession session) {
        logger.info("🔄 Selecting company: {}", id);
        try {
            // Get current user from session
            User currentUser = authenticationController.getUserFromSession(session);
            if (currentUser == null) {
                logger.error("❌ User not authenticated when selecting company");
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "User not authenticated"
                ));
            }

            // Check if company exists and user has access
            Optional<Company> companyOpt = companyService.getCompanyById(id);
            if (companyOpt.isEmpty()) {
                logger.error("❌ Company not found: {}", id);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Company not found"
                ));
            }

            // If user is not super admin, verify they have access to this company
            boolean isSuperAdmin = userRoleService.isSuperAdmin(currentUser);
            if (!isSuperAdmin && !companyService.userHasCompanyAccess(currentUser.getId(), id)) {
                logger.error("🔒 User {} does not have access to company {}", currentUser.getId(), id);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "You do not have access to this company"
                ));
            }

            // Update selected company in session
            session.setAttribute("selectedCompanyId", id);
            logger.info("✅ Successfully selected company: {}", id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("❌ Error selecting company: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/logo-upload/{id}")
    public ResponseEntity<?> uploadLogo(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        logger.info("🖼️ Uploading logo for company: {}", id);
        try {
            String logoUrl = fileStorageService.saveFile(file, "company_" + id);
            companyService.updateLogoUrl(id, logoUrl);

            logger.info("✅ Successfully uploaded logo for company: {}", id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("logoUrl", logoUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error uploading logo: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}
