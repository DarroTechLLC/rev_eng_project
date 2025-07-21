package com.darro_tech.revengproject.controllers.api;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.darro_tech.revengproject.dto.FarmCreateDTO;
import com.darro_tech.revengproject.dto.ValidationResult;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.FarmSelectionService;
import com.darro_tech.revengproject.services.FarmService;
import com.darro_tech.revengproject.services.UserService;

import jakarta.servlet.http.HttpSession;

/**
 * API controller for farm-related operations
 */
@RestController
@RequestMapping("/api")
public class FarmApiController {

    private static final Logger logger = Logger.getLogger(FarmApiController.class.getName());

    @Autowired
    private FarmService farmService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserService userService;

    @Autowired
    private FarmSelectionService farmSelectionService;

    /**
     * Get farms for the currently selected company
     */
    @GetMapping("/farms")
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
    @GetMapping("/farms/by-company/{companyId}")
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

            // Set a default farm in the session when changing companies
            // This is crucial for proper navigation when switching between companies
            if (!farms.isEmpty()) {
                // Sort farms alphabetically for consistent selection
                farms.sort(Comparator.comparing(Farm::getName));
                String defaultFarmId = farms.get(0).getId();
                session.setAttribute("selectedFarmKey", defaultFarmId);
                logger.info("🔄 Set default farm in session: " + farms.get(0).getName() + " (ID: " + defaultFarmId + ")");
            } else {
                // Clear the farm key if no farms exist for this company
                session.removeAttribute("selectedFarmKey");
                logger.info("⚠️ No farms found for company, cleared farm selection from session");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("farms", farms);
            response.put("companyId", companyId);
            response.put("companyName", companyName);

            // Include the selected farm in the response
            if (session.getAttribute("selectedFarmKey") != null) {
                response.put("selectedFarmId", session.getAttribute("selectedFarmKey"));
            }

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
    @GetMapping("/farms/{id}")
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
    @GetMapping("/farms/search")
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

    @PostMapping("/admin/farms/create")
    public ResponseEntity<Map<String, Object>> createFarm(
            @RequestBody FarmCreateDTO farmDTO,
            @SessionAttribute("selectedCompanyId") String companyId) {
        logger.info("📝 Creating new farm via API");

        try {
            // Create a new Farm entity from the DTO
            Farm farm = new Farm();
            farm.setName(farmDTO.getName());
            farm.setDisplayName(farmDTO.getDisplayName());
            farm.setFarmType(farmDTO.getFarmType());
            farm.setTempSourceId(farmDTO.getTempSourceId());
            farm.setIsTempSource(farmDTO.getIsTempSource() != null ? farmDTO.getIsTempSource() : false);

            // Save the farm with all details
            Farm created = farmService.createFarmWithDetails(farm);
            logger.info(String.format("🌾 Created farm with type: %s", created.getFarmType()));

            // Assign the farm to the company
            farmService.assignFarmToCompany(created.getId(), companyId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "farmId", created.getId(),
                    "message", "Farm created successfully"
            ));
        } catch (Exception e) {
            logger.severe("❌ Error creating farm: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/admin/farms/validate/name")
    public ResponseEntity<ValidationResult> validateName(
            @RequestBody Map<String, String> request,
            @SessionAttribute("selectedCompanyId") String companyId) {
        logger.info("✔️ Validating farm name: " + request.get("name"));
        return ResponseEntity.ok(farmService.validateFarmName(request.get("name"), companyId));
    }

    @PostMapping("/admin/farms/validate/display-name")
    public ResponseEntity<ValidationResult> validateDisplayName(
            @RequestBody Map<String, String> request,
            @SessionAttribute("selectedCompanyId") String companyId) {
        String displayName = request.get("displayName");
        logger.info("✔️ Validating farm display name: " + displayName);

        // If display name is null or empty, it's valid since it's optional
        if (displayName == null || displayName.trim().isEmpty()) {
            return ResponseEntity.ok(new ValidationResult(true, null));
        }

        return ResponseEntity.ok(farmService.validateDisplayName(displayName, companyId));
    }

    @GetMapping("/admin/farms/temp-sources")
    public ResponseEntity<List<Map<String, String>>> getTempSources(
            @SessionAttribute("selectedCompanyId") String companyId) {
        logger.info("🔍 Fetching temperature source farms for company: " + companyId);
        List<Farm> tempSources = farmService.getTempSourceFarms(companyId);

        List<Map<String, String>> response = tempSources.stream()
                .map(farm -> {
                    Map<String, String> farmData = new HashMap<>();
                    farmData.put("id", farm.getId());
                    farmData.put("name", farm.getName());
                    return farmData;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Get the currently selected farm
     */
    @GetMapping("/farms/selected")
    public ResponseEntity<Map<String, Object>> getSelectedFarm(HttpSession session) {
        logger.info("🔍 Getting selected farm from session");

        // Get selected farm from session
        String selectedFarmId = farmSelectionService.getSelectedFarmId(session);

        // Get selected company from session
        String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
        if (selectedCompanyId == null) {
            logger.warning("❌ No company selected in session");
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "No company selected"
            ));
        }

        if (selectedFarmId == null) {
            logger.info("ℹ️ No farm selected in session");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "selected", false,
                    "message", "No farm selected"
            ));
        }

        try {
            // Get farm details
            Optional<Farm> farmOpt = farmService.getFarmById(selectedFarmId);
            if (!farmOpt.isPresent()) {
                logger.warning("⚠️ Selected farm not found: " + selectedFarmId);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "selected", false,
                        "message", "Selected farm not found"
                ));
            }

            Farm farm = farmOpt.get();

            // Verify farm belongs to selected company
            List<Farm> companyFarms = farmService.getFarmsByCompanyId(selectedCompanyId);
            boolean farmBelongsToCompany = companyFarms.stream()
                    .anyMatch(f -> f.getId().equals(selectedFarmId));

            if (!farmBelongsToCompany) {
                logger.warning("⚠️ Selected farm does not belong to selected company");
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "selected", false,
                        "message", "Selected farm does not belong to selected company"
                ));
            }

            logger.info("✅ Found selected farm: " + farm.getName());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "selected", true,
                    "farm", farm
            ));
        } catch (Exception e) {
            logger.severe("❌ Error getting selected farm: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Error getting selected farm: " + e.getMessage()
            ));
        }
    }

    /**
     * Select a farm and update session
     */
    @PostMapping("/farms/select")
    public ResponseEntity<Map<String, Object>> selectFarm(
            @RequestBody Map<String, String> request,
            HttpSession session) {
        String farmId = request.get("farmId");
        logger.info("🔄 Selecting farm with ID: " + farmId);

        try {
            // Get selected company from session
            String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
            if (selectedCompanyId == null) {
                logger.warning("❌ No company selected in session");
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "No company selected"
                ));
            }

            // Validate farm exists
            Optional<Farm> farmOpt = farmService.getFarmById(farmId);
            if (!farmOpt.isPresent()) {
                logger.warning("❌ Farm not found with ID: " + farmId);
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Farm not found"
                ));
            }

            Farm farm = farmOpt.get();

            // Use FarmSelectionService to select the farm
            boolean selected = farmSelectionService.selectFarm(farmId, selectedCompanyId, session);

            if (!selected) {
                logger.warning("❌ Failed to select farm");
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Failed to select farm"
                ));
            }

            // Also store in localStorage via the response
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "farm", farm,
                    "message", "Farm selected successfully"
            ));
        } catch (Exception e) {
            logger.severe("❌ Error selecting farm: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Error selecting farm: " + e.getMessage()
            ));
        }
    }
}
