package com.darro_tech.revengproject.controllers;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

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
        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            logger.warn("🔒 No authenticated user found, redirecting to login");
            return "redirect:/login";
        }

        logger.info("📊 Loading dashboard for user: {}", user.getUsername());

        // Get selected company from session
        String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
        if (selectedCompanyId == null) {
            logger.warn("⚠️ No company selected for user: {}", user.getUsername());

            // Try to auto-select a company for the user
            boolean companySelected = companySelectionService.selectDefaultCompanyForUser(user, session);
            if (companySelected) {
                // Get the newly selected company ID
                selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
                logger.info("🔄 Auto-selected company for user during dashboard access");
            } else {
                logger.warn("❌ Failed to auto-select company, redirecting to company selection");
                return "redirect:/select-company";
            }
        }

        // Load company-specific data for the dashboard
        loadDashboardData(model, selectedCompanyId, user);

        return view("dashboard", model);
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

        try {
            User user = authenticationController.getUserFromSession(session);
            if (user == null) {
                logger.warn("🔒 No authenticated user found, redirecting to login");
                return "redirect:/login";
            }

            logger.info("🏢 Loading dashboard page: {}/{}, User: {}",
                    companyId, dashboardType, user.getUsername());

            // Find company by ID
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
            logger.info("✅ Selected company for dashboard: {} (ID: {})", company.getName(), company.getId());

            // Add project type to model
            model.addAttribute("dashboardType", dashboardType);

            // Load company-specific data
            loadDashboardData(model, company.getId(), user);

            // Redirect to SEO-friendly URL format
            String companySlug = company.getName().toLowerCase().replace(" ", "-");
            return "redirect:/" + companySlug + "/dashboard/" + dashboardType;
        } catch (Exception e) {
            logger.error("💥 Error in companyDashboardPage: {} - {}", e.getClass().getName(), e.getMessage(), e);
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

        try {
            logger.info("🔄 Redirecting company dashboard to daily-volume: {}", companyId);

            // Default to daily-volume when no specific dashboard type is specified
            return "redirect:/dashboard/" + companyId + "/daily-volume";
        } catch (Exception e) {
            logger.error("💥 Error in companyDashboardRoot: {} - {}", e.getClass().getName(), e.getMessage(), e);
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

        try {
            User user = authenticationController.getUserFromSession(session);
            if (user == null) {
                logger.warn("🔒 No authenticated user found, redirecting to login");
                return "redirect:/login";
            }

            logger.info("🏢 Loading company projects page: {}, User: {}", companyId, user.getUsername());

            // Find company by ID
            logger.debug("🔍 Searching for company by ID: {}", companyId);
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

            // Load company-specific data
            loadDashboardData(model, company.getId(), user);

            // Return the projects view
            return view("projects/index", model);
        } catch (Exception e) {
            logger.error("💥 Error in companyProjects: {} - {}", e.getClass().getName(), e.getMessage(), e);
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
     * Primary SEO-friendly URL format for dashboard pages
     */
    @GetMapping("/{companyName}/dashboard/{dashboardType}")
    public String seoFriendlyDashboard(
            @PathVariable String companyName,
            @PathVariable String dashboardType,
            Model model,
            HttpSession session) {

        try {
            User user = authenticationController.getUserFromSession(session);
            if (user == null) {
                logger.warn("🔒 No authenticated user found, redirecting to login");
                return "redirect:/login";
            }

            // Decode the company name to handle spaces and special characters
            String decodedCompanyName = URLDecoder.decode(companyName, StandardCharsets.UTF_8);

            logger.info("🏢 Loading SEO-friendly dashboard: {}/{}, User: {}",
                    decodedCompanyName, dashboardType, user.getUsername());

            // Try to find company by name
            Optional<Company> companyOpt = companyService.getCompanyByName(decodedCompanyName);

            if (!companyOpt.isPresent()) {
                // Fall back to key lookup
                companyOpt = companyService.getCompanyByKey(decodedCompanyName.replace("-", " "));
                
                if (!companyOpt.isPresent()) {
                    // Last attempt - try with slug conversion
                    List<Company> allCompanies = companyService.getAllCompanies();
                    for (Company company : allCompanies) {
                        String companySlug = company.getName().toLowerCase().replace(" ", "-");
                        if (companySlug.equals(decodedCompanyName.toLowerCase())) {
                            companyOpt = Optional.of(company);
                            break;
                        }
                    }
                    
                    if (!companyOpt.isPresent()) {
                        logger.warn("❓ Company not found with name: {}", decodedCompanyName);
                        return "redirect:/dashboard";
                    }
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

            // Update the selected company in session
            session.setAttribute("selectedCompanyId", company.getId());
            logger.info("✅ Selected company for dashboard: {} (ID: {})", company.getName(), company.getId());

            // Add dashboard type to model
            model.addAttribute("dashboardType", dashboardType);

            // Load company-specific data
            loadDashboardData(model, company.getId(), user);

            // Return appropriate view based on dashboard type
            return view("dashboard/" + dashboardType, model);
        } catch (Exception e) {
            logger.error("💥 Error in seoFriendlyDashboard: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return "redirect:/dashboard";
        }
    }

    /**
     * Handle old style URL routing for projects - redirect to the new URL
     * format
     */
    @GetMapping("/{companyName}/projects")
    public String legacyCompanyProjects(
            @PathVariable String companyName,
            Model model,
            HttpSession session) {

        try {
            // Decode the company name to handle spaces and special characters
            String decodedCompanyName = URLDecoder.decode(companyName, StandardCharsets.UTF_8);
            logger.info("🔄 Redirecting legacy URL format: /{}/projects", decodedCompanyName);

            // Try to find company by name
            Optional<Company> companyOpt = companyService.getCompanyByName(decodedCompanyName);

            if (!companyOpt.isPresent()) {
                // Fall back to key lookup
                companyOpt = companyService.getCompanyByKey(decodedCompanyName);
            }

            if (companyOpt.isPresent()) {
                Company company = companyOpt.get();
                // Redirect to new URL format
                return "redirect:/projects/" + company.getId();
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
     * Helper method to load company-specific dashboard data
     */
    private void loadDashboardData(Model model, String companyId, User user) {
        logger.info("🔄 Loading dashboard data for company ID: {}", companyId);

        // Add company ID to model for debugging
        model.addAttribute("currentCompanyId", companyId);

        // Get company details
        companyService.getCompanyById(companyId).ifPresent(company -> {
            model.addAttribute("companyName", company.getName());
            model.addAttribute("companyLogoUrl", company.getLogoUrl());
            model.addAttribute("selectedCompany", company);
            logger.info("🏢 Dashboard loaded for company: {}, User: {}",
                    company.getName(), (user != null ? user.getUsername() : "unknown"));
            // Add any additional company data here
        });

        // Add user to model
        if (user != null) {
            model.addAttribute("currentUser", user);
        }

        // Add any other dashboard data here
        model.addAttribute("dashboardTitle", "Company Dashboard");
        model.addAttribute("lastUpdated", java.time.LocalDateTime.now().toString());
    }
}
