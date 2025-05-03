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
import com.darro_tech.revengproject.services.UserRoleService;

import jakarta.servlet.http.HttpSession;

/**
 * Centralized controller for all dynamic company and farm routing Eliminates
 * ambiguous mapping by creating a clear hierarchy
 */
@Controller
public class RoutingController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(RoutingController.class);

    @Autowired
    private CompanyService companyService;

    @Autowired
    private FarmService farmService;

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private DashboardController dashboardController;

    @Autowired
    private UserRoleService userRoleService;

    /**
     * Dashboard main entry - redirects to default dashboard view IMPORTANT:
     * Root ("/") mapping is now handled by HomeController only
     */
    @GetMapping("/dashboard-entry")
    public String dashboardEntry(HttpSession session) {
        logger.info("🚪 Dashboard entry point accessed, redirecting to dashboard");
        return "redirect:/dashboard";
    }

    /**
     * Company dashboard with specific dashboard type Format:
     * /{companyName}/dashboard/{dashboardType}
     */
    @GetMapping("/{companyName}/dashboard/{dashboardType}")
    public String companyDashboard(
            @PathVariable String companyName,
            @PathVariable String dashboardType,
            Model model,
            HttpSession session) {

        logger.info("🌟 Routing: Company dashboard route accessed - /{}/{}", companyName, dashboardType);

        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            logger.warn("🔒 No authenticated user found, redirecting to login");
            return "redirect:/login";
        }

        // Decode company name
        String decodedCompanyName = URLDecoder.decode(companyName, StandardCharsets.UTF_8);
        logger.info("📊 Loading dashboard: {}/dashboard/{}, User: {}",
                decodedCompanyName, dashboardType, user.getUsername());

        // Find company by name or key
        Optional<Company> companyOpt = companyService.getCompanyByName(decodedCompanyName);
        if (!companyOpt.isPresent()) {
            logger.debug("🔍 Company not found by name, trying by key: {}", decodedCompanyName);
            companyOpt = companyService.getCompanyByKey(decodedCompanyName);
            if (!companyOpt.isPresent()) {
                logger.warn("❓ Company not found: {}", decodedCompanyName);
                return "redirect:/dashboard";
            }
        }

        Company company = companyOpt.get();
        logger.debug("🏢 Found company: {} (ID: {})", company.getName(), company.getId());

        // Check if user has access to this company
        boolean hasAccess = companyService.userHasCompanyAccess(user.getId(), company.getId());
        if (!hasAccess) {
            logger.warn("🚫 User {} doesn't have access to company: {}", user.getUsername(), company.getName());
            return "redirect:/dashboard";
        }
        logger.debug("✅ User has access to company: {}", company.getName());

        // Update selected company in session
        session.setAttribute("selectedCompanyId", company.getId());
        logger.debug("💾 Updated session with company ID: {}", company.getId());

        // Set dashboard type in model
        model.addAttribute("dashboardType", dashboardType);
        model.addAttribute("selectedCompany", company);
        model.addAttribute("currentUser", user);
        logger.debug("🧩 Added company and dashboard type to model");

        // Load other dashboard data
        loadCommonData(model, company.getId(), user);

        // Return appropriate view based on dashboard type
        logger.info("🔄 Returning view: dashboard/{}", dashboardType);
        return view("dashboard/" + dashboardType, model);
    }

    /**
     * Company dashboard root - redirects to daily-volume Format:
     * /{companyName}/dashboard
     */
    @GetMapping("/{companyName}/dashboard")
    public String companyDashboardRoot(@PathVariable String companyName) {
        logger.info("🔄 Routing: Company dashboard root - redirecting to daily-volume: {}", companyName);
        return "redirect:/" + companyName + "/dashboard/daily-volume";
    }

    /**
     * MTD Volume page Format: /{companyName}/mtd-volume
     */
    @GetMapping("/{companyName}/mtd-volume")
    public String mtdVolume(
            @PathVariable String companyName,
            Model model,
            HttpSession session) {

        logger.info("📈 Routing: MTD Volume route accessed - /{}/mtd-volume", companyName);
        return handleDirectDashboardAccess(companyName, "mtd-volume", model, session);
    }

    /**
     * YTD Volume page Format: /{companyName}/ytd-volume
     */
    @GetMapping("/{companyName}/ytd-volume")
    public String ytdVolume(
            @PathVariable String companyName,
            Model model,
            HttpSession session) {

        logger.info("📊 Routing: YTD Volume route accessed - /{}/ytd-volume", companyName);
        return handleDirectDashboardAccess(companyName, "ytd-volume", model, session);
    }

    /**
     * Production Headcount page Format: /{companyName}/production-headcount
     */
    @GetMapping("/{companyName}/production-headcount")
    public String productionHeadcount(
            @PathVariable String companyName,
            Model model,
            HttpSession session) {

        logger.info("👥 Routing: Production Headcount route accessed - /{}/production-headcount", companyName);
        return handleDirectDashboardAccess(companyName, "production-headcount", model, session);
    }

    /**
     * Animal Headcount page Format: /{companyName}/animal-headcount
     */
    @GetMapping("/{companyName}/animal-headcount")
    public String animalHeadcount(
            @PathVariable String companyName,
            Model model,
            HttpSession session) {

        logger.info("🐄 Routing: Animal Headcount route accessed - /{}/animal-headcount", companyName);
        return handleDirectDashboardAccess(companyName, "animal-headcount", model, session);
    }

    /**
     * Weekly Report page Format: /{companyName}/weekly-report
     */
    @GetMapping("/{companyName}/weekly-report")
    public String weeklyReport(
            @PathVariable String companyName,
            Model model,
            HttpSession session) {

        logger.info("📅 Routing: Weekly Report route accessed - /{}/weekly-report", companyName);
        return handleDirectDashboardAccess(companyName, "weekly-report", model, session);
    }

    /**
     * Daily Report page Format: /{companyName}/daily-report
     */
    @GetMapping("/{companyName}/daily-report")
    public String dailyReport(
            @PathVariable String companyName,
            Model model,
            HttpSession session) {

        logger.info("📆 Routing: Daily Report route accessed - /{}/daily-report", companyName);
        return handleDirectDashboardAccess(companyName, "daily-report", model, session);
    }

    /**
     * Production Detail page Format: /{companyName}/production-detail
     */
    @GetMapping("/{companyName}/production-detail")
    public String productionDetail(
            @PathVariable String companyName,
            Model model,
            HttpSession session) {

        logger.info("🔍 Routing: Production Detail route accessed - /{}/production-detail", companyName);
        return handleDirectDashboardAccess(companyName, "production-detail", model, session);
    }

    /**
     * Market Data page Format: /{companyName}/market-data
     */
    @GetMapping("/{companyName}/market-data")
    public String marketData(
            @PathVariable String companyName,
            Model model,
            HttpSession session) {

        logger.info("📉 Routing: Market Data route accessed - /{}/market-data", companyName);
        return handleDirectDashboardAccess(companyName, "market-data", model, session);
    }

    /**
     * Company projects root page - shows list of farms Format:
     * /{companyName}/projects
     */
    @GetMapping("/{companyName}/projects")
    public String companyProjects(
            @PathVariable String companyName,
            Model model,
            HttpSession session) {

        logger.info("🏗️ Routing: Company Projects route accessed - /{}/projects", companyName);

        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            logger.warn("🔒 No authenticated user found, redirecting to login");
            return "redirect:/login";
        }

        // Decode company name
        String decodedCompanyName = URLDecoder.decode(companyName, StandardCharsets.UTF_8);
        logger.info("🏢 Loading projects for company: {}, User: {}",
                decodedCompanyName, user.getUsername());

        // Find company by name or key
        Optional<Company> companyOpt = companyService.getCompanyByName(decodedCompanyName);
        if (!companyOpt.isPresent()) {
            logger.debug("🔍 Company not found by name, trying by key: {}", decodedCompanyName);
            companyOpt = companyService.getCompanyByKey(decodedCompanyName);
            if (!companyOpt.isPresent()) {
                logger.warn("❓ Company not found: {}", decodedCompanyName);
                return "redirect:/dashboard";
            }
        }

        Company company = companyOpt.get();
        logger.debug("🏢 Found company: {} (ID: {})", company.getName(), company.getId());

        // Check if user has access to this company
        boolean hasAccess = companyService.userHasCompanyAccess(user.getId(), company.getId());
        if (!hasAccess) {
            logger.warn("🚫 User {} doesn't have access to company: {}", user.getUsername(), company.getName());
            return "redirect:/dashboard";
        }
        logger.debug("✅ User has access to company: {}", company.getName());

        // Update selected company in session
        session.setAttribute("selectedCompanyId", company.getId());
        logger.debug("💾 Updated session with company ID: {}", company.getId());

        model.addAttribute("selectedCompany", company);
        model.addAttribute("currentUser", user);

        // Load other data
        loadCommonData(model, company.getId(), user);

        logger.info("🔄 Returning projects index view");
        return view("projects/index", model);
    }

    /**
     * Farm-specific project page Format:
     * /{companyName}/projects/{farmName}/{projectType}
     */
    @GetMapping("/{companyName}/projects/{farmName}/{projectType}")
    public String farmProject(
            @PathVariable String companyName,
            @PathVariable String farmName,
            @PathVariable String projectType,
            Model model,
            HttpSession session) {

        logger.info("🚜 Routing: Farm Project route accessed - /{}/projects/{}/{}", companyName, farmName, projectType);

        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            logger.warn("🔒 No authenticated user found, redirecting to login");
            return "redirect:/login";
        }

        // Decode parameters
        String decodedCompanyName = URLDecoder.decode(companyName, StandardCharsets.UTF_8);
        String decodedFarmName = URLDecoder.decode(farmName, StandardCharsets.UTF_8);

        logger.info("🏢 Loading project: {}/projects/{}/{}, User: {}",
                decodedCompanyName, decodedFarmName, projectType, user.getUsername());

        // Find company
        Optional<Company> companyOpt = companyService.getCompanyByName(decodedCompanyName);
        if (!companyOpt.isPresent()) {
            logger.debug("🔍 Company not found by name, trying by key: {}", decodedCompanyName);
            companyOpt = companyService.getCompanyByKey(decodedCompanyName);
            if (!companyOpt.isPresent()) {
                logger.warn("❓ Company not found: {}", decodedCompanyName);
                return "redirect:/dashboard";
            }
        }
        Company company = companyOpt.get();
        logger.debug("🏢 Found company: {} (ID: {})", company.getName(), company.getId());

        // Check access
        boolean hasAccess = companyService.userHasCompanyAccess(user.getId(), company.getId());
        if (!hasAccess) {
            logger.warn("🚫 User {} doesn't have access to company: {}", user.getUsername(), company.getName());
            return "redirect:/dashboard";
        }
        logger.debug("✅ User has access to company: {}", company.getName());

        // Find farm
        Optional<Farm> farmOpt = farmService.getFarmByName(decodedFarmName, company.getId());
        if (!farmOpt.isPresent()) {
            // Try by ID as fallback
            logger.debug("🔍 Farm not found by name, trying by ID: {}", decodedFarmName);
            farmOpt = farmService.getFarmById(decodedFarmName);
            if (!farmOpt.isPresent()) {
                logger.warn("❓ Farm not found: {} in company: {}", decodedFarmName, company.getName());
                return "redirect:/" + companyName + "/projects";
            }
        }
        Farm farm = farmOpt.get();
        logger.debug("🚜 Found farm: {} (ID: {})", farm.getName(), farm.getId());

        // Update session
        session.setAttribute("selectedCompanyId", company.getId());
        session.setAttribute("selectedFarmKey", farm.getId());
        logger.debug("💾 Updated session with company ID: {} and farm ID: {}", company.getId(), farm.getId());

        // Update model
        model.addAttribute("selectedCompany", company);
        model.addAttribute("selectedFarm", farm);
        model.addAttribute("projectType", projectType);
        model.addAttribute("currentUser", user);
        logger.debug("🧩 Added company, farm and project type to model");

        // Load other data
        loadCommonData(model, company.getId(), user);

        // Validate project type
        if (!isValidProjectType(projectType)) {
            logger.warn("⚠️ Invalid project type: {}, redirecting to production", projectType);
            return "redirect:/" + companyName + "/projects/" + farmName + "/production";
        }

        logger.info("🔄 Returning project view: projects/{}", projectType);
        return view("projects/" + projectType, model);
    }

    /**
     * Helper method to validate project type
     */
    private boolean isValidProjectType(String projectType) {
        boolean isValid = projectType.equals("production")
                || projectType.equals("livestock")
                || projectType.equals("digesters")
                || projectType.equals("metrics");

        if (isValid) {
            logger.debug("✅ Valid project type: {}", projectType);
        } else {
            logger.warn("❌ Invalid project type: {}", projectType);
        }

        return isValid;
    }

    /**
     * Common helper for direct dashboard access endpoints
     */
    private String handleDirectDashboardAccess(
            String companyName,
            String dashboardType,
            Model model,
            HttpSession session) {

        logger.debug("🔄 Handling direct dashboard access: {}/{}", companyName, dashboardType);

        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            logger.warn("🔒 No authenticated user found, redirecting to login");
            return "redirect:/login";
        }

        // Decode company name
        String decodedCompanyName = URLDecoder.decode(companyName, StandardCharsets.UTF_8);
        logger.info("📊 Direct access to dashboard: {}/{}, User: {}",
                decodedCompanyName, dashboardType, user.getUsername());

        // Find company
        Optional<Company> companyOpt = companyService.getCompanyByName(decodedCompanyName);
        if (!companyOpt.isPresent()) {
            logger.debug("🔍 Company not found by name, trying by key: {}", decodedCompanyName);
            companyOpt = companyService.getCompanyByKey(decodedCompanyName);
            if (!companyOpt.isPresent()) {
                logger.warn("❓ Company not found: {}", decodedCompanyName);
                return "redirect:/dashboard";
            }
        }
        Company company = companyOpt.get();
        logger.debug("🏢 Found company: {} (ID: {})", company.getName(), company.getId());

        // Check access
        boolean hasAccess = companyService.userHasCompanyAccess(user.getId(), company.getId());
        if (!hasAccess) {
            logger.warn("🚫 User {} doesn't have access to company: {}", user.getUsername(), company.getName());
            return "redirect:/dashboard";
        }
        logger.debug("✅ User has access to company: {}", company.getName());

        // Update session
        session.setAttribute("selectedCompanyId", company.getId());
        logger.debug("💾 Updated session with company ID: {}", company.getId());

        // Update model
        model.addAttribute("selectedCompany", company);
        model.addAttribute("dashboardType", dashboardType);
        model.addAttribute("currentUser", user);
        logger.debug("🧩 Added company and dashboard type to model");

        // Load common data
        loadCommonData(model, company.getId(), user);

        logger.info("🔄 Returning dashboard view: dashboard/{}", dashboardType);
        return view("dashboard/" + dashboardType, model);
    }

    /**
     * Helper method to load common data for views
     */
    private void loadCommonData(Model model, String companyId, User user) {
        logger.debug("📥 Loading common data for company ID: {}", companyId);

        // Add company data
        Optional<Company> companyOpt = companyService.getCompanyById(companyId);
        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();
            model.addAttribute("selectedCompany", company);
            logger.debug("✅ Loaded company data: {}", company.getName());

            // Load farms for company
            model.addAttribute("farms", farmService.getFarmsByCompanyId(companyId));
            logger.debug("🚜 Loaded farms for company: {}", company.getName());
        }

        // Add available companies for user
        boolean isSuperAdmin = userRoleService.isSuperAdmin(user);
        model.addAttribute("availableCompanies", companyService.getUserCompanies(user.getId(), isSuperAdmin));
        logger.debug("🏢 Loaded available companies for user: {} (isSuperAdmin: {})", user.getUsername(), isSuperAdmin);

        // Add user data
        model.addAttribute("currentUser", user);
        logger.debug("👤 Added current user to model: {}", user.getUsername());
    }
}
