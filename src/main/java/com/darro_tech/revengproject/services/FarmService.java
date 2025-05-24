package com.darro_tech.revengproject.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darro_tech.revengproject.dto.ValidationResult;
import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.CompanyFarm;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.repositories.CompanyFarmRepository;
import com.darro_tech.revengproject.repositories.CompanyRepository;
import com.darro_tech.revengproject.repositories.FarmRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FarmService {

    private static final Logger logger = Logger.getLogger(FarmService.class.getName());

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private CompanyFarmRepository companyFarmRepository;

    @Autowired
    private CompanyRepository companyRepository;

    /**
     * Get all farms
     */
    @Transactional(readOnly = true)
    public List<Farm> getAllFarms() {
        System.out.println("FarmService: Getting all farms");
        return farmRepository.findAll();
    }

    /**
     * Get farm by ID
     */
    @Transactional(readOnly = true)
    public Optional<Farm> getFarmById(String id) {
        logger.info(String.format("🔍 Fetching farm with ID: %s", id));
        return farmRepository.findById(id);
    }

    /**
     * Get farms for a specific company
     */
    @Transactional(readOnly = true)
    public List<Farm> getFarmsByCompanyId(String companyId) {
        logger.info(String.format("🔍 Fetching farms for company ID: %s", companyId));
        List<CompanyFarm> companyFarms = companyFarmRepository.findByCompanyId(companyId);
        return companyFarms.stream()
                .map(CompanyFarm::getFarm)
                .collect(Collectors.toList());
    }

    /**
     * Create a new farm
     */
    @Transactional
    public Farm createFarm(String name, String displayName) {
        logger.info(String.format("📝 Creating new farm with name: %s", name));
        Farm farm = new Farm();
        farm.setId(UUID.randomUUID().toString());
        farm.setName(name);
        farm.setDisplayName(displayName);
        farm.setTimestamp(Instant.now());
        return farmRepository.save(farm);
    }

    /**
     * Update farm details
     */
    @Transactional
    public Farm updateFarm(String id, String name, String displayName) {
        logger.info(String.format("📝 Updating farm with ID: %s", id));
        Optional<Farm> farmOpt = farmRepository.findById(id);
        if (farmOpt.isPresent()) {
            Farm farm = farmOpt.get();
            farm.setName(name);
            farm.setDisplayName(displayName);
            farm.setTimestamp(Instant.now());
            return farmRepository.save(farm);
        }
        throw new RuntimeException("Farm not found with ID: " + id);
    }

    /**
     * Delete a farm
     */
    @Transactional
    public void deleteFarm(String id) {
        logger.info(String.format("🗑️ Deleting farm with ID: %s", id));
        farmRepository.deleteById(id);
    }

    /**
     * Assign a farm to a company
     */
    @Transactional
    public void assignFarmToCompany(String farmId, String companyId) {
        logger.info(String.format("🔗 Assigning farm %s to company %s", farmId, companyId));
        CompanyFarm companyFarm = new CompanyFarm();
        Company company = new Company();
        company.setId(companyId);
        companyFarm.setCompany(company);

        Farm farm = new Farm();
        farm.setId(farmId);
        companyFarm.setFarm(farm);

        companyFarm.setTimestamp(Instant.now());
        companyFarmRepository.save(companyFarm);
    }

    /**
     * Update farm-company association
     */
    @Transactional
    public void updateFarmCompanyAssociation(String farmId, String companyId) {
        logger.info(String.format("🔄 Updating farm %s company association to %s", farmId, companyId));
        // Delete existing association
        companyFarmRepository.deleteByFarmId(farmId);
        // Create new association
        assignFarmToCompany(farmId, companyId);
    }

    /**
     * Get company-farm associations for a farm
     */
    @Transactional(readOnly = true)
    public List<CompanyFarm> getCompanyFarmsForFarm(String farmId) {
        System.out.println("FarmService: Getting company associations for farm: " + farmId);
        return companyFarmRepository.findByFarmId(farmId);
    }

    /**
     * Search for farms by name for a specific company
     *
     * @param query Search term
     * @param companyId Company ID
     * @return List of farms that match the search criteria
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchFarmsByNameForCompany(String query, String companyId) {
        System.out.println("FarmService: Searching farms for company ID: " + companyId + ", query: " + query);

        List<Map<String, Object>> results = new ArrayList<>();

        // Get farms for the company
        List<Farm> companyFarms = getFarmsByCompanyId(companyId);

        // If no query is provided, return all farms for the company
        if (query == null || query.trim().isEmpty()) {
            for (Farm farm : companyFarms) {
                Map<String, Object> farmData = new HashMap<>();
                farmData.put("id", farm.getId());
                farmData.put("name", farm.getName());
                farmData.put("displayName", farm.getDisplayName());
                results.add(farmData);
            }
            return results;
        }

        // Filter farms by query
        String lowerQuery = query.toLowerCase();
        for (Farm farm : companyFarms) {
            if (farm.getName().toLowerCase().contains(lowerQuery)
                    || (farm.getDisplayName() != null && farm.getDisplayName().toLowerCase().contains(lowerQuery))) {

                Map<String, Object> farmData = new HashMap<>();
                farmData.put("id", farm.getId());
                farmData.put("name", farm.getName());
                farmData.put("displayName", farm.getDisplayName());
                results.add(farmData);
            }
        }

        return results;
    }

    /**
     * Search for farms by name for a specific company
     */
    @Transactional(readOnly = true)
    public List<Farm> searchFarmsByName(String query, String companyId) {
        System.out.println("FarmService: Searching farms with query '" + query + "' for company: " + companyId);

        // First get CompanyFarm links for this company
        List<CompanyFarm> companyFarms = companyFarmRepository.findByCompanyId(companyId);

        if (companyFarms.isEmpty()) {
            System.out.println("FarmService: No farms found for company ID: " + companyId);
            return new ArrayList<>();
        }

        // Then filter farms by name containing the query (case insensitive)
        List<Farm> matchingFarms = new ArrayList<>();
        String queryLower = query.toLowerCase();

        for (CompanyFarm cf : companyFarms) {
            Farm farm = cf.getFarm();
            if (farm != null) {
                if (farm.getName() != null && farm.getName().toLowerCase().contains(queryLower)) {
                    matchingFarms.add(farm);
                    continue;
                }

                if (farm.getDisplayName() != null && farm.getDisplayName().toLowerCase().contains(queryLower)) {
                    matchingFarms.add(farm);
                }
            }
        }

        System.out.println("FarmService: Found " + matchingFarms.size() + " farms matching query");
        return matchingFarms;
    }

    /**
     * Find a farm by name in a specific company This handles both exact and
     * slug-based name matching
     *
     * @param farmName Name of the farm (can be display name or slug format)
     * @param companyId Company ID
     * @return Optional Farm if found
     */
    @Transactional(readOnly = true)
    public Optional<Farm> getFarmByName(String farmName, String companyId) {
        System.out.println("FarmService: Finding farm by name: " + farmName + " in company: " + companyId);

        if (farmName == null || farmName.trim().isEmpty()) {
            return Optional.empty();
        }

        // Get all farms for this company
        List<Farm> companyFarms = getFarmsByCompanyId(companyId);

        // Normalize the search term
        String normalizedSearch = farmName.toLowerCase().replace("-", " ").trim();

        // First try exact name match
        for (Farm farm : companyFarms) {
            String normalizedName = farm.getName().toLowerCase().trim();
            String normalizedDisplayName = farm.getDisplayName() != null
                    ? farm.getDisplayName().toLowerCase().trim() : "";

            if (normalizedName.equals(normalizedSearch) || normalizedDisplayName.equals(normalizedSearch)) {
                System.out.println("FarmService: Found exact match for farm: " + farm.getName());
                return Optional.of(farm);
            }
        }

        // Try slug-based match
        for (Farm farm : companyFarms) {
            String nameSlug = farm.getName().toLowerCase().replace(" ", "-");
            String displayNameSlug = farm.getDisplayName() != null
                    ? farm.getDisplayName().toLowerCase().replace(" ", "-") : "";

            if (nameSlug.equals(farmName.toLowerCase()) || displayNameSlug.equals(farmName.toLowerCase())) {
                System.out.println("FarmService: Found slug match for farm: " + farm.getName());
                return Optional.of(farm);
            }
        }

        // If still not found, try partial match
        for (Farm farm : companyFarms) {
            String normalizedName = farm.getName().toLowerCase().trim();

            if (normalizedName.contains(normalizedSearch) || normalizedSearch.contains(normalizedName)) {
                System.out.println("FarmService: Found partial match for farm: " + farm.getName());
                return Optional.of(farm);
            }
        }

        System.out.println("FarmService: No farm found with name: " + farmName);
        return Optional.empty();
    }

    /**
     * Get all farms that are temperature sources for a company
     */
    public List<Farm> getTempSourceFarms(String companyId) {
        logger.info(String.format("🌡️ Fetching temperature source farms for company %s", companyId));
        List<Farm> companyFarms = getFarmsByCompanyId(companyId);
        return companyFarms.stream()
                .filter(Farm::getIsTempSource)
                .collect(Collectors.toList());
    }

    /**
     * Validate farm name
     */
    public ValidationResult validateFarmName(String name, String companyId) {
        logger.info(String.format("✔️ Validating farm name: %s for company: %s", name, companyId));

        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Farm name is required");
        }

        // Check min length (3 characters as per Next.js app)
        if (name.length() < 3) {
            return new ValidationResult(false, "Farm name must be at least 3 characters");
        }

        // Check max length (20 characters as per Next.js app)
        if (name.length() > 20) {
            return new ValidationResult(false, "Farm name cannot be more than 20 characters");
        }

        List<Farm> companyFarms = getFarmsByCompanyId(companyId);
        boolean exists = companyFarms.stream()
                .anyMatch(farm -> name.trim().equalsIgnoreCase(farm.getName()));

        return new ValidationResult(!exists, exists ? "Farm name already exists" : null);
    }

    /**
     * Validate farm display name
     */
    public ValidationResult validateDisplayName(String displayName, String companyId) {
        logger.info(String.format("✔️ Validating farm display name: %s for company: %s", displayName, companyId));

        // If display name is null or empty, it's valid since it's optional
        if (displayName == null || displayName.trim().isEmpty()) {
            return new ValidationResult(true, null);
        }

        // Check max length (6 characters as per Next.js app)
        if (displayName.length() > 6) {
            return new ValidationResult(false, "Display Name cannot be more than 6 characters");
        }

        // Check uniqueness
        List<Farm> companyFarms = getFarmsByCompanyId(companyId);
        boolean exists = companyFarms.stream()
                .anyMatch(farm -> displayName.trim().equalsIgnoreCase(farm.getDisplayName()));

        return new ValidationResult(!exists, exists ? "Display name already exists" : null);
    }
}
