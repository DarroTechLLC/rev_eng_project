package com.darro_tech.revengproject.services.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darro_tech.revengproject.dto.WebsiteAlertDTO;
import com.darro_tech.revengproject.dto.WebsiteAlertRequest;
import com.darro_tech.revengproject.models.WebsiteAlert;
import com.darro_tech.revengproject.repositories.WebsiteAlertRepository;
import com.darro_tech.revengproject.services.WebsiteAlertService;

@Service
public class WebsiteAlertServiceImpl implements WebsiteAlertService {

    private static final Logger logger = LoggerFactory.getLogger(WebsiteAlertServiceImpl.class);
    private final WebsiteAlertRepository websiteAlertRepository;

    @Autowired
    public WebsiteAlertServiceImpl(WebsiteAlertRepository websiteAlertRepository) {
        this.websiteAlertRepository = websiteAlertRepository;
    }

    @Override
    public List<WebsiteAlertDTO> getAllAlerts() {
        logger.info("📢 Fetching all website alerts");
        return websiteAlertRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WebsiteAlertDTO> getAlertById(Integer id) {
        logger.info("📢 Fetching website alert with ID: {}", id);
        return websiteAlertRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    public WebsiteAlertDTO upsertAlert(WebsiteAlertRequest request) {
        Integer id = request.getId();
        boolean isNew = id == null || id == 0;

        logger.info("📢 {} website alert with ID: {}", isNew ? "Creating" : "Updating", id);

        WebsiteAlert alert;
        if (isNew) {
            alert = new WebsiteAlert();
            // Don't set ID for new entities, let it be auto-generated
        } else {
            alert = websiteAlertRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Alert not found with ID: " + id));
            // For existing entities, keep the ID
            alert.setId(id);
        }

        // Set values from request
        alert.setMessage(request.getMessage());
        alert.setIsActive(request.getIsActive());

        // Set company IDs directly
        alert.setCompanyIds(request.getCompanyIds());

        // Set timestamps
        Instant now = Instant.now();
        if (isNew) {
            alert.setCreatedAt(now);
        }
        alert.setUpdatedAt(now);

        // Save to database
        WebsiteAlert savedAlert = websiteAlertRepository.save(alert);
        logger.info("📢 Successfully saved website alert with ID: {}", savedAlert.getId());

        return convertToDTO(savedAlert);
    }

    @Override
    public boolean deleteAlert(Integer id) {
        logger.info("📢 Deleting website alert with ID: {}", id);
        if (websiteAlertRepository.existsById(id)) {
            websiteAlertRepository.deleteById(id);
            logger.info("📢 Successfully deleted website alert with ID: {}", id);
            return true;
        }
        logger.warn("📢 Attempted to delete non-existent website alert with ID: {}", id);
        return false;
    }

    @Override
    public List<WebsiteAlertDTO> getActiveAlertsByCompanyId(String companyId) {
        logger.info("📢 Fetching active alerts for company ID: {}", companyId);
        return websiteAlertRepository.findActiveAlertsByCompanyId(companyId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert a WebsiteAlert entity to a WebsiteAlertDTO
     *
     * @param alert The entity to convert
     * @return The converted DTO
     */
    private WebsiteAlertDTO convertToDTO(WebsiteAlert alert) {
        WebsiteAlertDTO dto = new WebsiteAlertDTO();
        dto.setId(alert.getId());
        dto.setMessage(alert.getMessage());
        dto.setIsActive(alert.getIsActive());
        dto.setCreatedAt(alert.getCreatedAt());
        dto.setUpdatedAt(alert.getUpdatedAt());

        // Set company IDs directly
        dto.setCompanyIds(alert.getCompanyIds());

        return dto;
    }
}
