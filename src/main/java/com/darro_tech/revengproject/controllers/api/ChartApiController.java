package com.darro_tech.revengproject.controllers.api;

import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.dto.CompanyDateRequest;
import com.darro_tech.revengproject.dto.CompanyDateRangeRequest;
import com.darro_tech.revengproject.dto.DateRangeRequest;
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

    @PostMapping("/multi-farm/farm-volumes-for-range")
    public ResponseEntity<Map<String, Object>> getFarmVolumesForRange(@RequestBody CompanyDateRangeRequest request) {
        logger.info("📊 Processing farm volumes range request - companyId: {}, from: {}, to: {}", 
            request.getCompany_id(), request.getFrom(), request.getTo());

        try {
            List<FarmVolumeData> volumeData = chartService.getVolumeByFarmForDateRange(
                request.getCompany_id(), 
                request.getFrom(),
                request.getTo()
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

    @PostMapping("/multi-farm/production-population-52week-timeline")
    public ResponseEntity<Map<String, Object>> getProductionPopulationTimeline(@RequestBody CompanyDateRangeRequest request) {
        logger.info("📊 Processing production population timeline request - companyId: {}, from: {}, to: {}", 
            request.getCompany_id(), request.getFrom(), request.getTo());

        try {
            List<Map<String, Object>> populationData = chartService.getProductionPopulationTimeline(
                request.getCompany_id(), 
                request.getFrom(),
                request.getTo()
            );

            logger.info("📈 Found {} production population records", populationData.size());

            Map<String, Object> response = new HashMap<>();
            response.put("data", populationData);

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

    @PostMapping("/company/population-timeline")
    public ResponseEntity<Map<String, Object>> getCompanyPopulationTimeline(@RequestBody CompanyDateRangeRequest request) {
        logger.info("📊 Processing company population timeline request - companyId: {}, from: {}, to: {}", 
            request.getCompany_id(), request.getFrom(), request.getTo());

        try {
            List<Map<String, Object>> populationData = chartService.getCompanyPopulationTimeline(
                request.getCompany_id(), 
                request.getFrom(),
                request.getTo()
            );

            logger.info("📈 Found {} company population records", populationData.size());

            Map<String, Object> response = new HashMap<>();
            response.put("data", populationData);

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

    @PostMapping("/company/population-forecast-timeline")
    public ResponseEntity<Map<String, Object>> getCompanyPopulationForecastTimeline(@RequestBody CompanyDateRangeRequest request) {
        logger.info("📊 Processing company population forecast timeline request - companyId: {}, from: {}, to: {}", 
            request.getCompany_id(), request.getFrom(), request.getTo());

        try {
            List<Map<String, Object>> forecastData = chartService.getCompanyPopulationForecastTimeline(
                request.getCompany_id(), 
                request.getFrom(),
                request.getTo()
            );

            logger.info("📈 Found {} company population forecast records", forecastData.size());

            Map<String, Object> response = new HashMap<>();
            response.put("data", forecastData);

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

    @PostMapping("/company/population-budget-timeline")
    public ResponseEntity<Map<String, Object>> getCompanyPopulationBudgetTimeline(@RequestBody CompanyDateRangeRequest request) {
        logger.info("📊 Processing company population budget timeline request - companyId: {}, from: {}, to: {}", 
            request.getCompany_id(), request.getFrom(), request.getTo());

        try {
            List<Map<String, Object>> budgetData = chartService.getCompanyPopulationBudgetTimeline(
                request.getCompany_id(), 
                request.getFrom(),
                request.getTo()
            );

            logger.info("📈 Found {} company population budget records", budgetData.size());

            Map<String, Object> response = new HashMap<>();
            response.put("data", budgetData);

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

    @PostMapping("/market/market-prices-monthly-timeline")
    public ResponseEntity<Map<String, Object>> getMarketPricesMonthlyTimeline(@RequestBody DateRangeRequest request) {
        logger.info("📊 Processing market prices monthly timeline request - from: {}, to: {}", 
            request.getFrom(), request.getTo());

        try {
            List<Map<String, Object>> pricesData = chartService.getMarketPricesMonthlyTimeline(
                request.getFrom(),
                request.getTo()
            );

            logger.info("📈 Found {} market prices monthly records", pricesData.size());

            Map<String, Object> response = new HashMap<>();
            response.put("data", pricesData);

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

    @PostMapping("/market/market-prices-daily-timeline")
    public ResponseEntity<Map<String, Object>> getMarketPricesDailyTimeline(@RequestBody DateRangeRequest request) {
        logger.info("📊 Processing market prices daily timeline request - from: {}, to: {}", 
            request.getFrom(), request.getTo());

        try {
            List<Map<String, Object>> pricesData = chartService.getMarketPricesDailyTimeline(
                request.getFrom(),
                request.getTo()
            );

            logger.info("📈 Found {} market prices daily records", pricesData.size());

            Map<String, Object> response = new HashMap<>();
            response.put("data", pricesData);

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
