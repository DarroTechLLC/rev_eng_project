package com.darro_tech.revengproject.controllers;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.FarmService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProjectsController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectsController.class);

    @Autowired
    private CompanyService companyService;

    @Autowired
    private FarmService farmService;

    @Autowired
    private AuthenticationController authenticationController;

    /**
     * Legacy route for UUID-based URL format - now redirects to SEO-friendly
     * version Example: /projects/{companyId}/{farmKey}/{projectType}
     */
    @GetMapping("/projects/{companyId}/{farmKey}/{projectType}")
    public String legacyProjectsPage(
            @PathVariable String companyId,
            @PathVariable String farmKey,
            @PathVariable String projectType,
            HttpSession session) {

        try {
            // Find company by ID
            Optional<Company> companyOpt = companyService.getCompanyById(companyId);
            if (!companyOpt.isPresent()) {
                logger.warn("❓ Company not found with ID: {}", companyId);
                return "redirect:/dashboard";
            }

            Company company = companyOpt.get();

            // Find farm by ID
            Optional<Farm> farmOpt = farmService.getFarmById(farmKey);
            if (!farmOpt.isPresent()) {
                logger.warn("❓ Farm not found with ID: {}", farmKey);
                return "redirect:/dashboard";
            }

            Farm farm = farmOpt.get();

            // Update session - needed for redirect continuity
            session.setAttribute("selectedCompanyId", company.getId());
            session.setAttribute("selectedFarmKey", farm.getId());

            logger.info("🔄 Redirecting legacy URL to SEO-friendly format: /{}/projects/{}/{}",
                    company.getName(), farm.getName(), projectType);

            // Construct the SEO-friendly URL - with proper encoding
            String encodedCompanyName = company.getName().toLowerCase().replace(" ", "-");
            String encodedFarmName = farm.getName().toLowerCase().replace(" ", "-");

            // Redirect to new URL format
            return "redirect:/" + encodedCompanyName + "/projects/" + encodedFarmName + "/" + projectType;
        } catch (Exception e) {
            logger.error("💥 Error in legacyProjectsPage: {} - {}", e.getClass().getName(), e.getMessage(), e);
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
