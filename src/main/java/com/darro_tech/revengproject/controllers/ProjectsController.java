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
     * Primary route for SEO-friendly URL format
     * Example: /{companyName}/projects/{farmName}/{projectType}
     */
    @GetMapping("/{companyName}/projects/{farmName}/{projectType}")
    public String seoFriendlyProjectsPage(
            @PathVariable String companyName,
            @PathVariable String farmName,
            @PathVariable String projectType,
            Model model,
            HttpSession session) {

        try {
            User user = authenticationController.getUserFromSession(session);
            if (user == null) {
                logger.warn("🔒 No authenticated user found, redirecting to login");
                return "redirect:/login";
            }

            // Decode parameters to handle spaces and special characters
            String decodedCompanyName = URLDecoder.decode(companyName, StandardCharsets.UTF_8);
            String decodedFarmName = URLDecoder.decode(farmName, StandardCharsets.UTF_8);

            logger.info("🏢 Loading SEO-friendly project page: {}/projects/{}/{}, User: {}",
                    decodedCompanyName, decodedFarmName, projectType, user.getUsername());

            // Try to find company by name
            Optional<Company> companyOpt = companyService.getCompanyByName(decodedCompanyName);

            if (!companyOpt.isPresent()) {
                // Fall back to key lookup
                companyOpt = companyService.getCompanyByKey(decodedCompanyName);
                
                if (!companyOpt.isPresent()) {
                    logger.warn("❓ Company not found with name: {}", decodedCompanyName);
                    return "redirect:/dashboard";
                }
            }

            Company company = companyOpt.get();
            logger.debug("✅ Found company: {} (ID: {})", company.getName(), company.getId());

            // Check if user has access to this company
            boolean hasAccess = companyService.userHasCompanyAccess(user.getId(), company.getId());
            if (!hasAccess) {
                logger.warn("🚫 User does not have access to company: {}", company.getName());
                return "redirect:/dashboard";
            }

            // Try to find farm by name
            Optional<Farm> farmOpt = farmService.getFarmByName(decodedFarmName, company.getId());
            
            if (!farmOpt.isPresent()) {
                // If not found by name, try by ID (legacy support)
                farmOpt = farmService.getFarmById(decodedFarmName);
                
                if (!farmOpt.isPresent()) {
                    logger.warn("❓ Farm not found with name: {} in company: {}", decodedFarmName, company.getName());
                    return "redirect:/" + companyName + "/projects";
                }
            }
            
            Farm farm = farmOpt.get();
            logger.debug("✅ Found farm: {} (ID: {})", farm.getName(), farm.getId());

            // Update the selected company and farm in session
            session.setAttribute("selectedCompanyId", company.getId());
            session.setAttribute("selectedFarmKey", farm.getId());
            
            logger.info("✅ Selected company/farm for projects: {}:{} (IDs: {}:{})", 
                    company.getName(), farm.getName(), company.getId(), farm.getId());

            // Add project type and farm details to model
            model.addAttribute("projectType", projectType);
            model.addAttribute("selectedFarm", farm);

            // Add company data to model
            loadCompanyData(model, company.getId(), user);

            // Return the appropriate view based on project type
            return view("projects/" + projectType, model);
        } catch (Exception e) {
            logger.error("💥 Error in seoFriendlyProjectsPage: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return "redirect:/dashboard";
        }
    }

    /**
     * Legacy route for UUID-based URL format - now redirects to SEO-friendly version
     * Example: /projects/{companyId}/{farmKey}/{projectType}
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
