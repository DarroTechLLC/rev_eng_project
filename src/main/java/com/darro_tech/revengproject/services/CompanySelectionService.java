package com.darro_tech.revengproject.services;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.dto.CompanyDTO;

import jakarta.servlet.http.HttpSession;

/**
 * Service for managing company selection for users
 */
@Service
public class CompanySelectionService {

    private static final Logger logger = Logger.getLogger(CompanySelectionService.class.getName());

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserRoleService userRoleService;

    /**
     * Select the first alphabetical company for a user and store it in the
     * session
     *
     * @param user The authenticated user
     * @param session The HTTP session
     * @return true if a company was selected, false otherwise
     */
    public boolean selectDefaultCompanyForUser(User user, HttpSession session) {
        if (user == null) {
            logger.warning("🚫 Cannot select company for null user");
            return false;
        }

        logger.info("🔍 Selecting default company for user: " + user.getUsername());

        boolean isSuperAdmin = userRoleService.isSuperAdmin(user);

        // Get all companies for the user
        List<CompanyDTO> userCompanies = companyService.getUserCompanies(user.getId(), isSuperAdmin);

        if (userCompanies.isEmpty()) {
            logger.warning("⚠️ No companies available for user: " + user.getUsername());
            return false;
        }

        // Sort alphabetically by name and get the first company
        userCompanies.sort(Comparator.comparing(CompanyDTO::getCompanyName));
        CompanyDTO firstCompany = userCompanies.get(0);

        // Store the selected company ID in session
        session.setAttribute("selectedCompanyId", firstCompany.getCompanyId());
        logger.info("✅ Auto-selected company: " + firstCompany.getCompanyName() + " (ID: " + firstCompany.getCompanyId() + ") for user: " + user.getUsername());

        return true;
    }

    /**
     * Select a specific company for the user if they have access to it
     *
     * @param user The authenticated user
     * @param companyId The ID of the company to select
     * @param session The HTTP session
     * @return true if the company was selected, false otherwise
     */
    public boolean selectCompanyForUser(User user, String companyId, HttpSession session) {
        if (user == null || companyId == null) {
            logger.warning("🚫 Cannot select company: user or companyId is null");
            return false;
        }

        // Check if user has access to this company
        boolean isSuperAdmin = userRoleService.isSuperAdmin(user);
        boolean hasAccess = isSuperAdmin || companyService.userHasCompanyAccess(user.getId(), companyId);

        if (!hasAccess) {
            logger.warning("🔒 User " + user.getUsername() + " does not have access to company ID: " + companyId);
            return false;
        }

        // Get company details for logging
        Optional<String> companyName = companyService.getCompanyById(companyId)
                .map(company -> company.getName());

        // Store the selected company ID in session
        session.setAttribute("selectedCompanyId", companyId);
        logger.info("✅ Manually selected company: "
                + (companyName.isPresent() ? companyName.get() : "Unknown")
                + " (ID: " + companyId + ") for user: " + user.getUsername());

        return true;
    }

    /**
     * Get the currently selected company ID from the session
     *
     * @param session The HTTP session
     * @return The selected company ID, or null if none is selected
     */
    public String getSelectedCompanyId(HttpSession session) {
        return (String) session.getAttribute("selectedCompanyId");
    }
}
