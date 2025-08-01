package com.darro_tech.revengproject.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.dto.CompanyDTO;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.UserRoleService;
import com.darro_tech.revengproject.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller("companySelectorController")
@RequestMapping("/api/company-selector")
public class CompanySelectorController extends BaseController {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private AuthenticationController authenticationController;

    /**
     * API endpoint to get all companies for the current user
     */
    @GetMapping("/list")
    @ResponseBody
    public Map<String, Object> getAllCompanies(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            response.put("error", "User not authenticated");
            return response;
        }

        boolean isSuperAdmin = userRoleService.isSuperAdmin(user);
        List<CompanyDTO> companies = companyService.getUserCompanies(user.getId(), isSuperAdmin);

        // Get selected company from session
        String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");

        response.put("companies", companies);
        response.put("selectedCompanyId", selectedCompanyId);

        return response;
    }

    /**
     * API endpoint to set the selected company
     */
    @PostMapping("/set/{companyId}")
    @ResponseBody
    public Map<String, Object> setCompany(@PathVariable String companyId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return response;
        }

        // Verify the user has access to this company
        boolean isSuperAdmin = userRoleService.isSuperAdmin(user);

        if (!isSuperAdmin) {
            boolean hasAccess = companyService.userHasCompanyAccess(user.getId(), companyId);
            if (!hasAccess) {
                response.put("success", false);
                response.put("message", "Access denied to this company");
                return response;
            }
        }

        // Get company details to return in response
        Optional<Company> companyOpt = companyService.getCompanyById(companyId);
        if (companyOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Company not found");
            return response;
        }

        // Set the selected company in session
        session.setAttribute("selectedCompanyId", companyId);

        response.put("success", true);
        response.put("company", companyOpt.get());

        return response;
    }

    /**
     * Handle company change that stays on the same page (matches Next.js
     * behavior) This method updates the company context and reloads the current
     * page
     */
    @PostMapping("/change-company")
    public String changeCompany(@RequestParam String companyId,
            HttpServletRequest request,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return "redirect:/login";
        }

        // Verify the user has access to this company
        boolean isSuperAdmin = userRoleService.isSuperAdmin(user);

        if (!isSuperAdmin) {
            boolean hasAccess = companyService.userHasCompanyAccess(user.getId(), companyId);
            if (!hasAccess) {
                response.put("success", false);
                response.put("message", "Access denied to this company");
                return "redirect:/select-company";
            }
        }

        // Get company details
        Optional<Company> companyOpt = companyService.getCompanyById(companyId);
        if (companyOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Company not found");
            return "redirect:/select-company";
        }

        // Set the selected company in session
        session.setAttribute("selectedCompanyId", companyId);

        // Get the current URL to stay on the same page
        String referer = request.getHeader("Referer");
        String currentUrl = referer != null ? referer : "/dashboard";

        // Log the company change
        logger.info("🔄 Company changed to: " + companyOpt.get().getName() + " (ID: " + companyId + ") for user: " + user.getUsername());
        logger.info("📍 Staying on current page: " + currentUrl);

        // Redirect back to the same page (this will reload with new company data)
        return "redirect:" + currentUrl;
    }
}
