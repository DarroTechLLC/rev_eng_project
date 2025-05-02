package com.darro_tech.revengproject.controllers.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.FarmService;
import com.darro_tech.revengproject.services.UserService;

import jakarta.servlet.http.HttpSession;

/**
 * API controller for farm-related operations
 */
@RestController
@RequestMapping("/api/farms")
public class FarmApiController {

    private static final Logger logger = Logger.getLogger(FarmApiController.class.getName());

    @Autowired
    private FarmService farmService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserService userService;

    /**
     * Get farms for the currently selected company
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getFarms(HttpSession session) {
        logger.info("Getting farms for selected company");

        // Get selected company from session
        String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
        if (selectedCompanyId == null) {
            logger.warning("No company selected in session");
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "No company selected"
            ));
        }

        try {
            // Get farms for the selected company
            List<Farm> farms = farmService.getFarmsByCompanyId(selectedCompanyId);

            // Add company name
            String companyName = companyService.getCompanyById(selectedCompanyId)
                    .map(company -> company.getName())
                    .orElse("");

            logger.info("Found " + farms.size() + " farms for company: " + companyName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("farms", farms);
            response.put("companyId", selectedCompanyId);
            response.put("companyName", companyName);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error fetching farms: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Error fetching farms: " + e.getMessage()
            ));
        }
    }

    /**
     * Get farms for a specific company
     */
    @GetMapping("/by-company/{companyId}")
    public ResponseEntity<Map<String, Object>> getFarmsByCompany(
            @PathVariable String companyId,
            HttpSession session) {

        logger.info("Getting farms for company ID: " + companyId);

        // Update selected company in session
        session.setAttribute("selectedCompanyId", companyId);

        try {
            // Get farms for the selected company
            List<Farm> farms = farmService.getFarmsByCompanyId(companyId);

            // Add company name
            String companyName = companyService.getCompanyById(companyId)
                    .map(company -> company.getName())
                    .orElse("");

            logger.info("Found " + farms.size() + " farms for company: " + companyName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("farms", farms);
            response.put("companyId", companyId);
            response.put("companyName", companyName);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error fetching farms: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Error fetching farms: " + e.getMessage()
            ));
        }
    }

    /**
     * Get a specific farm by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFarm(@PathVariable String id) {
        logger.info("Getting farm details for ID: " + id);

        try {
            return farmService.getFarmById(id)
                    .map(farm -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("farm", farm);
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.ok(Map.of(
                            "success", false,
                            "message", "Farm not found"
                    )));
        } catch (Exception e) {
            logger.severe("Error fetching farm: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Error fetching farm: " + e.getMessage()
            ));
        }
    }

    /**
     * Search farms by name
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchFarms(
            @RequestParam String query,
            HttpSession session) {

        logger.info("Searching farms with query: " + query);

        // Get selected company from session
        String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
        if (selectedCompanyId == null) {
            logger.warning("No company selected in session for search");
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "No company selected"
            ));
        }

        try {
            // Search for farms in the selected company
            List<Farm> farms = farmService.searchFarmsByName(query, selectedCompanyId);

            logger.info("Found " + farms.size() + " farms matching search query");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("farms", farms);
            response.put("query", query);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error searching farms: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Error searching farms: " + e.getMessage()
            ));
        }
    }
}
