package com.darro_tech.revengproject.controllers.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.dto.CompanyDTO;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.UserService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/companies")
public class CompanySelectorController {
    private static final Logger logger = Logger.getLogger(CompanySelectorController.class.getName());

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserService userService;

    /**
     * Get all companies available for the current user
     */
    @GetMapping
    public ResponseEntity<List<CompanyDTO>> getUserCompanies() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        boolean isSuperAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        logger.info("Fetching companies for user: " + userId);
        List<CompanyDTO> companies = companyService.getUserCompanies(userId, isSuperAdmin);
        return ResponseEntity.ok(companies);
    }

    /**
     * Check if user has access to a specific company
     */
    @GetMapping("/{companyId}/access")
    public ResponseEntity<Boolean> checkCompanyAccess(@PathVariable String companyId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        boolean isSuperAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (isSuperAdmin) {
            return ResponseEntity.ok(true);
        }

        boolean hasAccess = companyService.userHasCompanyAccess(userId, companyId);
        return ResponseEntity.ok(hasAccess);
    }

    /**
     * Select a company (store selection in session)
     */
    @PostMapping("/select/{companyId}")
    public ResponseEntity<Map<String, Object>> selectCompany(
            @PathVariable("companyId") String companyId,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        // Verify the user has access to this company
        List<Company> userCompanies = userService.getUserCompanies(user.getId());
        boolean hasAccess = userCompanies.stream()
                .anyMatch(c -> c.getId().equals(companyId));

        if (!hasAccess) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied to this company"));
        }

        // Get the company details
        Optional<Company> companyOpt = companyService.getCompanyById(companyId);
        if (companyOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Company not found"));
        }

        // Store the selected company in session
        session.setAttribute("selectedCompanyId", companyId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "selectedCompanyId", companyId,
                "companyName", companyOpt.get().getName()
        ));
    }

    /**
     * Get the currently selected company
     */
    @GetMapping("/selected")
    public ResponseEntity<Map<String, Object>> getSelectedCompany(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
        if (selectedCompanyId == null) {
            return ResponseEntity.ok(Map.of("selected", false));
        }

        Optional<Company> companyOpt = companyService.getCompanyById(selectedCompanyId);
        if (companyOpt.isEmpty()) {
            // Clear the invalid selection
            session.removeAttribute("selectedCompanyId");
            return ResponseEntity.ok(Map.of("selected", false));
        }

        Company company = companyOpt.get();

        return ResponseEntity.ok(Map.of(
                "selected", true,
                "companyId", company.getId(),
                "companyName", company.getName(),
                "logoUrl", company.getLogoUrl() != null ? company.getLogoUrl() : ""
        ));
    }
}
 