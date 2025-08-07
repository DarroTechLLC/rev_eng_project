package com.darro_tech.revengproject.controllers.api.admin;

import com.darro_tech.revengproject.dto.ApiResponse;
import com.darro_tech.revengproject.dto.WebsiteAlertDTO;
import com.darro_tech.revengproject.dto.WebsiteAlertRequest;
import com.darro_tech.revengproject.services.WebsiteAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for website alerts administration
 */
@RestController
@RequestMapping("/api/admin/website-alerts")
public class WebsiteAlertApiController {

    private static final Logger logger = LoggerFactory.getLogger(WebsiteAlertApiController.class);
    private final WebsiteAlertService websiteAlertService;

    @Autowired
    public WebsiteAlertApiController(WebsiteAlertService websiteAlertService) {
        this.websiteAlertService = websiteAlertService;
    }

    /**
     * Get all website alerts
     *
     * @return List of all website alerts
     */
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<WebsiteAlertDTO>>> listAlerts() {
        logger.info("📢 API - List all website alerts");
        try {
            List<WebsiteAlertDTO> alerts = websiteAlertService.getAllAlerts();

            // Debug log for date values
            for (WebsiteAlertDTO alert : alerts) {
                logger.info("[DEBUG_LOG] Alert ID: {}, createdAt: {}, updatedAt: {}", 
                    alert.getId(), alert.getCreatedAt(), alert.getUpdatedAt());
            }

            return ResponseEntity.ok(ApiResponse.success(alerts));
        } catch (Exception e) {
            logger.error("📢 Error listing website alerts", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get a specific website alert by ID
     *
     * @param id The alert ID
     * @return The website alert data
     */
    @PostMapping("/get")
    public ResponseEntity<ApiResponse<WebsiteAlertDTO>> getAlert(@RequestBody(required = false) WebsiteAlertRequest request) {
        Integer id = request != null ? request.getId() : null;
        if (id == null) {
            return ResponseEntity.ok(ApiResponse.error("No ID provided"));
        }

        logger.info("📢 API - Get website alert by ID: {}", id);
        try {
            return websiteAlertService.getAlertById(id)
                    .map(alert -> {
                        // Debug log for date values
                        logger.info("[DEBUG_LOG] Get Alert ID: {}, createdAt: {}, updatedAt: {}", 
                            alert.getId(), alert.getCreatedAt(), alert.getUpdatedAt());
                        return ResponseEntity.ok(ApiResponse.success(alert));
                    })
                    .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Alert not found with ID: " + id)));
        } catch (Exception e) {
            logger.error("📢 Error getting website alert with ID: {}", id, e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Create or update a website alert
     *
     * @param request The alert data
     * @return The created/updated alert
     */
    @PostMapping("/upsert")
    public ResponseEntity<ApiResponse<WebsiteAlertDTO>> upsertAlert(@RequestBody WebsiteAlertRequest request) {
        logger.info("📢 API - Upsert website alert");
        try {
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("Message cannot be empty"));
            }

            WebsiteAlertDTO alert = websiteAlertService.upsertAlert(request);

            // Debug log for date values
            logger.info("[DEBUG_LOG] Upsert Alert ID: {}, createdAt: {}, updatedAt: {}", 
                alert.getId(), alert.getCreatedAt(), alert.getUpdatedAt());

            return ResponseEntity.ok(ApiResponse.success(alert));
        } catch (Exception e) {
            logger.error("📢 Error upserting website alert", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete a website alert
     *
     * @param request The alert ID to delete
     * @return Success/failure status
     */
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Boolean>> deleteAlert(@RequestBody WebsiteAlertRequest request) {
        Integer id = request != null ? request.getId() : null;
        if (id == null) {
            return ResponseEntity.ok(ApiResponse.error("No ID provided"));
        }

        logger.info("📢 API - Delete website alert with ID: {}", id);
        try {
            boolean deleted = websiteAlertService.deleteAlert(id);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success(true));
            } else {
                return ResponseEntity.ok(ApiResponse.error("Alert not found with ID: " + id));
            }
        } catch (Exception e) {
            logger.error("📢 Error deleting website alert with ID: {}", id, e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
} 
