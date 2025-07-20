package com.darro_tech.revengproject.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.darro_tech.revengproject.models.Role;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.repositories.UserRepository;
import com.darro_tech.revengproject.services.CompanySelectionService;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.UserRoleService;
import com.darro_tech.revengproject.util.LoggerUtils;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController extends BaseController {

    private static final Logger logger = LoggerUtils.getLogger(HomeController.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private CompanySelectionService companySelectionService;

    @Autowired
    private CompanyService companyService;

    @GetMapping("/")  // Explicitly map to root path
    @Transactional
    public String index(Model model, HttpSession session) {
        LoggerUtils.logRequest(logger, "GET", "/");
        logger.debug("🏠 Redirecting to daily volume dashboard");

        return "redirect:/align/dashboard/daily-volume";
    }

    @GetMapping("/home")  // Keep original functionality under /home
    @Transactional
    public String home(Model model, HttpSession session) {
        LoggerUtils.logRequest(logger, "GET", "/home");
        logger.debug("🏠 Rendering home page");

        // Performance tracking
        long startTime = System.currentTimeMillis();

        try {
            Date today = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("E MM-dd-yyyy");
            logger.debug("📅 Setting current date: {}", dateFormat.format(today));

            // Pass the entire user object to the view
            User user = authenticationController.getUserFromSession(session);

            if (user == null) {
                logger.warn("🔒 No authenticated user in session, redirecting to login");
                LoggerUtils.logResponse(logger, "/", "Redirecting to login due to missing authentication");
                return "redirect:/login";
            }

            LoggerUtils.logUserAction(logger, user.getUsername(), "accessed home page", null);
            model.addAttribute("user", user);
            logger.debug("👤 Added user object to model: {}", user.getUsername());

            // Use BaseController's pattern for getting the whole name
            String wholeName = "User";
            if (user != null) {
                try {
                    String firstName = getFieldValueSafely(user, "firstName", "");
                    String lastName = getFieldValueSafely(user, "lastName", "");
                    String username = getFieldValueSafely(user, "username", "User");

                    wholeName = (firstName + " " + lastName).trim();
                    if (wholeName.isEmpty()) {
                        wholeName = username;
                    }
                    logger.debug("👤 Resolved user's display name: {}", wholeName);
                } catch (Exception e) {
                    // In case of any errors, use a default name
                    LoggerUtils.logException(logger, e, "resolving user's full name");
                    wholeName = "User";
                }
            }
            model.addAttribute("wholeName", wholeName);

            // Check if a company is selected, and try to auto-select one if not
            String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
            if (selectedCompanyId == null) {
                logger.info("🔄 No company selected in session, attempting to auto-select one");
                boolean companySelected = companySelectionService.selectDefaultCompanyForUser(user, session);
                if (companySelected) {
                    selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
                    logger.info("✅ Successfully auto-selected company: {}", selectedCompanyId);
                } else {
                    logger.warn("⚠️ Could not auto-select a company for user: {}", user.getUsername());
                }
            } else {
                logger.debug("🏢 Using company from session: {}", selectedCompanyId);
            }

            // If we have a selected company, add its details to the model
            if (selectedCompanyId != null) {
                companyService.getCompanyById(selectedCompanyId).ifPresent(company -> {
                    model.addAttribute("currentCompanyId", company.getId());
                    model.addAttribute("companyName", company.getName());
                    model.addAttribute("companyLogoUrl", company.getLogoUrl());
                    logger.debug("🏢 Added company details to model: {}", company.getName());
                });
            } else {
                logger.warn("⚠️ No company selected, user may see limited functionality");
            }

            model.addAttribute("date", "Today is: " + dateFormat.format(today));

            // Track performance
            long endTime = System.currentTimeMillis();
            LoggerUtils.logPerformance(logger, "Home page preparation", endTime - startTime);

            LoggerUtils.logResponse(logger, "/home", "Rendered home page successfully");
            return view("content/content-container", model);  // Use view method from BaseController
        } catch (Exception e) {
            LoggerUtils.logException(logger, e, "rendering home page");
            throw e; // Re-throw to let Spring's exception handlers deal with it
        }
    }

    private String getFieldValueSafely(Object obj, String fieldName, String defaultValue) {
        try {
            logger.trace("🔍 Accessing field '{}' via reflection", fieldName);
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            return value != null ? value.toString() : defaultValue;
        } catch (Exception e) {
            logger.debug("⚠️ Could not access field '{}' via reflection: {}", fieldName, e.getMessage());
            return defaultValue;
        }
    }

    @GetMapping("/debug/roles-info")
    @ResponseBody
    public String debugRolesInfo(HttpSession session) {
        LoggerUtils.logRequest(logger, "GET", "/debug/roles-info");
        logger.debug("🔧 Accessing debug roles information endpoint");

        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            logger.warn("🔒 Not logged in, cannot show roles information");
            LoggerUtils.logResponse(logger, "/debug/roles-info", "Not logged in");
            return "Not logged in";
        }

        List<Role> roles = userRoleService.getUserRoles(user);
        boolean isAdmin = userRoleService.isAdmin(user);
        boolean isSuperAdmin = userRoleService.isSuperAdmin(user);

        logger.debug("👤 User {} has {} roles", user.getUsername(), roles.size());
        logger.debug("👤 User {} isAdmin: {}, isSuperAdmin: {}", user.getUsername(), isAdmin, isSuperAdmin);

        StringBuilder sb = new StringBuilder();
        sb.append("User: ").append(getFieldValueSafely(user, "username", "unknown")).append("\n");
        sb.append("Roles: ").append(roles.stream().map(Role::getName).collect(Collectors.joining(", "))).append("\n");
        sb.append("isAdmin: ").append(isAdmin).append("\n");
        sb.append("isSuperAdmin: ").append(isSuperAdmin).append("\n");

        LoggerUtils.logResponse(logger, "/debug/roles-info", "Returned roles information");
        return sb.toString();
    }
}
