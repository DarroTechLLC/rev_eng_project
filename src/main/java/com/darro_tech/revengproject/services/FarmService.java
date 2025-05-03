package com.darro_tech.revengproject.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.CompanyFarm;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.repositories.CompanyFarmRepository;
import com.darro_tech.revengproject.repositories.CompanyRepository;
import com.darro_tech.revengproject.repositories.FarmRepository;

@Service
public class FarmService {

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
        System.out.println("FarmService: Getting farm by ID: " + id);
        return farmRepository.findById(id);
    }

    /**
     * Get farms for a specific company
     */
    @Transactional(readOnly = true)
    public List<Farm> getFarmsByCompanyId(String companyId) {
        System.out.println("FarmService: Getting farms for company ID: " + companyId);

        // First get CompanyFarm links
        List<CompanyFarm> companyFarms = companyFarmRepository.findByCompanyId(companyId);

        if (companyFarms.isEmpty()) {
            System.out.println("FarmService: No farms found for company ID: " + companyId);
            return new ArrayList<>();
        }

        // Then get Farm entities
        List<Farm> farms = new ArrayList<>();
        for (CompanyFarm cf : companyFarms) {
            // Get farm directly from CompanyFarm entity
            Farm farm = cf.getFarm();
            if (farm != null) {
                farms.add(farm);
            }
        }

        System.out.println("FarmService: Found " + farms.size() + " farms for company ID: " + companyId);
        return farms;
    }

    /**
     * Create a new farm
     */
    @Transactional
    public Farm createFarm(String name, String displayName) {
        System.out.println("FarmService: Creating farm: " + name);

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
        System.out.println("FarmService: Updating farm: " + id);

        Farm farm = farmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Farm not found"));

        farm.setName(name);
        farm.setDisplayName(displayName);
        farm.setTimestamp(Instant.now());

        return farmRepository.save(farm);
    }

    /**
     * Delete a farm
     */
    @Transactional
    public void deleteFarm(String id) {
        System.out.println("FarmService: Deleting farm: " + id);

        // First remove any company associations
        companyFarmRepository.deleteByFarmId(id);

        // Then delete the farm
        farmRepository.deleteById(id);
    }

    /**
     * Assign a farm to a company
     */
    @Transactional
    public void assignFarmToCompany(String farmId, String companyId) {
        System.out.println("FarmService: Assigning farm " + farmId + " to company " + companyId);

        // Check if association already exists
        if (companyFarmRepository.findByCompanyIdAndFarmId(companyId, farmId).isPresent()) {
            System.out.println("FarmService: Farm is already assigned to this company");
            return;
        }

        // Get the farm and company entities
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found"));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Create new association
        CompanyFarm companyFarm = new CompanyFarm();
        companyFarm.setId(new Random().nextInt(1000000)); // Generate a random integer ID
        companyFarm.setCompany(company);
        companyFarm.setFarm(farm);
        companyFarm.setTimestamp(Instant.now());

        companyFarmRepository.save(companyFarm);
    }

    /**
     * Update farm-company association
     */
    @Transactional
    public void updateFarmCompanyAssociation(String farmId, String companyId) {
        System.out.println("FarmService: Updating farm-company association for farm " + farmId);

        // First get current associations
        List<CompanyFarm> existingAssociations = companyFarmRepository.findByFarmId(farmId);

        // Get the company
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Check if there's already an association with this company
        boolean found = false;
        for (CompanyFarm cf : existingAssociations) {
            if (cf.getCompany().getId().equals(companyId)) {
                found = true;
                break;
            }
        }

        // If there's already a correct association, return
        if (found && existingAssociations.size() == 1) {
            System.out.println("FarmService: No changes needed to farm-company association");
            return;
        }

        // Remove existing associations
        for (CompanyFarm cf : existingAssociations) {
            companyFarmRepository.delete(cf);
        }

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
     * Find a farm by name in a specific company
     * This handles both exact and slug-based name matching
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
            String normalizedDisplayName = farm.getDisplayName() != null ? 
                farm.getDisplayName().toLowerCase().trim() : "";
                
            if (normalizedName.equals(normalizedSearch) || normalizedDisplayName.equals(normalizedSearch)) {
                System.out.println("FarmService: Found exact match for farm: " + farm.getName());
                return Optional.of(farm);
            }
        }
        
        // Try slug-based match
        for (Farm farm : companyFarms) {
            String nameSlug = farm.getName().toLowerCase().replace(" ", "-");
            String displayNameSlug = farm.getDisplayName() != null ? 
                farm.getDisplayName().toLowerCase().replace(" ", "-") : "";
                
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
}
