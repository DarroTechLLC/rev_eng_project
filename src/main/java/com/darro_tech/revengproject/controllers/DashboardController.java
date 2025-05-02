package com.darro_tech.revengproject.controllers;

import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.CompanySelectionService;
import com.darro_tech.revengproject.services.CompanyService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/dashboard")
public class DashboardController extends BaseController {

    private static final Logger logger = Logger.getLogger(DashboardController.class.getName());

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanySelectionService companySelectionService;

    @Autowired
    private AuthenticationController authenticationController;

    /**
     * Default dashboard route
     */
    @GetMapping
    public String dashboard(Model model, HttpSession session) {
        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            logger.warning("🔒 No authenticated user found, redirecting to login");
            return "redirect:/login";
        }

        logger.info("📊 Loading dashboard for user: " + user.getUsername());

        // Get selected company from session
        String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
        if (selectedCompanyId == null) {
            logger.warning("⚠️ No company selected for user: " + user.getUsername());

            // Try to auto-select a company for the user
            boolean companySelected = companySelectionService.selectDefaultCompanyForUser(user, session);
            if (companySelected) {
                // Get the newly selected company ID
                selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
                logger.info("🔄 Auto-selected company for user during dashboard access");
            } else {
                logger.warning("❌ Failed to auto-select company, redirecting to company selection");
                return "redirect:/select-company";
            }
        }

        // Load company-specific data for the dashboard
        loadDashboardData(model, selectedCompanyId, user);

        return view("dashboard", model);
    }

    /**
     * Company-specific dashboard with path parameter
     */
    @GetMapping("/{companyKey}")
    public String companyDashboard(
            @PathVariable String companyKey,
            @RequestParam(required = false) String id,
            Model model,
            HttpSession session) {

        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            logger.warning("🔒 No authenticated user found, redirecting to login");
            return "redirect:/login";
        }

        logger.info("🏢 Loading company-specific dashboard. Company key: " + companyKey + ", User: " + user.getUsername());

        // If company ID is provided as query parameter, try to select it
        if (id != null && !id.isEmpty()) {
            Optional<Company> companyOpt = companyService.getCompanyById(id);
            if (companyOpt.isPresent()) {
                // Check if user has access to this company
                boolean hasAccess = companyService.userHasCompanyAccess(user.getId(), id);
                if (hasAccess) {
                    // Update the selected company in session
                    session.setAttribute("selectedCompanyId", id);
                    logger.info("✅ Selected company by ID: " + id + " (" + companyOpt.get().getName() + ")");
                } else {
                    logger.warning("🚫 User does not have access to company: " + id);
                    return "redirect:/dashboard";
                }
            } else {
                logger.warning("❓ Company not found with ID: " + id);
                return "redirect:/dashboard";
            }
        } else {
            // Try to find company by key (slug)
            Optional<Company> companyByKey = companyService.getCompanyByKey(companyKey);
            if (companyByKey.isPresent()) {
                Company company = companyByKey.get();
                // Check if user has access to this company
                boolean hasAccess = companyService.userHasCompanyAccess(user.getId(), company.getId());
                if (hasAccess) {
                    // Update the selected company in session
                    session.setAttribute("selectedCompanyId", company.getId());
                    logger.info("✅ Selected company by key: " + companyKey + " (" + company.getName() + ")");
                } else {
                    logger.warning("🚫 User does not have access to company: " + companyKey);
                    return "redirect:/dashboard";
                }
            } else {
                logger.warning("❓ Company not found with key: " + companyKey);
                return "redirect:/dashboard";
            }
        }

        // Get the current selected company ID from session (after potential updates above)
        String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
        if (selectedCompanyId == null) {
            logger.warning("⚠️ No company selected after processing, redirecting to dashboard");
            return "redirect:/dashboard";
        }

        // Load company-specific data for the dashboard
        loadDashboardData(model, selectedCompanyId, user);

        return view("dashboard", model);
    }

    /**
     * Helper method to load company-specific dashboard data
     */
    private void loadDashboardData(Model model, String companyId, User user) {
        logger.info("🔄 Loading dashboard data for company ID: " + companyId);

        // Add company ID to model for debugging
        model.addAttribute("currentCompanyId", companyId);

        // Get company details
        companyService.getCompanyById(companyId).ifPresent(company -> {
            model.addAttribute("companyName", company.getName());
            model.addAttribute("companyLogoUrl", company.getLogoUrl());
            logger.info("🏢 Dashboard loaded for company: " + company.getName() + ", User: "
                    + (user != null ? user.getUsername() : "unknown"));
            // Add any additional company data here
            // TODO: Add more specific company data
        });

        // Add any other dashboard data here
        model.addAttribute("dashboardTitle", "Company Dashboard");
        model.addAttribute("lastUpdated", java.time.LocalDateTime.now().toString());
    }
}
