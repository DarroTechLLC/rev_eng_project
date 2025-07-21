package com.darro_tech.revengproject.services;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.repositories.FarmRepository;

import jakarta.servlet.http.HttpSession;

/**
 * Service for managing farm selection for users
 */
@Service
public class FarmSelectionService {

    private static final Logger logger = Logger.getLogger(FarmSelectionService.class.getName());
    private static final String SESSION_FARM_KEY = "selectedFarmKey";

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private FarmService farmService;

    /**
     * Select a default farm for a user and store it in the session
     *
     * @param user The authenticated user
     * @param companyId The ID of the current company
     * @param session The HTTP session
     * @return true if a farm was selected, false otherwise
     */
    public boolean selectDefaultFarmForUser(User user, String companyId, HttpSession session) {
        if (user == null || companyId == null) {
            logger.warning("🚫 Cannot select default farm: user or companyId is null");
            return false;
        }

        logger.info("🔍 Selecting default farm for user: " + user.getUsername() + " in company: " + companyId);

        // Get all farms for the company
        List<Farm> companyFarms = farmService.getFarmsByCompanyId(companyId);

        if (companyFarms.isEmpty()) {
            logger.warning("⚠️ No farms available for company: " + companyId);
            return false;
        }

        // Sort farms alphabetically for consistent selection
        companyFarms.sort(Comparator.comparing(Farm::getName));

        // Get the first farm
        Farm firstFarm = companyFarms.get(0);

        // Store the selected farm ID in session
        session.setAttribute(SESSION_FARM_KEY, firstFarm.getId());
        logger.info("✅ Auto-selected farm: " + firstFarm.getName() + " (ID: " + firstFarm.getId() + ") for user: " + user.getUsername());

        return true;
    }

    /**
     * Select a specific farm for the user if it exists in the current company
     *
     * @param user The authenticated user
     * @param farmId The ID of the farm to select
     * @param companyId The ID of the current company
     * @param session The HTTP session
     * @return true if the farm was selected, false otherwise
     */
    public boolean selectFarmForUser(User user, String farmId, String companyId, HttpSession session) {
        if (user == null || farmId == null || companyId == null) {
            logger.warning("🚫 Cannot select farm: user, farmId, or companyId is null");
            return false;
        }

        // Check if farm exists
        Optional<Farm> farmOpt = farmRepository.findById(farmId);
        if (farmOpt.isEmpty()) {
            logger.warning("🔒 Farm ID: " + farmId + " does not exist");
            return false;
        }

        // Check if farm belongs to the current company
        List<Farm> companyFarms = farmService.getFarmsByCompanyId(companyId);
        boolean farmBelongsToCompany = companyFarms.stream()
                .anyMatch(f -> f.getId().equals(farmId));

        if (!farmBelongsToCompany) {
            logger.warning("🔒 Farm ID: " + farmId + " does not belong to company: " + companyId);
            return false;
        }

        // Store the selected farm ID in session
        session.setAttribute(SESSION_FARM_KEY, farmId);
        logger.info("✅ Manually selected farm: " + farmOpt.get().getName() + " (ID: " + farmId + ") for user: " + user.getUsername());

        return true;
    }

    /**
     * Get the currently selected farm ID from the session
     *
     * @param session The HTTP session
     * @return The selected farm ID, or null if none is selected
     */
    public String getSelectedFarmId(HttpSession session) {
        return (String) session.getAttribute(SESSION_FARM_KEY);
    }

    /**
     * Select a specific farm if it exists in the current company
     * This version doesn't require a user and is used by the API
     *
     * @param farmId The ID of the farm to select
     * @param companyId The ID of the current company
     * @param session The HTTP session
     * @return true if the farm was selected, false otherwise
     */
    public boolean selectFarm(String farmId, String companyId, HttpSession session) {
        if (farmId == null || companyId == null) {
            logger.warning("🚫 Cannot select farm: farmId or companyId is null");
            return false;
        }

        // Check if farm exists
        Optional<Farm> farmOpt = farmRepository.findById(farmId);
        if (farmOpt.isEmpty()) {
            logger.warning("🔒 Farm ID: " + farmId + " does not exist");
            return false;
        }

        // Check if farm belongs to the current company
        List<Farm> companyFarms = farmService.getFarmsByCompanyId(companyId);
        boolean farmBelongsToCompany = companyFarms.stream()
                .anyMatch(f -> f.getId().equals(farmId));

        if (!farmBelongsToCompany) {
            logger.warning("🔒 Farm ID: " + farmId + " does not belong to company: " + companyId);
            return false;
        }

        // Store the selected farm ID in session
        session.setAttribute(SESSION_FARM_KEY, farmId);
        logger.info("✅ Selected farm: " + farmOpt.get().getName() + " (ID: " + farmId + ")");

        return true;
    }
}
