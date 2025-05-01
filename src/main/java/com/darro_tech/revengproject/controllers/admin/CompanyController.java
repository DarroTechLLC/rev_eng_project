package com.darro_tech.revengproject.controllers.admin;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.services.CompanyService;

@Controller("adminCompanyController")
@RequestMapping("/admin/companies")
public class CompanyController extends BaseController {

    @Autowired
    private CompanyService companyService;

    // List all companies view
    @GetMapping
    public String listCompanies(Model model) {
        List<Company> companies = companyService.getAllCompanies();
        System.out.println("Found " + companies.size() + " companies in the database");
        model.addAttribute("companies", companies);
        model.addAttribute("title", "Companies Management");
        return view("admin/companies/index", model);
    }

    // Create company form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("company", new Company());
        model.addAttribute("title", "Create Company");
        return view("admin/companies/create", model);
    }

    // Process company creation
    @PostMapping("/create")
    public String createCompany(@RequestParam String name,
            @RequestParam String displayName,
            @RequestParam(required = false) String logoUrl,
            RedirectAttributes redirectAttributes) {
        try {
            Company company = companyService.createCompany(name, displayName);

            // Set logo URL if provided
            if (logoUrl != null && !logoUrl.trim().isEmpty()) {
                companyService.updateLogoUrl(company.getId(), logoUrl);
            }

            redirectAttributes.addFlashAttribute("success", "Company created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating company: " + e.getMessage());
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

    // REST API endpoints for logo operations and AJAX
    @PostMapping("/api/{id}/logo")
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

    @DeleteMapping("/api/{id}/logo")
    @ResponseBody
    public ResponseEntity<?> removeLogoUrl(@PathVariable String id) {
        try {
            companyService.removeLogoUrl(id);
            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // API endpoint to get all companies (for AJAX)
    @GetMapping("/api/list")
    @ResponseBody
    public List<Company> getCompanies() {
        return companyService.getAllCompanies();
    }
}
