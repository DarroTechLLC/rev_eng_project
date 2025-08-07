package com.darro_tech.revengproject.controllers.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.darro_tech.revengproject.dto.ApiResponse;
import com.darro_tech.revengproject.dto.ListActiveAlertsRequest;
import com.darro_tech.revengproject.dto.WebsiteAlertDTO;
import com.darro_tech.revengproject.services.WebsiteAlertService;

/**
 * Public API controller for website alerts
 */
@RestController
@RequestMapping("/api/website-alerts")
public class WebsiteAlertPublicApiController {

    private static final Logger logger = LoggerFactory.getLogger(WebsiteAlertPublicApiController.class);
    private final WebsiteAlertService websiteAlertService;

    @Autowired
    public WebsiteAlertPublicApiController(WebsiteAlertService websiteAlertService) {
        this.websiteAlertService = websiteAlertService;
    }

    /**
     * Get active alerts for a company
     *
     * @param request Request with company ID
     * @return List of active alerts for the company
     */
    @PostMapping("/list-active")
    public ResponseEntity<ApiResponse<List<WebsiteAlertDTO>>> listActiveAlerts(@RequestBody ListActiveAlertsRequest request) {
        String companyId = request.getCompanyId();
        if (companyId == null || companyId.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("No company ID provided"));
        }

        logger.info("📢 API - List active alerts for company ID: {}", companyId);
        try {
            List<WebsiteAlertDTO> alerts = websiteAlertService.getActiveAlertsByCompanyId(companyId);

            // Debug log for date values
            for (WebsiteAlertDTO alert : alerts) {
                logger.info("[DEBUG_LOG] Active Alert ID: {}, createdAt: {}, updatedAt: {}", 
                    alert.getId(), alert.getCreatedAt(), alert.getUpdatedAt());
            }

            return ResponseEntity.ok(ApiResponse.success(alerts));
        } catch (Exception e) {
            logger.error("📢 Error listing active alerts for company ID: {}", companyId, e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}
