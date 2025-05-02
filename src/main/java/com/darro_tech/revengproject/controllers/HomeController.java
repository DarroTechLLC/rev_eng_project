package com.darro_tech.revengproject.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController extends BaseController {

    private static final Logger logger = Logger.getLogger(HomeController.class.getName());

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
        logger.info("🏠 Loading home page");

        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("E MM-dd-yyyy");

        // Pass the entire user object to the view
        User user = authenticationController.getUserFromSession(session);

        if (user == null) {
            logger.warning("🔒 No authenticated user, redirecting to login");
            return "redirect:/login";
        }

        logger.info("👤 User: " + user.getUsername() + " accessing home page");
        model.addAttribute("user", user);

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
            } catch (Exception e) {
                // In case of any errors, use a default name
                wholeName = "User";
            }
        }
        model.addAttribute("wholeName", wholeName);

        // Check if a company is selected, and try to auto-select one if not
        String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
        if (selectedCompanyId == null) {
            logger.info("🔄 No company selected, attempting to auto-select one");
            boolean companySelected = companySelectionService.selectDefaultCompanyForUser(user, session);
            if (companySelected) {
                selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
                logger.info("✅ Auto-selected company on homepage access");
            } else {
                logger.warning("⚠️ Could not auto-select a company");
            }
        }

        // If we have a selected company, add its details to the model
        if (selectedCompanyId != null) {
            companyService.getCompanyById(selectedCompanyId).ifPresent(company -> {
                model.addAttribute("currentCompanyId", company.getId());
                model.addAttribute("companyName", company.getName());
                model.addAttribute("companyLogoUrl", company.getLogoUrl());
                logger.info("🏢 Loaded home page with company: " + company.getName());
            });
        }

        model.addAttribute("date", "Today is: " + dateFormat.format(today));
        return view("dashboard", model);  // Use view method from BaseController
    }

    private String getFieldValueSafely(Object obj, String fieldName, String defaultValue) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            return value != null ? value.toString() : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @GetMapping("/debug/roles-info")
    @ResponseBody
    public String debugRolesInfo(HttpSession session) {
        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            return "Not logged in";
        }

        List<Role> roles = userRoleService.getUserRoles(user);
        boolean isAdmin = userRoleService.isAdmin(user);
        boolean isSuperAdmin = userRoleService.isSuperAdmin(user);

        StringBuilder sb = new StringBuilder();
        sb.append("User: ").append(getFieldValueSafely(user, "username", "unknown")).append("\n");
        sb.append("Roles: ").append(roles.stream().map(Role::getName).collect(Collectors.joining(", "))).append("\n");
        sb.append("isAdmin: ").append(isAdmin).append("\n");
        sb.append("isSuperAdmin: ").append(isSuperAdmin).append("\n");

        return sb.toString();
    }
}
