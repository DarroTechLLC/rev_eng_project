package com.darro_tech.revengproject.controllers.api;

import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.dto.CompanyDateRequest;
import com.darro_tech.revengproject.dto.FarmVolumeData;
import com.darro_tech.revengproject.services.ChartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/charts")
public class ChartApiController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ChartApiController.class);

    @Autowired
    private ChartService chartService;

    @PostMapping("/multi-farm/farm-volumes-for-date")
    public ResponseEntity<Map<String, Object>> getFarmVolumesForDate(@RequestBody CompanyDateRequest request) {
        logger.info("📊 Processing farm volumes request - companyId: {}, date: {}", 
            request.getCompany_id(), request.getDate());

        try {
            List<FarmVolumeData> volumeData = chartService.getDailyVolumeByFarmForDate(
                request.getCompany_id(), 
                request.getDate()
            );

            logger.info("📈 Found {} farm volume records", volumeData.size());

            // Convert the existing data to the format expected by NextJS
            List<Map<String, Object>> formattedData = new ArrayList<>();
            for (FarmVolumeData data : volumeData) {
                Map<String, Object> formatted = new HashMap<>();
                formatted.put("farm_id", data.getFarm_id());
                formatted.put("farm_name", data.getFarmName());
                formatted.put("volume", data.getVolume());
                formattedData.add(formatted);

                logger.info("🏠 Farm: {} ({}), Volume: {}", data.getFarmName(), data.getFarm_id(), data.getVolume());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", formattedData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing request: {}", e.getMessage(), e);

            // Return empty data with error flag for frontend debugging
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }
}
