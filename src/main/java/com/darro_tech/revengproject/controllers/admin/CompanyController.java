package com.darro_tech.revengproject.controllers.admin;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.darro_tech.revengproject.controllers.AuthenticationController;
import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.UserRoleService;

import jakarta.servlet.http.HttpSession;

@Controller("adminCompanyController")
@RequestMapping("/admin/companies")
public class CompanyController extends BaseController {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private AuthenticationController authenticationController;

    // List all companies view
    @GetMapping
    public String listCompanies(Model model, HttpSession session) {
        // Get current user from session
        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            return "redirect:/login";
        }

        List<Company> companies = companyService.getAllCompanies();
        System.out.println("🏢 Found " + companies.size() + " companies in the database");
        model.addAttribute("companies", companies);
        model.addAttribute("title", "Companies Management");
        return view("admin/companies/index", model);
    }

    // Create company form
    @GetMapping("/create")
    public String showCreateForm(Model model,
            @RequestParam(value = "isAjaxRequest", required = false) Boolean isAjaxRequest) {
        model.addAttribute("company", new Company());
        model.addAttribute("title", "Create Company");
        model.addAttribute("isAjaxRequest", isAjaxRequest != null && isAjaxRequest);
        return view("admin/companies/create", model);
    }

    // Process company creation
    @PostMapping("/create")
    public String createCompany(@RequestParam String name,
            @RequestParam String displayName,
            @RequestParam(required = false) String logoUrl,
            RedirectAttributes redirectAttributes,
            @RequestParam(value = "isAjaxRequest", required = false) Boolean isAjaxRequest,
            HttpSession session) {

        // Get current user from session
        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            System.out.println("❌ Company creation failed: user not authenticated");
            redirectAttributes.addFlashAttribute("errorMessage", "Authentication required");
            redirectAttributes.addFlashAttribute("requestedUrl", "/admin/companies/create");
            return "redirect:/login";
        }

        // Verify user has superadmin access
        if (!userRoleService.isSuperAdmin(user)) {
            System.out.println("⛔ Company creation failed: user " + user.getUsername() + " is not a super admin");
            redirectAttributes.addFlashAttribute("errorMessage", "Super Admin privileges required to create companies");
            redirectAttributes.addFlashAttribute("username", user.getUsername());
            redirectAttributes.addFlashAttribute("userRoles", user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.joining(", ")));
            redirectAttributes.addFlashAttribute("requestedUrl", "/admin/companies/create");
            redirectAttributes.addFlashAttribute("debugInfo", "Current roles: " + user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.joining(", ")));
            return "redirect:/access-denied";
        }

        try {
            // Validate input
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Company name is required");
            }
            if (!name.matches("[a-zA-Z0-9\\s-]+")) {
                throw new IllegalArgumentException("Company name can only contain letters, numbers, spaces and hyphens");
            }
            if (displayName == null || displayName.trim().isEmpty()) {
                throw new IllegalArgumentException("Display name is required");
            }
            if (logoUrl != null && !logoUrl.trim().isEmpty() && !logoUrl.matches("^https?://.*")) {
                throw new IllegalArgumentException("Logo URL must be a valid HTTP/HTTPS URL");
            }

            System.out.println("🏢 Creating new company: " + name);
            Company company = companyService.createCompany(name.trim(), displayName.trim());

            // Set logo URL if provided
            if (logoUrl != null && !logoUrl.trim().isEmpty()) {
                System.out.println("🖼️ Setting logo URL for company: " + company.getId());
                companyService.updateLogoUrl(company.getId(), logoUrl.trim());
            }

            System.out.println("✅ Company created successfully: " + company.getId());
            redirectAttributes.addFlashAttribute("success", "Company '" + displayName + "' created successfully!");
            redirectAttributes.addFlashAttribute("companyId", company.getId());

            // For AJAX requests, return a success response
            if (isAjaxRequest != null && isAjaxRequest) {
                return "redirect:/admin/companies?success=true&companyId=" + company.getId();
            }
        } catch (Exception e) {
            String errorMsg = "Error creating company: " + e.getMessage();
            System.out.println("❌ " + errorMsg);
            System.out.println("Stack trace: " + e.getStackTrace());

            redirectAttributes.addFlashAttribute("error", errorMsg);
            redirectAttributes.addFlashAttribute("formData", Map.of(
                    "name", name,
                    "displayName", displayName,
                    "logoUrl", logoUrl != null ? logoUrl : ""
            ));

            // For AJAX requests, return an error response
            if (isAjaxRequest != null && isAjaxRequest) {
                return "redirect:/admin/companies/create?error=" + errorMsg;
            }
            return "redirect:/admin/companies/create";
        }
        return "redirect:/admin/companies";
    }

    // Edit company form
    @GetMapping("/{id}")
    public String showEditForm(@PathVariable String id, Model model,
            @RequestParam(value = "isAjaxRequest", required = false) Boolean isAjaxRequest) {
        Optional<Company> companyOpt = companyService.getCompanyById(id);
        if (companyOpt.isPresent()) {
            model.addAttribute("company", companyOpt.get());
            model.addAttribute("title", "Edit Company");
            model.addAttribute("isAjaxRequest", isAjaxRequest != null && isAjaxRequest);
            return view("admin/companies/edit", model);
        }
        return "redirect:/admin/companies";
    }

    // Process company update
    @PostMapping("/{id}")
    public String updateCompany(@PathVariable String id,
            @RequestParam String name,
            @RequestParam String displayName,
            RedirectAttributes redirectAttributes) {
        try {
            companyService.updateCompany(id, name, displayName);
            redirectAttributes.addFlashAttribute("success", "Company updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating company: " + e.getMessage());
        }
        return "redirect:/admin/companies";
    }

    // Delete company
    @PostMapping("/{id}/delete")
    public String deleteCompany(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            // Check if company exists
            Optional<Company> companyOpt = companyService.getCompanyById(id);
            if (companyOpt.isPresent()) {
                String companyName = companyOpt.get().getDisplayName();

                // Delete company logic - you'll need to add this method to CompanyService
                companyService.deleteCompany(id);

                redirectAttributes.addFlashAttribute("success",
                        "Company '" + companyName + "' has been deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Company not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting company: " + e.getMessage());
        }
        return "redirect:/admin/companies";
    }

    // Update API endpoints to use consistent naming
    @PostMapping("/api/logo/{id}")
    @ResponseBody
    public ResponseEntity<?> updateLogoUrl(@PathVariable String id, @RequestBody Map<String, String> payload) {
        try {
            String logoUrl = payload.get("logoUrl");
            companyService.updateLogoUrl(id, logoUrl);
            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/logo/{id}")
    @ResponseBody
    public ResponseEntity<?> removeLogoUrl(@PathVariable String id) {
        try {
            companyService.removeLogoUrl(id);
            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/api/list")
    @ResponseBody
    public List<Company> getCompanies() {
        return companyService.getAllCompanies();
    }
}
