package com.darro_tech.revengproject.controllers;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.dto.CompanyDTO;
import com.darro_tech.revengproject.services.CompanySelectionService;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.UserRoleService;

import jakarta.servlet.http.HttpSession;

/**
 * Controller for handling company selection
 */
@Controller
@RequestMapping("/select-company")
public class SelectCompanyController extends BaseController {

    private static final Logger logger = Logger.getLogger(SelectCompanyController.class.getName());

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private CompanySelectionService companySelectionService;

    /**
     * Show the company selection page
     */
    @GetMapping
    public String selectCompany(Model model, HttpSession session) {
        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            logger.warning("🔒 User not authenticated, redirecting to login");
            return "redirect:/login";
        }

        logger.info("🏢 Loading company selection page for user: " + user.getUsername());

        // Get companies the user has access to
        boolean isSuperAdmin = userRoleService.isSuperAdmin(user);
        List<CompanyDTO> userCompanies = companyService.getUserCompanies(user.getId(), isSuperAdmin);

        if (userCompanies.isEmpty()) {
            logger.warning("⚠️ User has no companies available: " + user.getUsername());
            model.addAttribute("error", "You don't have access to any companies.");
            return view("select-company", model);
        }

        // If there's only one company, auto-select it
        if (userCompanies.size() == 1) {
            CompanyDTO company = userCompanies.get(0);
            boolean selected = companySelectionService.selectCompanyForUser(user, company.getCompanyId(), session);
            if (selected) {
                logger.info("✅ Auto-selected the only available company: " + company.getCompanyName());
                return "redirect:/dashboard";
            }
        }

        // Add companies to model for selection
        model.addAttribute("companies", userCompanies);
        model.addAttribute("title", "Select Company");
        logger.info("📋 Showing company selection options. Found " + userCompanies.size() + " companies for user: " + user.getUsername());

        return view("select-company", model);
    }

    /**
     * Handle company selection form submission
     */
    @PostMapping
    public String selectCompanySubmit(@RequestParam("companyId") String companyId, HttpSession session, Model model) {
        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            logger.warning("🔒 User not authenticated, redirecting to login");
            return "redirect:/login";
        }

        logger.info("🔄 Processing company selection request. User: " + user.getUsername() + ", Company ID: " + companyId);

        // Use the company selection service to set the selected company
        boolean selected = companySelectionService.selectCompanyForUser(user, companyId, session);

        if (!selected) {
            logger.warning("❌ Failed to select company: " + companyId + " for user: " + user.getUsername());
            model.addAttribute("error", "You don't have access to this company.");
            return selectCompany(model, session);
        }

        // Get the company name for URL
        String companyKey = null;
        try {
            companyService.getCompanyById(companyId).ifPresent(company -> {
                String normalizedName = companyService.normalizeCompanyName(company.getName());
                model.addAttribute("companyKey", normalizedName);
                logger.info("🔗 Created company URL key: " + normalizedName + " for company: " + company.getName());
            });
        } catch (Exception e) {
            logger.warning("⚠️ Error getting company name: " + e.getMessage());
        }

        // Redirect to dashboard with the company in the URL
        companyKey = (String) model.getAttribute("companyKey");
        if (companyKey != null && !companyKey.isEmpty()) {
            logger.info("➡️ Redirecting to company-specific dashboard: " + companyKey);
            return "redirect:/dashboard/" + companyKey + "?id=" + companyId;
        } else {
            logger.info("➡️ Redirecting to default dashboard");
            return "redirect:/dashboard";
        }
    }
}
