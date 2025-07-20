package com.darro_tech.revengproject.controllers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.darro_tech.revengproject.models.dto.AnomalyDTO;
import com.darro_tech.revengproject.models.dto.ForecastDTO;
import com.darro_tech.revengproject.models.dto.TrendDTO;
import com.darro_tech.revengproject.models.dto.TimeSeriesPointDTO;
import com.darro_tech.revengproject.services.AnomalyDetectionService;
import com.darro_tech.revengproject.services.ForecastingService;
import com.darro_tech.revengproject.services.TrendAnalysisService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

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

        logger.info("[DEBUG_LOG] CH4 Recovery Statistical Anomaly Detection request - farmId: {}, startDate: {}, endDate: {}",
                farmId, startDate, endDate);

        try {
            List<AnomalyDTO> anomalies = anomalyService.detectCh4RecoveryAnomalies(farmId, startDate, endDate);

            logger.info("[DEBUG_LOG] CH4 Recovery Statistical Anomaly Detection found {} anomalies",
                    anomalies != null ? anomalies.size() : 0);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("anomalies", anomalies);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[DEBUG_LOG] Error in CH4 Recovery Statistical Anomaly Detection", e);
            throw e;
        }
    }

    @GetMapping("/anomalies/temperature")
    public ResponseEntity<?> getTemperatureAnomalies(
            @RequestParam String farmId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        logger.info("[DEBUG_LOG] Temperature Statistical Anomaly Detection request - farmId: {}, startDate: {}, endDate: {}",
                farmId, startDate, endDate);

        try {
            List<AnomalyDTO> anomalies = anomalyService.detectTemperatureAnomalies(farmId, startDate, endDate);

            logger.info("[DEBUG_LOG] Temperature Statistical Anomaly Detection found {} anomalies",
                    anomalies != null ? anomalies.size() : 0);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("anomalies", anomalies);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[DEBUG_LOG] Error in Temperature Statistical Anomaly Detection", e);
            throw e;
        }
    }

    @GetMapping("/anomalies/mass-balance")
    public ResponseEntity<?> getMassBalanceAnomalies(
            @RequestParam String farmId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        logger.info("[DEBUG_LOG] Mass Balance Statistical Anomaly Detection request - farmId: {}, startDate: {}, endDate: {}",
                farmId, startDate, endDate);

        try {
            List<AnomalyDTO> anomalies = anomalyService.detectMassBalanceAnomalies(farmId, startDate, endDate);

            logger.info("[DEBUG_LOG] Mass Balance Statistical Anomaly Detection found {} anomalies",
                    anomalies != null ? anomalies.size() : 0);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("anomalies", anomalies);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[DEBUG_LOG] Error in Mass Balance Statistical Anomaly Detection", e);
            throw e;
        }
    }

    @GetMapping("/trends/ch4-recovery")
    public ResponseEntity<?> getCh4RecoveryTrend(
            @RequestParam String farmId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "linear") String trendType) {

        logger.info("[DEBUG_LOG] CH4 Recovery Trend Analysis request - farmId: {}, startDate: {}, endDate: {}, trendType: {}",
                farmId, startDate, endDate, trendType);

        try {
            TrendDTO trend = trendService.analyzeCh4RecoveryTrend(farmId, startDate, endDate);

            logger.info("[DEBUG_LOG] CH4 Recovery Trend Analysis completed with R²: {}",
                    trend != null ? trend.getR2Value() : "N/A");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("trend", trend);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[DEBUG_LOG] Error in CH4 Recovery Trend Analysis", e);
            throw e;
        }
    }

    @GetMapping("/forecasts/ch4-recovery")
    public ResponseEntity<?> getCh4RecoveryForecast(
            @RequestParam String farmId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "30") int forecastDays,
            @RequestParam(defaultValue = "0.7") double confidenceLevel) {

        logger.info("[DEBUG_LOG] CH4 Recovery Production Forecasting request - farmId: {}, startDate: {}, endDate: {}, forecastDays: {}, confidenceLevel: {}",
                farmId, startDate, endDate, forecastDays, confidenceLevel);

        try {
            ForecastDTO forecast = forecastService.forecastCh4Recovery(farmId, startDate, endDate);

            logger.info("[DEBUG_LOG] CH4 Recovery Production Forecasting completed with {} forecast points",
                    forecast != null && forecast.getForecastData() != null ? forecast.getForecastData().size() : 0);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("forecast", forecast);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[DEBUG_LOG] Error in CH4 Recovery Production Forecasting", e);
            throw e;
        }
    }

    // Test endpoints for development
    @GetMapping("/test")
    public ResponseEntity<?> testAnalyticsServices() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Analytics services are running");
        response.put("services", List.of("Statistical Anomaly Detection", "Trend Analysis", "Production Forecasting"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-anomalies")
    public ResponseEntity<?> testAnomalies() {
        List<AnomalyDTO> mockAnomalies = new ArrayList<>();
        
        AnomalyDTO anomaly1 = new AnomalyDTO();
        anomaly1.setTimestamp(Instant.now().minus(2, ChronoUnit.DAYS));
        anomaly1.setValue(150.5);
        anomaly1.setSeverity("HIGH");
        anomaly1.setFarmId("test-farm-1");
        anomaly1.setFarmName("Test Farm 1");
        anomaly1.setMetricType("CH4 Recovery");
        anomaly1.setExpectedValue(1250.0);
        anomaly1.setDeviationPercent(-21.6);
        mockAnomalies.add(anomaly1);
        
        AnomalyDTO anomaly2 = new AnomalyDTO();
        anomaly2.setTimestamp(Instant.now().minus(1, ChronoUnit.DAYS));
        anomaly2.setValue(180.2);
        anomaly2.setSeverity("MEDIUM");
        anomaly2.setFarmId("test-farm-2");
        anomaly2.setFarmName("Test Farm 2");
        anomaly2.setMetricType("Temperature");
        anomaly2.setExpectedValue(72.0);
        anomaly2.setDeviationPercent(18.1);
        mockAnomalies.add(anomaly2);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("anomalies", mockAnomalies);
        response.put("message", "Mock statistical anomaly detection data");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-trends")
    public ResponseEntity<?> testTrends() {
        TrendDTO mockTrend = new TrendDTO();
        mockTrend.setDirection("INCREASING");
        mockTrend.setSlope(2.5);
        mockTrend.setR2Value(0.85);
        mockTrend.setInterpretation("Strong upward trend detected with 85% correlation");
        mockTrend.setFarmId("test-farm-1");
        mockTrend.setFarmName("Test Farm 1");
        mockTrend.setMetricType("CH4 Recovery");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("trend", mockTrend);
        response.put("message", "Mock trend analysis data");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-forecasts")
    public ResponseEntity<?> testForecasts() {
        ForecastDTO mockForecast = new ForecastDTO();
        mockForecast.setConfidenceLevel(0.7);
        mockForecast.setFarmId("test-farm-1");
        mockForecast.setFarmName("Test Farm 1");
        mockForecast.setMetricType("CH4 Recovery");
        
        List<TimeSeriesPointDTO> forecastData = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            TimeSeriesPointDTO point = new TimeSeriesPointDTO();
            point.setTimestamp(Instant.now().plus(i, ChronoUnit.DAYS));
            point.setValue(165.0 + (i * 0.5)); // Simple linear increase
            forecastData.add(point);
        }
        mockForecast.setForecastData(forecastData);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("forecast", mockForecast);
        response.put("message", "Mock production forecasting data");
        
        return ResponseEntity.ok(response);
    }
}
