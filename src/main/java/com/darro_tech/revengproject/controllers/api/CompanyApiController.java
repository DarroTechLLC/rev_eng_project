package com.darro_tech.revengproject.controllers.api;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.FileStorageService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class CompanyApiController {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/companies-all")
    public ResponseEntity<?> getCompanies(HttpSession session) {
        try {
            List<Company> companies = companyService.getAllCompanies()
                    .stream()
                    .sorted(Comparator.comparing(Company::getName))
                    .collect(Collectors.toList());

            String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");

            // If no company is selected, default to first alphabetical
            if (selectedCompanyId == null && !companies.isEmpty()) {
                selectedCompanyId = companies.get(0).getId();
                session.setAttribute("selectedCompanyId", selectedCompanyId);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("companies", companies);
            response.put("selectedCompanyId", selectedCompanyId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/companies/select/{id}")
    public ResponseEntity<?> selectCompany(@PathVariable String id, HttpSession session) {
        try {
            // Verify company exists
            if (companyService.getCompanyById(id).isPresent()) {
                session.setAttribute("selectedCompanyId", id);
                
                // Clear any existing farm-related attributes to prevent stale lazy-loaded references
                session.removeAttribute("selectedFarm");
                
                return ResponseEntity.ok(Map.of("success", true));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Company not found"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/admin/companies/{id}/logo-upload")
    public ResponseEntity<?> uploadLogo(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        try {
            String logoUrl = fileStorageService.saveFile(file, "company_" + id);
            companyService.updateLogoUrl(id, logoUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("logoUrl", logoUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}
