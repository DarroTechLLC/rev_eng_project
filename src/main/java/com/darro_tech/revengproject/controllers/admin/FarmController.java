package com.darro_tech.revengproject.controllers.admin;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.FarmService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/farms")
public class FarmController extends BaseController {

    @Autowired
    private FarmService farmService;

    @Autowired
    private CompanyService companyService;

    /**
     * Show all farms for the selected company
     */
    @GetMapping
    public String manageFarms(Model model, HttpSession session,
            @RequestParam(required = false) String companyId) {
        model.addAttribute("title", "Farm Management");

        // Get all companies sorted alphabetically
        List<Company> companies = companyService.getAllCompanies();
        companies.sort(Comparator.comparing(Company::getName));
        model.addAttribute("companies", companies);

        // Check if a company ID was provided in the request
        if (companyId != null && !companyId.isEmpty()) {
            // Store the selected company in session
            session.setAttribute("selectedCompanyId", companyId);
        } else {
            // If no company selected, use the first one alphabetically
            String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
            if (selectedCompanyId == null && !companies.isEmpty()) {
                selectedCompanyId = companies.get(0).getId();
                session.setAttribute("selectedCompanyId", selectedCompanyId);
            }
        }

        // Get the selected company from session
        String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");

        // If a company is selected, get its farms
        if (selectedCompanyId != null) {
            List<Farm> farms = farmService.getFarmsByCompanyId(selectedCompanyId);
            model.addAttribute("farms", farms);

            // Add company info to model
            companyService.getCompanyById(selectedCompanyId).ifPresent(company -> {
                model.addAttribute("selectedCompany", company);
            });
        }

        return view("admin/farms/farm-management", model);
    }

    /**
     * Show the form to create a new farm
     */
    @GetMapping("/create")
    public String showCreateFarmForm(Model model, HttpSession session) {
        model.addAttribute("title", "Create Farm");
        model.addAttribute("farm", new Farm());

        // Get all companies for the dropdown if needed
        List<Company> companies = companyService.getAllCompanies();
        companies.sort(Comparator.comparing(Company::getName));
        model.addAttribute("companies", companies);

        // Get the selected company from session to display in the form
        String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
        if (selectedCompanyId != null) {
            companyService.getCompanyById(selectedCompanyId).ifPresent(company -> {
                model.addAttribute("selectedCompany", company);
            });
        }

        return view("admin/farms/create", model);
    }

    /**
     * Process the creation of a new farm
     */
    @PostMapping("/create")
    public String createFarm(@ModelAttribute Farm farm,
            @RequestParam String companyId,
            RedirectAttributes redirectAttributes) {

        // Generate a UUID for the farm
        farm.setId(UUID.randomUUID().toString());

        // Save the farm
        Farm savedFarm = farmService.createFarm(farm.getName(), farm.getDisplayName());

        // Update additional farm properties
        if (farm.getFarmType() != null) {
            savedFarm.setFarmType(farm.getFarmType());
        }

        if (farm.getIsTempSource() != null) {
            savedFarm.setIsTempSource(farm.getIsTempSource());
        }

        if (farm.getTempSourceId() != null) {
            savedFarm.setTempSourceId(farm.getTempSourceId());
        }

        farmService.updateFarm(savedFarm.getId(), savedFarm.getName(), savedFarm.getDisplayName());

        // Assign the farm to the company if a company ID was provided
        if (companyId != null && !companyId.isEmpty()) {
            farmService.assignFarmToCompany(savedFarm.getId(), companyId);
        }

        redirectAttributes.addFlashAttribute("message", "Farm created successfully!");
        return "redirect:/admin/farms?companyId=" + companyId;
    }

    /**
     * Show the form to edit a farm
     */
    @GetMapping("/edit/{id}")
    public String showEditFarmForm(@PathVariable String id, Model model, HttpSession session) {
        model.addAttribute("title", "Edit Farm");

        // Get the selected company from session
        String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
        if (selectedCompanyId != null) {
            companyService.getCompanyById(selectedCompanyId).ifPresent(company -> {
                model.addAttribute("selectedCompany", company);
            });
        }

        farmService.getFarmById(id).ifPresent(farm -> {
            model.addAttribute("farm", farm);
        });

        return view("admin/farms/edit", model);
    }

    /**
     * Process the update of a farm
     */
    @PostMapping("/edit/{id}")
    public String updateFarm(@PathVariable String id,
            @ModelAttribute Farm farm,
            @RequestParam String companyId,
            RedirectAttributes redirectAttributes) {

        // Update the farm
        Farm updatedFarm = farmService.updateFarm(id, farm.getName(), farm.getDisplayName());

        // Update additional farm properties
        if (farm.getFarmType() != null) {
            updatedFarm.setFarmType(farm.getFarmType());
        }

        if (farm.getIsTempSource() != null) {
            updatedFarm.setIsTempSource(farm.getIsTempSource());
        }

        if (farm.getTempSourceId() != null) {
            updatedFarm.setTempSourceId(farm.getTempSourceId());
        }

        farmService.updateFarm(updatedFarm.getId(), updatedFarm.getName(), updatedFarm.getDisplayName());

        // Update company association if needed
        if (companyId != null && !companyId.isEmpty()) {
            farmService.updateFarmCompanyAssociation(updatedFarm.getId(), companyId);
        }

        redirectAttributes.addFlashAttribute("message", "Farm updated successfully!");
        return "redirect:/admin/farms?companyId=" + companyId;
    }

    /**
     * Delete a farm
     */
    @PostMapping("/delete/{id}")
    public String deleteFarm(@PathVariable String id,
            @RequestParam String companyId,
            RedirectAttributes redirectAttributes) {

        farmService.deleteFarm(id);

        redirectAttributes.addFlashAttribute("message", "Farm deleted successfully!");
        return "redirect:/admin/farms?companyId=" + companyId;
    }
}
