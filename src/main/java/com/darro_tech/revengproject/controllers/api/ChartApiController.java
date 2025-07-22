package com.darro_tech.revengproject.controllers.api;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.darro_tech.revengproject.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.darro_tech.revengproject.services.ChartService;

@RestController
@RequestMapping("/api/charts")
public class ChartApiController {

    private static final Logger logger = LoggerFactory.getLogger(ChartApiController.class);

    @Autowired
    private ChartService chartService;

    @PostMapping("/multi-farm/mtd-farm-volumes-for-years")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getMTDVolumeByFarmForYears(@RequestBody CompanyMTDVolumeRequest request) {
        logger.info("📊 Processing MTD farm volumes for years - companyId: {}, date: {}", request.getCompanyId(), request.getDate());
        try {
            Map<String, List<FarmVolumeData>> result = chartService.getMTDVolumeByFarmForYears(request.getCompanyId(), request.getDate());
            logger.info("📈 MTD farm volumes map size: {}", result.size());

            Map<String, List<Map<String, Object>>> convertedResult = new HashMap<>();
            for (Map.Entry<String, List<FarmVolumeData>> entry : result.entrySet()) {
                List<Map<String, Object>> convertedList = new ArrayList<>();
                for (FarmVolumeData data : entry.getValue()) {
                    Map<String, Object> convertedData = new HashMap<>();
                    convertedData.put("farmId", data.getFarm_id());
                    convertedData.put("farmName", data.getFarmName());
                    convertedData.put("volume", data.getVolume());
                    convertedList.add(convertedData);
                }
                convertedResult.put(entry.getKey(), convertedList);
            }
            return ResponseEntity.ok(convertedResult);
        } catch (Exception e) {
            logger.error("❌ Error processing MTD farm volumes for years: {}", e.getMessage(), e);
            return ResponseEntity.ok(new HashMap<>());
        }
    }

    @PostMapping("/multi-farm/farm-volumes-for-date")
    public ResponseEntity<Map<String, Object>> getMultiFarmVolumesForDate(@RequestBody ChartDateRequest request) {
        logger.info("📊 Processing multi-farm volumes request - companyId: {}, date: {}", request.getCompany_id(), request.getDate());

        try {
            // Parse the date string to LocalDate
            LocalDate date = LocalDate.parse(request.getDate());

            // Get volume data from service
            List<FarmVolumeData> volumeData = chartService.getDailyVolumeByFarmForDate(
                    request.getCompany_id(),
                    date
            );

            logger.info("📈 Found {} farm volume records", volumeData.size());

            // Format data for response
            List<Map<String, Object>> formattedData = new ArrayList<>();
            for (FarmVolumeData data : volumeData) {
                Map<String, Object> formatted = new HashMap<>();
                formatted.put("farm_id", data.getFarm_id());
                formatted.put("farm_name", data.getFarmName());
                formatted.put("volume", data.getVolume());
                formattedData.add(formatted);

                logger.info("🏠 Farm: {} ({}), Volume: {}", data.getFarmName(), data.getFarm_id(), data.getVolume());
            }

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", formattedData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing multi-farm volumes request: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/single-farm/volumes-for-date")
    public ResponseEntity<Map<String, Object>> getFarmVolumesForDate(@RequestBody CompanyDateRequest request) {
        logger.info("📊 Processing farm volumes request - companyId: {}, date: {}", request.getCompany_id(), request.getDate());

        try {
            List<FarmVolumeData> volumeData = chartService.getDailyVolumeByFarmForDate(
                    request.getCompany_id(),
                    request.getDate()
            );

            logger.info("📈 Found {} farm volume records", volumeData.size());

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

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }
    @PostMapping("/multi-farm/farm-volumes-for-range")
    public ResponseEntity<Map<String, Object>> getMultiFarmVolumesForRange(@RequestBody CompanyDateRangeRequest request) {
        logger.info("📊 Processing multi-farm volumes for range - companyId: {}, from: {}, to: {}",
                request.getCompany_id(), request.getFrom(), request.getTo());

        try {
            // Get volume data from service
            List<FarmVolumeData> volumeData = chartService.getVolumeByFarmForDateRange(
                    request.getCompany_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} farm volume records", volumeData.size());

            // Format data for response
            List<Map<String, Object>> formattedData = new ArrayList<>();
            for (FarmVolumeData data : volumeData) {
                Map<String, Object> formatted = new HashMap<>();
                formatted.put("farmId", data.getFarm_id());
                formatted.put("farmName", data.getFarmName());
                formatted.put("volume", data.getVolume());
                formattedData.add(formatted);

                logger.info("🏠 Farm: {} ({}), Volume: {}", data.getFarmName(), data.getFarm_id(), data.getVolume());
            }

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", formattedData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing multi-farm volumes for range: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/multi-farm/production-population-52week-timeline")
    public ResponseEntity<Map<String, Object>> getProductionPopulation52WeekTimeline(@RequestBody CompanyDateRangeRequest request) {
        logger.info("📊 Processing production population 52-week timeline - companyId: {}, from: {}, to: {}",
                request.getCompany_id(), request.getFrom(), request.getTo());

        try {
            // Get production population data from service
            List<Map<String, Object>> populationData = chartService.getProductionPopulationTimeline(
                    request.getCompany_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} production population records", populationData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", populationData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing production population 52-week timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }
}
