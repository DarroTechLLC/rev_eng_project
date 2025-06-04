package com.darro_tech.revengproject.controllers;

import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.CompanySelectionService;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.util.LoggerUtils;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController extends BaseController {

    private static final Logger logger = LoggerUtils.getLogger(DashboardController.class);

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanySelectionService companySelectionService;

    @Autowired
    private AuthenticationController authenticationController;

    /**
     * Default dashboard route
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        LoggerUtils.logRequest(logger, "GET", "/dashboard");
        logger.debug("📊 Processing default dashboard request");

        // Performance tracking
        long startTime = System.currentTimeMillis();

        try {
            User user = authenticationController.getUserFromSession(session);
            if (user == null) {
                logger.warn("🔒 No authenticated user found, redirecting to login");
                LoggerUtils.logResponse(logger, "/dashboard", "Redirecting to login due to missing authentication");
                return "redirect:/login";
            }

            LoggerUtils.logUserAction(logger, user.getUsername(), "accessed dashboard", null);

            // Get selected company from session
            String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
            if (selectedCompanyId == null) {
                logger.warn("⚠️ No company selected for user: {}", user.getUsername());

                // Try to auto-select a company for the user
                logger.debug("🔄 Attempting to auto-select company for user");
                boolean companySelected = companySelectionService.selectDefaultCompanyForUser(user, session);
                if (companySelected) {
                    // Get the newly selected company ID
                    selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
                    logger.info("✅ Successfully auto-selected company during dashboard access: {}", selectedCompanyId);
                } else {
                    logger.warn("❌ Failed to auto-select company, redirecting to company selection");
                    LoggerUtils.logResponse(logger, "/dashboard", "Redirecting to company selection due to no company available");
                    return "redirect:/select-company";
                }
            } else {
                logger.debug("🏢 Using company from session: {}", selectedCompanyId);
            }

            // Load company-specific data for the dashboard
            logger.debug("📊 Loading dashboard data for company: {}", selectedCompanyId);
            loadDashboardData(model, selectedCompanyId, user);

            // Track performance
            long endTime = System.currentTimeMillis();
            LoggerUtils.logPerformance(logger, "Dashboard page preparation", endTime - startTime);

            LoggerUtils.logResponse(logger, "/dashboard", "Dashboard rendered successfully");
            return view("dashboard", model);
        } catch (Exception e) {
            LoggerUtils.logException(logger, e, "processing dashboard request");
            throw e; // Re-throw to let Spring's exception handlers deal with it
        }
    }

    /**
     * Dashboard with specific type (daily-volume, weekly-report, etc.)
     */
    @GetMapping("/dashboard/{companyId}/{dashboardType}")
    public String companyDashboardPage(
            @PathVariable String companyId,
            @PathVariable String dashboardType,
            Model model,
            HttpSession session) {

        LoggerUtils.logRequest(logger, "GET", "/dashboard/" + companyId + "/" + dashboardType);
        logger.debug("📊 Processing typed dashboard request for company: {}, type: {}", companyId, dashboardType);

        // Performance tracking
        long startTime = System.currentTimeMillis();

        try {
            User user = authenticationController.getUserFromSession(session);
            if (user == null) {
                logger.warn("🔒 No authenticated user found, redirecting to login");
                LoggerUtils.logResponse(logger, "/dashboard/" + companyId + "/" + dashboardType, "Redirecting to login due to missing authentication");
                return "redirect:/login";
            }

            LoggerUtils.logUserAction(logger, user.getUsername(), "accessed dashboard",
                    "company: " + companyId + ", type: " + dashboardType);

            // Find company by ID
            logger.debug("🔍 Looking up company by ID: {}", companyId);
            LoggerUtils.logDatabase(logger, "SELECT", "Company", "Finding company by ID: " + companyId);
            Optional<Company> companyOpt = companyService.getCompanyById(companyId);

            if (!companyOpt.isPresent()) {
                logger.warn("❓ Company not found with ID: {}", companyId);
                LoggerUtils.logResponse(logger, "/dashboard/" + companyId + "/" + dashboardType, "Redirecting to default dashboard due to company not found");
                return "redirect:/dashboard";
            }

            Company company = companyOpt.get();
            logger.debug("✅ Found company: {} (ID: {})", company.getName(), company.getId());

            // Check if user has access to this company
            logger.debug("🔒 Checking if user has access to company: {}", company.getName());
            LoggerUtils.logDatabase(logger, "SELECT", "UserCompany", "Checking user access to company");
            boolean hasAccess = companyService.userHasCompanyAccess(user.getId(), company.getId());

            if (!hasAccess) {
                logger.warn("🚫 User {} does not have access to company: {}", user.getUsername(), company.getName());
                LoggerUtils.logAuthorization(logger, false, user.getUsername(), "company: " + company.getName());
                LoggerUtils.logResponse(logger, "/dashboard/" + companyId + "/" + dashboardType, "Redirecting to default dashboard due to access denied");
                return "redirect:/dashboard";
            }

            // Update the selected company in session
            logger.debug("🔄 Updating selected company in session to: {}", company.getName());
            session.setAttribute("selectedCompanyId", company.getId());

            // Add project type to model
            logger.debug("📝 Setting dashboard type in model: {}", dashboardType);
            model.addAttribute("dashboardType", dashboardType);

            // Load company-specific data
            logger.debug("📊 Loading dashboard data for company: {}", company.getName());
            loadDashboardData(model, company.getId(), user);

            // Redirect to SEO-friendly URL format
            String companySlug = company.getName().toLowerCase().replace(" ", "-");
            logger.debug("🔄 Redirecting to SEO-friendly URL: /{}/dashboard/{}", companySlug, dashboardType);

            // Track performance
            long endTime = System.currentTimeMillis();
            LoggerUtils.logPerformance(logger, "Typed dashboard preparation", endTime - startTime);

            LoggerUtils.logResponse(logger, "/dashboard/" + companyId + "/" + dashboardType,
                    "Redirecting to SEO-friendly dashboard URL");
            return "redirect:/" + companySlug + "/dashboard/" + dashboardType;
        } catch (Exception e) {
            LoggerUtils.logException(logger, e, "processing typed dashboard request");
            logger.error("💥 Error in companyDashboardPage: {} - {}", e.getClass().getName(), e.getMessage());
            return "redirect:/dashboard";
        }
    }

    /**
     * Company-specific dashboard root page
     */
    @GetMapping("/dashboard/{companyId}")
    public String companyDashboardRoot(
            @PathVariable String companyId,
            Model model,
            HttpSession session) {

        LoggerUtils.logRequest(logger, "GET", "/dashboard/" + companyId);
        logger.debug("🔄 Processing company dashboard root request for company: {}", companyId);

        try {
            logger.info("🔄 Redirecting to default dashboard type (daily-volume) for company: {}", companyId);

            // Default to daily-volume when no specific dashboard type is specified
            LoggerUtils.logResponse(logger, "/dashboard/" + companyId,
                    "Redirecting to daily-volume dashboard for company");
            return "redirect:/dashboard/" + companyId + "/daily-volume";
        } catch (Exception e) {
            LoggerUtils.logException(logger, e, "processing company dashboard root request");
            logger.error("💥 Error in companyDashboardRoot: {} - {}", e.getClass().getName(), e.getMessage());
            return "redirect:/dashboard";
        }
    }

    /**
     * Company-specific projects page
     */
    @GetMapping("/projects/{companyId}")
    public String companyProjects(
            @PathVariable String companyId,
            Model model,
            HttpSession session) {

        LoggerUtils.logRequest(logger, "GET", "/projects/" + companyId);
        logger.debug("📋 Processing company projects request for company: {}", companyId);

        // Performance tracking
        long startTime = System.currentTimeMillis();

        try {
            User user = authenticationController.getUserFromSession(session);
            if (user == null) {
                logger.warn("🔒 No authenticated user found, redirecting to login");
                LoggerUtils.logResponse(logger, "/projects/" + companyId, "Redirecting to login due to missing authentication");
                return "redirect:/login";
            }

            LoggerUtils.logUserAction(logger, user.getUsername(), "accessed projects page", "company: " + companyId);

            // Find company by ID
            logger.debug("🔍 Looking up company by ID: {}", companyId);
            LoggerUtils.logDatabase(logger, "SELECT", "Company", "Finding company by ID: " + companyId);
            Optional<Company> companyOpt = companyService.getCompanyById(companyId);

            if (!companyOpt.isPresent()) {
                logger.warn("❓ Company not found with ID: {}", companyId);
                LoggerUtils.logResponse(logger, "/projects/" + companyId, "Redirecting to dashboard due to company not found");
                return "redirect:/dashboard";
            }

            Company company = companyOpt.get();
            logger.debug("✅ Found company: {} (ID: {})", company.getName(), company.getId());

            // Check if user has access to this company
            logger.debug("🔒 Checking if user has access to company: {}", company.getName());
            LoggerUtils.logDatabase(logger, "SELECT", "UserCompany", "Checking user access to company");
            boolean hasAccess = companyService.userHasCompanyAccess(user.getId(), company.getId());

            if (!hasAccess) {
                logger.warn("🚫 User {} does not have access to company: {}", user.getUsername(), company.getName());
                LoggerUtils.logAuthorization(logger, false, user.getUsername(), "company: " + company.getName());
                LoggerUtils.logResponse(logger, "/projects/" + companyId, "Redirecting to dashboard due to access denied");
                return "redirect:/dashboard";
            }

            // Update the selected company in session
            logger.debug("🔄 Updating selected company in session to: {}", company.getName());
            session.setAttribute("selectedCompanyId", company.getId());

            // Load company-specific data
            logger.debug("📊 Loading dashboard data for company: {}", company.getName());
            loadDashboardData(model, company.getId(), user);

            // Track performance
            long endTime = System.currentTimeMillis();
            LoggerUtils.logPerformance(logger, "Projects page preparation", endTime - startTime);

            // Return the projects view
            LoggerUtils.logResponse(logger, "/projects/" + companyId, "Projects page rendered successfully");
            return view("projects/index", model);
        } catch (Exception e) {
            LoggerUtils.logException(logger, e, "processing company projects request");
            logger.error("💥 Error in companyProjects: {} - {}", e.getClass().getName(), e.getMessage());
            return "redirect:/dashboard";
        }
    }

    /**
     * Legacy company-specific dashboard with path parameter Kept for backwards
     * compatibility
     */
    @GetMapping("/dashboard/{companyKey}")
    public String legacyCompanyDashboard(
            @PathVariable String companyKey,
            @RequestParam(required = false) String id,
            Model model,
            HttpSession session) {

        try {
            User user = authenticationController.getUserFromSession(session);
            if (user == null) {
                logger.warn("🔒 No authenticated user found, redirecting to login");
                return "redirect:/login";
            }

            logger.info("🏢 Loading legacy company dashboard. Company: {}, User: {}", companyKey, user.getUsername());

            // If company ID is provided as query parameter, try to select it
            if (id != null && !id.isEmpty()) {
                Optional<Company> companyOpt = companyService.getCompanyById(id);
                if (companyOpt.isPresent()) {
                    // Check if user has access to this company
                    boolean hasAccess = companyService.userHasCompanyAccess(user.getId(), id);
                    if (hasAccess) {
                        // Update the selected company in session
                        session.setAttribute("selectedCompanyId", id);
                        Company company = companyOpt.get();
                        logger.info("✅ Selected company by ID: {} ({})", id, company.getName());

                        // Redirect to new URL format
                        return "redirect:/dashboard/" + id + "/daily-volume";
                    } else {
                        logger.warn("🚫 User does not have access to company: {}", id);
                        return "redirect:/dashboard";
                    }
                } else {
                    logger.warn("❓ Company not found with ID: {}", id);
                    return "redirect:/dashboard";
                }
            } else {
                // Try to find company by name first
                Optional<Company> companyByName = companyService.getCompanyByName(companyKey);
                if (companyByName.isPresent()) {
                    Company company = companyByName.get();
                    logger.info("✅ Found company by name: {}", company.getName());

                    // Redirect to new URL format
                    return "redirect:/dashboard/" + company.getId() + "/daily-volume";
                }

                // Fall back to key lookup
                Optional<Company> companyByKey = companyService.getCompanyByKey(companyKey);
                if (companyByKey.isPresent()) {
                    Company company = companyByKey.get();
                    logger.info("✅ Found company by key: {}", company.getName());

                    // Redirect to new URL format
                    return "redirect:/dashboard/" + company.getId() + "/daily-volume";
                } else {
                    logger.warn("❓ Company not found with name or key: {}", companyKey);
                    return "redirect:/dashboard";
                }
            }
        } catch (Exception e) {
            logger.error("💥 Error in legacyCompanyDashboard: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return "redirect:/dashboard";
        }
    }

    /**
     * Helper method to load company-specific dashboard data
     */
    private void loadDashboardData(Model model, String companyId, User user) {
        try {
            logger.debug("📊 Loading dashboard data for company ID: {}", companyId);

            // Load the company
            LoggerUtils.logDatabase(logger, "SELECT", "Company", "Loading company data for dashboard");
            Optional<Company> companyOpt = companyService.getCompanyById(companyId);

            if (!companyOpt.isPresent()) {
                logger.warn("❓ Company not found when loading dashboard data: {}", companyId);
                return;
            }

            Company company = companyOpt.get();
            model.addAttribute("companyName", company.getName());
            model.addAttribute("currentCompanyId", company.getId());
            model.addAttribute("selectedCompanyId", company.getId());
            model.addAttribute("companyLogoUrl", company.getLogoUrl());
            logger.debug("📊 Added company details to model: {}", company.getName());

            // Load other data here...
        } catch (Exception e) {
            LoggerUtils.logException(logger, e, "loading dashboard data");
            logger.error("💥 Error loading dashboard data: {} - {}", e.getClass().getName(), e.getMessage());
        }
    }
}
