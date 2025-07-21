package com.darro_tech.revengproject.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.FarmService;
import com.darro_tech.revengproject.services.UserRoleService;

import jakarta.servlet.http.HttpSession;

public abstract class BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private FarmService farmService;

    @ModelAttribute
    public void addCommonAttributes(Model model, HttpSession session) {
        User user = authenticationController.getUserFromSession(session);
        if (user != null) {
            model.addAttribute("user", user);

            // Get user name for display
            String wholeName = "User";
            try {
                String firstName = (String) getFieldValueSafely(user, "firstName", "");
                String lastName = (String) getFieldValueSafely(user, "lastName", "");
                String username = (String) getFieldValueSafely(user, "username", "User");

                wholeName = (firstName + " " + lastName).trim();
                if (wholeName.isEmpty()) {
                    wholeName = username;
                }
            } catch (Exception e) {
                // In case of any errors, use a default name
                wholeName = "User";
            }

            model.addAttribute("wholeName", wholeName);

            // Add role-based attributes
            boolean isAdmin = userRoleService.isAdmin(user);
            boolean isSuperAdmin = userRoleService.isSuperAdmin(user);

            model.addAttribute("isAdmin", isAdmin ? Boolean.TRUE : Boolean.FALSE);
            model.addAttribute("isSuperAdmin", isSuperAdmin ? Boolean.TRUE : Boolean.FALSE);

            // Add selected company if available in session
            String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
            if (selectedCompanyId != null) {
                try {
                    companyService.getCompanyById(selectedCompanyId).ifPresentOrElse(
                            company -> {
                                model.addAttribute("selectedCompany", company);
                                // Add the company ID for Thymeleaf templates
                                model.addAttribute("selectedCompanyKey", company.getId());
                                model.addAttribute("selectedCompanyName", company.getName());
                                logger.info("🏢 Added selected company to model: {}", company.getName());
                            },
                            () -> {
                                logger.warn("⚠️ Selected company ID not found: {}", selectedCompanyId);
                                // If company not found, try to select the first available company
                                selectFirstAvailableCompany(session, model);
                            }
                    );
                } catch (Exception e) {
                    logger.error("❌ Failed to load selected company: {}", e.getMessage(), e);
                    // If error occurs, try to select the first available company
                    selectFirstAvailableCompany(session, model);
                }
            } else {
                // If no company selected, select the first available one
                selectFirstAvailableCompany(session, model);
            }

            // Add farms and selected farm key
            try {
                String selectedFarmId = (String) session.getAttribute("selectedFarmKey");
                List<Farm> userFarms = farmService.getFarmsByCompanyId(selectedCompanyId);
                model.addAttribute("farms", userFarms);

                if (selectedFarmId != null) {
                    model.addAttribute("selectedFarmKey", selectedFarmId);
                    logger.info("🚜 Added selected farm key to model: {}", selectedFarmId);

                    // Try to find the selected farm and add its full details
                    userFarms.stream()
                            .filter(farm -> selectedFarmId.equals(farm.getId()))
                            .findFirst()
                            .ifPresent(farm -> {
                                model.addAttribute("selectedFarm", farm);
                                model.addAttribute("selectedFarmName", farm.getName());
                                logger.info("🚜 Added selected farm details to model: {}", farm.getName());
                            });
                } else if (!userFarms.isEmpty()) {
                    // Select first farm if none selected
                    Farm firstFarm = userFarms.get(0);
                    model.addAttribute("selectedFarmKey", firstFarm.getId());
                    model.addAttribute("selectedFarm", firstFarm);
                    model.addAttribute("selectedFarmName", firstFarm.getName());
                    session.setAttribute("selectedFarmKey", firstFarm.getId());
                    logger.info("🚜 Selected first available farm: {}", firstFarm.getName());
                }
            } catch (Exception e) {
                logger.error("❌ Failed to load farms: {}", e.getMessage(), e);
            }

            logger.debug("👤 Adding role attributes for user: {}",
                    getFieldValueSafely(user, "username", "unknown"));
            logger.debug("👮 isAdmin: {}", isAdmin);
            logger.debug("🔑 isSuperAdmin: {}", isSuperAdmin);
        } else {
            // Always set these values, even if there's no user
            model.addAttribute("isAdmin", Boolean.FALSE);
            model.addAttribute("isSuperAdmin", Boolean.FALSE);
            model.addAttribute("wholeName", "Guest");
        }
    }

    /**
     * Safely get field value using reflection
     */
    private Object getFieldValueSafely(Object obj, String fieldName, Object defaultValue) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Helper method to select the first available company
     */
    private void selectFirstAvailableCompany(HttpSession session, Model model) {
        try {
            List<Company> companies = companyService.getAllCompanies();
            if (!companies.isEmpty()) {
                Company firstCompany = companies.get(0);
                session.setAttribute("selectedCompanyId", firstCompany.getId());
                model.addAttribute("selectedCompany", firstCompany);
                // Add the company ID and name for Thymeleaf templates
                model.addAttribute("selectedCompanyKey", firstCompany.getId());
                model.addAttribute("selectedCompanyName", firstCompany.getName());
                logger.info("🏢 Selected first available company: {}", firstCompany.getName());
            } else {
                logger.warn("⚠️ No companies available to select");
            }
        } catch (Exception e) {
            logger.error("❌ Failed to select first available company: {}", e.getMessage(), e);
        }
    }

    /**
     * Helper method to build a view name with content path
     *
     * @param viewName the name of the view without content/ prefix
     * @param model the model to add attributes to
     * @return the layout template name
     */
    protected String view(String viewName, Model model) {
        try {
            // First, check if we need to use the correct path format
            if (!viewName.startsWith("content/")) {
                logger.debug("📄 Setting content template path: content/{}", viewName);
                model.addAttribute("content", "content/" + viewName);
            } else {
                logger.debug("📄 Using provided content template path: {}", viewName);
                model.addAttribute("content", viewName);
            }

            // Add debug info
            logger.info("🔍 Resolving view: {} to layout: layouts/main-layout", viewName);

            // Return the main layout that will include the content
            return "layouts/main-layout";
        } catch (Exception e) {
            // Log any errors and fall back to a simple view name
            logger.error("❌ Error in view resolution: {}", e.getMessage(), e);
            return viewName;
        }
    }
}
