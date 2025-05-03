package com.darro_tech.revengproject.controllers;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.CompanyService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProjectsController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectsController.class);

    @Autowired
    private CompanyService companyService;

    @Autowired
    private AuthenticationController authenticationController;

    /**
     * Project-specific routes with farm key New URL format:
     * /projects/{companyId}/{farmKey}/{projectType}
     */
    @GetMapping("/projects/{companyId}/{farmKey}/{projectType}")
    public String projectsPage(
            @PathVariable String companyId,
            @PathVariable String farmKey,
            @PathVariable String projectType,
            Model model,
            HttpSession session) {

        try {
            User user = authenticationController.getUserFromSession(session);
            if (user == null) {
                logger.warn("🔒 No authenticated user found, redirecting to login");
                return "redirect:/login";
            }

            // Decode farm key to handle spaces/special characters
            String decodedFarmKey = URLDecoder.decode(farmKey, StandardCharsets.UTF_8);

            logger.info("🏢 Loading project page: {}/{}/{}, User: {}",
                    companyId, decodedFarmKey, projectType, user.getUsername());

            // Find company by ID
            logger.debug("🔍 Searching for company with ID: {}", companyId);
            Optional<Company> companyOpt = companyService.getCompanyById(companyId);

            if (!companyOpt.isPresent()) {
                logger.warn("❓ Company not found with ID: {}", companyId);
                return "redirect:/dashboard";
            }

            Company company = companyOpt.get();
            logger.debug("✅ Found company: {} (ID: {})", company.getName(), company.getId());

            // Check if user has access to this company
            boolean hasAccess = companyService.userHasCompanyAccess(user.getId(), company.getId());
            if (!hasAccess) {
                logger.warn("🚫 User does not have access to company: {}", company.getName());
                return "redirect:/dashboard";
            }

            // Update the selected company in session
            session.setAttribute("selectedCompanyId", company.getId());
            logger.info("✅ Selected company for projects: {} (ID: {})", company.getName(), company.getId());

            // Store the selected farm key in model
            model.addAttribute("selectedFarmKey", decodedFarmKey);
            model.addAttribute("projectType", projectType);

            // Add company data to model
            loadCompanyData(model, company.getId(), user);

            // Return the appropriate view based on project type
            return view("projects/" + projectType, model);
        } catch (Exception e) {
            logger.error("💥 Error in projectsPage: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return "redirect:/dashboard";
        }
    }

    /**
     * Handle legacy URL format and redirect to new format
     */
    @GetMapping("/{companyName}/projects/{farmKey}/{projectType}")
    public String legacyProjectsPage(
            @PathVariable String companyName,
            @PathVariable String farmKey,
            @PathVariable String projectType,
            Model model,
            HttpSession session) {

        try {
            // Decode the company name to handle spaces and special characters
            String decodedCompanyName = URLDecoder.decode(companyName, StandardCharsets.UTF_8);
            String decodedFarmKey = URLDecoder.decode(farmKey, StandardCharsets.UTF_8);

            logger.info("🔄 Redirecting legacy URL format: /{}/projects/{}/{}",
                    decodedCompanyName, decodedFarmKey, projectType);

            // Try to find company by name
            Optional<Company> companyOpt = companyService.getCompanyByName(decodedCompanyName);

            if (!companyOpt.isPresent()) {
                // Fall back to key lookup
                companyOpt = companyService.getCompanyByKey(decodedCompanyName);
            }

            if (companyOpt.isPresent()) {
                Company company = companyOpt.get();
                // Redirect to new URL format
                return "redirect:/projects/" + company.getId() + "/" + decodedFarmKey + "/" + projectType;
            } else {
                logger.warn("❓ Company not found with name: {}", decodedCompanyName);
                return "redirect:/dashboard";
            }
        } catch (Exception e) {
            logger.error("💥 Error redirecting legacy URL: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return "redirect:/dashboard";
        }
    }

    /**
     * Helper method to load company data
     */
    private void loadCompanyData(Model model, String companyId, User user) {
        // Get company by ID
        Optional<Company> companyOpt = companyService.getCompanyById(companyId);
        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();
            model.addAttribute("selectedCompany", company);
            logger.info("📊 Loaded company data for: {}", company.getName());
        } else {
            logger.warn("⚠️ Company not found with ID: {}", companyId);
        }

        // Add user to model
        model.addAttribute("currentUser", user);
    }
}
