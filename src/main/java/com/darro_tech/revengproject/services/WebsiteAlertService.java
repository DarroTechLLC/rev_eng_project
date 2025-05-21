package com.darro_tech.revengproject.services;

import java.util.List;
import java.util.Optional;

import com.darro_tech.revengproject.dto.WebsiteAlertDTO;
import com.darro_tech.revengproject.dto.WebsiteAlertRequest;

/**
 * Service interface for Website Alert operations
 */
public interface WebsiteAlertService {

    /**
     * Get all website alerts
     *
     * @return List of all website alerts
     */
    List<WebsiteAlertDTO> getAllAlerts();

    /**
     * Get a website alert by ID
     *
     * @param id The alert ID
     * @return Optional containing the alert if found
     */
    Optional<WebsiteAlertDTO> getAlertById(Integer id);

    /**
     * Create or update a website alert
     *
     * @param request The alert request data
     * @return The created/updated alert
     */
    WebsiteAlertDTO upsertAlert(WebsiteAlertRequest request);

    /**
     * Delete a website alert
     *
     * @param id The alert ID to delete
     * @return true if deleted, false if not found
     */
    boolean deleteAlert(Integer id);

    /**
     * Get all active alerts for a specific company
     *
     * @param companyId The company ID
     * @return List of active alerts for the company
     */
    List<WebsiteAlertDTO> getActiveAlertsByCompanyId(String companyId);
}
