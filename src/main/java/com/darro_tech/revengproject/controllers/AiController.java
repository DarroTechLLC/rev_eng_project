package com.darro_tech.revengproject.controllers;

import com.darro_tech.revengproject.models.dto.AnomalyDTO;
import com.darro_tech.revengproject.models.dto.ForecastDTO;
import com.darro_tech.revengproject.models.dto.TrendDTO;
import com.darro_tech.revengproject.services.AnomalyDetectionService;
import com.darro_tech.revengproject.services.ForecastingService;
import com.darro_tech.revengproject.services.TrendAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    @Autowired
    private AnomalyDetectionService anomalyService;

    @Autowired
    private TrendAnalysisService trendService;

    @Autowired
    private ForecastingService forecastService;

    @GetMapping("/anomalies/ch4-recovery")
    public ResponseEntity<?> getCh4RecoveryAnomalies(
            @RequestParam String farmId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        logger.info("[DEBUG_LOG] CH4 Recovery Anomaly Detection request - farmId: {}, startDate: {}, endDate: {}", 
                farmId, startDate, endDate);

        try {
            List<AnomalyDTO> anomalies = anomalyService.detectCh4RecoveryAnomalies(farmId, startDate, endDate);

            logger.info("[DEBUG_LOG] CH4 Recovery Anomaly Detection found {} anomalies", 
                    anomalies != null ? anomalies.size() : 0);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("anomalies", anomalies);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[DEBUG_LOG] Error in CH4 Recovery Anomaly Detection", e);
            throw e;
        }
    }

    @GetMapping("/anomalies/temperature")
    public ResponseEntity<?> getTemperatureAnomalies(
            @RequestParam String farmId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        logger.info("[DEBUG_LOG] Temperature Anomaly Detection request - farmId: {}, startDate: {}, endDate: {}", 
                farmId, startDate, endDate);

        try {
            List<AnomalyDTO> anomalies = anomalyService.detectTemperatureAnomalies(farmId, startDate, endDate);

            logger.info("[DEBUG_LOG] Temperature Anomaly Detection found {} anomalies", 
                    anomalies != null ? anomalies.size() : 0);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("anomalies", anomalies);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[DEBUG_LOG] Error in Temperature Anomaly Detection", e);
            throw e;
        }
    }

    @GetMapping("/anomalies/mass-balance")
    public ResponseEntity<?> getMassBalanceAnomalies(
            @RequestParam String farmId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        logger.info("[DEBUG_LOG] Mass Balance Anomaly Detection request - farmId: {}, startDate: {}, endDate: {}", 
                farmId, startDate, endDate);

        try {
            // Implement mass balance anomaly detection similar to CH4 recovery
            // For now, return an empty list to prevent 404 errors
            List<AnomalyDTO> anomalies = new ArrayList<>();

            logger.info("[DEBUG_LOG] Mass Balance Anomaly Detection found {} anomalies", anomalies.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("anomalies", anomalies);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[DEBUG_LOG] Error in Mass Balance Anomaly Detection", e);
            throw e;
        }
    }

    @GetMapping("/trends/ch4-recovery")
    public ResponseEntity<?> getCh4RecoveryTrend(
            @RequestParam String farmId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        logger.info("[DEBUG_LOG] CH4 Recovery Trend Analysis request - farmId: {}, startDate: {}, endDate: {}", 
                farmId, startDate, endDate);

        try {
            TrendDTO trend = trendService.analyzeCh4RecoveryTrend(farmId, startDate, endDate);

            logger.info("[DEBUG_LOG] CH4 Recovery Trend Analysis completed - direction: {}, slope: {}, r2Value: {}", 
                    trend != null ? trend.getDirection() : "null", 
                    trend != null ? trend.getSlope() : "null", 
                    trend != null ? trend.getR2Value() : "null");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("trend", trend);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[DEBUG_LOG] Error in CH4 Recovery Trend Analysis", e);
            throw e;
        }
    }

    @GetMapping("/forecast/ch4-recovery")
    public ResponseEntity<?> forecastCh4Recovery(
            @RequestParam String farmId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        logger.info("[DEBUG_LOG] CH4 Recovery Forecast request - farmId: {}, startDate: {}, endDate: {}", 
                farmId, startDate, endDate);

        try {
            ForecastDTO forecast = forecastService.forecastCh4Recovery(farmId, startDate, endDate);

            logger.info("[DEBUG_LOG] CH4 Recovery Forecast completed - farmName: {}, historicalDataPoints: {}, forecastDataPoints: {}", 
                    forecast != null ? forecast.getFarmName() : "null", 
                    forecast != null && forecast.getHistoricalData() != null ? forecast.getHistoricalData().size() : 0, 
                    forecast != null && forecast.getForecastData() != null ? forecast.getForecastData().size() : 0);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("forecast", forecast);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[DEBUG_LOG] Error in CH4 Recovery Forecast", e);
            throw e;
        }
    }
}
