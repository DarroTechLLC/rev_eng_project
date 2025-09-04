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
@RequestMapping({"/api/charts", "/align/api/charts"})
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

    @PostMapping("/company/population-timeline")
    public ResponseEntity<Map<String, Object>> getCompanyPopulationTimeline(@RequestBody CompanyDateRangeRequest request) {
        logger.info("📊 Processing company population timeline - companyId: {}, from: {}, to: {}",
                request.getCompany_id(), request.getFrom(), request.getTo());

        try {
            // Get company population data from service
            List<Map<String, Object>> populationData = chartService.getCompanyPopulationTimeline(
                    request.getCompany_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} company population records", populationData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", populationData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing company population timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/company/population-forecast-timeline")
    public ResponseEntity<Map<String, Object>> getCompanyPopulationForecastTimeline(@RequestBody CompanyDateRangeRequest request) {
        logger.info("📊 Processing company population forecast timeline - companyId: {}, from: {}, to: {}",
                request.getCompany_id(), request.getFrom(), request.getTo());

        try {
            // Get company population forecast data from service
            List<Map<String, Object>> forecastData = chartService.getCompanyPopulationForecastTimeline(
                    request.getCompany_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} company population forecast records", forecastData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", forecastData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing company population forecast timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/company/population-budget-timeline")
    public ResponseEntity<Map<String, Object>> getCompanyPopulationBudgetTimeline(@RequestBody CompanyDateRangeRequest request) {
        logger.info("📊 Processing company population budget timeline - companyId: {}, from: {}, to: {}",
                request.getCompany_id(), request.getFrom(), request.getTo());

        try {
            // Get company population budget data from service
            List<Map<String, Object>> budgetData = chartService.getCompanyPopulationBudgetTimeline(
                    request.getCompany_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} company population budget records", budgetData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", budgetData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing company population budget timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/single-farm/meter-monthly-timeline")
    public ResponseEntity<Map<String, Object>> getSingleFarmMeterMonthlyTimeline(@RequestBody FarmDateRangeRequest request) {
        logger.info("📊 Processing single farm meter monthly timeline - farmId: {}, from: {}, to: {}",
                request.getFarm_id(), request.getFrom(), request.getTo());

        try {
            // Get monthly meter data from service
            List<Map<String, Object>> monthlyData = chartService.getSingleFarmMeterMonthlyTimeline(
                    request.getFarm_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} monthly meter records", monthlyData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", monthlyData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing single farm meter monthly timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/single-farm/production-forecast-timeline")
    public ResponseEntity<Map<String, Object>> getSingleFarmProductionForecastTimeline(@RequestBody FarmDateRangeRequest request) {
        logger.info("📊 Processing single farm production forecast timeline - farmId: {}, from: {}, to: {}",
                request.getFarm_id(), request.getFrom(), request.getTo());

        try {
            // Get production forecast data from service
            List<Map<String, Object>> forecastData = chartService.getSingleFarmProductionForecastTimeline(
                    request.getFarm_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} production forecast records", forecastData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", forecastData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing single farm production forecast timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/single-farm/production-budget-timeline")
    public ResponseEntity<Map<String, Object>> getSingleFarmProductionBudgetTimeline(@RequestBody FarmDateRangeRequest request) {
        logger.info("📊 Processing single farm production budget timeline - farmId: {}, from: {}, to: {}",
                request.getFarm_id(), request.getFrom(), request.getTo());

        try {
            // Get production budget data from service
            List<Map<String, Object>> budgetData = chartService.getSingleFarmProductionBudgetTimeline(
                    request.getFarm_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} production budget records", budgetData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", budgetData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing single farm production budget timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/single-farm/population-timeline")
    public ResponseEntity<Map<String, Object>> getSingleFarmPopulationTimeline(@RequestBody FarmDateRangeRequest request) {
        logger.info("📊 Processing single farm population timeline - farmId: {}, from: {}, to: {}",
                request.getFarm_id(), request.getFrom(), request.getTo());

        try {
            // Get population data from service
            List<Map<String, Object>> populationData = chartService.getSingleFarmPopulationTimeline(
                    request.getFarm_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} population records", populationData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", populationData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing single farm population timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/single-farm/population-forecast-timeline")
    public ResponseEntity<Map<String, Object>> getSingleFarmPopulationForecastTimeline(@RequestBody FarmDateRangeRequest request) {
        logger.info("📊 Processing single farm population forecast timeline - farmId: {}, from: {}, to: {}",
                request.getFarm_id(), request.getFrom(), request.getTo());

        try {
            // Get population forecast data from service
            List<Map<String, Object>> forecastData = chartService.getSingleFarmPopulationForecastTimeline(
                    request.getFarm_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} population forecast records", forecastData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", forecastData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing single farm population forecast timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/single-farm/population-budget-timeline")
    public ResponseEntity<Map<String, Object>> getSingleFarmPopulationBudgetTimeline(@RequestBody FarmDateRangeRequest request) {
        logger.info("📊 Processing single farm population budget timeline - farmId: {}, from: {}, to: {}",
                request.getFarm_id(), request.getFrom(), request.getTo());

        try {
            // Get population budget data from service
            List<Map<String, Object>> budgetData = chartService.getSingleFarmPopulationBudgetTimeline(
                    request.getFarm_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} population budget records", budgetData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", budgetData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing single farm population budget timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/single-farm/population-52week-timeline")
    public ResponseEntity<Map<String, Object>> getSingleFarmPopulation52WeekTimeline(@RequestBody FarmDateRangeRequest request) {
        logger.info("📊 Processing single farm population 52-week timeline - farmId: {}, from: {}, to: {}",
                request.getFarm_id(), request.getFrom(), request.getTo());

        try {
            // Get population 52-week data from service
            List<Map<String, Object>> populationData = chartService.getSingleFarmPopulation52WeekTimeline(
                    request.getFarm_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} population 52-week records", populationData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", populationData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing single farm population 52-week timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/single-farm/lagoon-timeline")
    public ResponseEntity<Map<String, Object>> getSingleFarmLagoonTimeline(@RequestBody FarmDateRangeRequest request) {
        logger.info("📊 Processing single farm lagoon timeline - farmId: {}, from: {}, to: {}",
                request.getFarm_id(), request.getFrom(), request.getTo());

        try {
            // Get lagoon levels data from service
            List<Map<String, Object>> lagoonData = chartService.getSingleFarmLagoonLevels(
                    request.getFarm_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} lagoon level records", lagoonData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", lagoonData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing single farm lagoon timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/single-farm/mass-balance-timeline")
    public ResponseEntity<Map<String, Object>> getSingleFarmMassBalanceTimeline(@RequestBody FarmDateRangeRequest request) {
        logger.info("📊 Processing single farm mass balance timeline - farmId: {}, from: {}, to: {}",
                request.getFarm_id(), request.getFrom(), request.getTo());

        try {
            // Get mass balance data from service
            List<Map<String, Object>> massBalanceData = chartService.getSingleFarmMassBalanceTimeline(
                    request.getFarm_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} mass balance records", massBalanceData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", massBalanceData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing single farm mass balance timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/single-farm/ci-scores")
    public ResponseEntity<Map<String, Object>> getSingleFarmCiScores(@RequestBody FarmDateRangeRequest request) {
        logger.info("📊 Processing single farm CI scores - farmId: {}", request.getFarm_id());

        try {
            // For CI scores, we need to get the company ID for the farm
            // In a real implementation, we would look up the company ID for the farm
            // For now, we'll use a placeholder company ID
            String companyId = "placeholder-company-id";

            // Get CI scores data from service
            List<Map<String, Object>> ciScoresData = chartService.getMultiFarmCiScoresCombinedTable(companyId);

            logger.info("📈 Found {} CI scores records", ciScoresData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", ciScoresData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing single farm CI scores: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/single-farm/methane-recovery-timeline")
    public ResponseEntity<Map<String, Object>> getSingleFarmMethaneRecoveryTimeline(@RequestBody FarmDateRangeRequest request) {
        logger.info("📊 Processing single farm methane recovery timeline - farmId: {}, from: {}, to: {}",
                request.getFarm_id(), request.getFrom(), request.getTo());

        try {
            // Get methane recovery data from service
            List<Map<String, Object>> methaneRecoveryData = chartService.getSingleFarmCh4RecoveryTimeline(
                    request.getFarm_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} methane recovery records", methaneRecoveryData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", methaneRecoveryData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing single farm methane recovery timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/single-farm/temperature-timeline")
    public ResponseEntity<Map<String, Object>> getSingleFarmTemperatureTimeline(@RequestBody FarmDateRangeRequest request) {
        logger.info("📊 Processing single farm temperature timeline - farmId: {}, from: {}, to: {}",
                request.getFarm_id(), request.getFrom(), request.getTo());

        try {
            // Get temperature data from service
            List<Map<String, Object>> temperatureData = chartService.getSingleFarmTemperatureTimeline(
                    request.getFarm_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} temperature records", temperatureData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", temperatureData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing single farm temperature timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/market/market-prices-monthly-timeline")
    public ResponseEntity<Map<String, Object>> getMarketPricesMonthlyTimeline(@RequestBody DateRangeRequest request) {
        logger.info("📊 Processing market prices monthly timeline - from: {}, to: {}", 
                request.getFrom(), request.getTo());

        try {
            // Parse date strings to LocalDate
            LocalDate fromDate = request.getFrom();
            LocalDate toDate = request.getTo();

            // Get market prices data from service
            List<Map<String, Object>> pricesData = chartService.getMarketPricesMonthlyTimeline(
                    fromDate, 
                    toDate
            );

            logger.info("📈 Found {} monthly market price records", pricesData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", pricesData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing market prices monthly timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/market/market-prices-daily-timeline")
    public ResponseEntity<Map<String, Object>> getMarketPricesDailyTimeline(@RequestBody DateRangeRequest request) {
        logger.info("📊 Processing market prices daily timeline - from: {}, to: {}", 
                request.getFrom(), request.getTo());

        try {
            // Parse date strings to LocalDate
            LocalDate fromDate = request.getFrom();
            LocalDate toDate = request.getTo();

            // Get market prices data from service
            List<Map<String, Object>> pricesData = chartService.getMarketPricesDailyTimeline(
                    fromDate, 
                    toDate
            );

            logger.info("📈 Found {} daily market price records", pricesData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", pricesData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing market prices daily timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/company/production-timeline")
    public ResponseEntity<Map<String, Object>> getCompanyProductionTimeline(@RequestBody CompanyDateRangeRequest request) {
        logger.info("📊 Processing company production timeline - companyId: {}, from: {}, to: {}", 
                request.getCompany_id(), request.getFrom(), request.getTo());

        try {
            // Get production data from service
            List<Map<String, Object>> productionData = chartService.getCompanyProductionTimeline(
                    request.getCompany_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Found {} production records", productionData.size());

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", productionData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing company production timeline: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/multi-farm/production-budget-summary")
    public ResponseEntity<Map<String, Object>> getProductionBudgetSummary(@RequestBody CompanyDateRangeRequest request) {
        logger.info("📊 Processing production budget summary - companyId: {}, from: {}, to: {}", 
                request.getCompany_id(), request.getFrom(), request.getTo());

        try {
            // Get production vs budget data from service
            Map<String, Object> budgetData = chartService.getProductionVsBudgetByFarm(
                    request.getCompany_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Successfully retrieved production budget summary");

            return ResponseEntity.ok(budgetData);
        } catch (Exception e) {
            logger.error("❌ Error processing production budget summary: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/company/production-budget-summary")
    public ResponseEntity<Map<String, Object>> getCompanyProductionBudgetSummary(@RequestBody CompanyDateRangeRequest request) {
        logger.info("📊 Processing company production budget summary - companyId: {}, from: {}, to: {}", 
                request.getCompany_id(), request.getFrom(), request.getTo());

        try {
            // Get company production budget summary data from service
            List<Map<String, Object>> budgetData = chartService.getCompanyProductionBudgetSummary(
                    request.getCompany_id(),
                    request.getFrom(),
                    request.getTo()
            );

            logger.info("📈 Successfully retrieved company production budget summary");

            // Create response with data array
            Map<String, Object> response = new HashMap<>();
            response.put("data", budgetData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing company production budget summary: {}", e.getMessage(), e);

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }
}
