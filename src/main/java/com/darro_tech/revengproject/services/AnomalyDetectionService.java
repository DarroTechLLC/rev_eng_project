package com.darro_tech.revengproject.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darro_tech.revengproject.models.Ch4Recovery;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.models.Temperature;
import com.darro_tech.revengproject.models.MassBalance;
import com.darro_tech.revengproject.models.dto.AnomalyDTO;
import com.darro_tech.revengproject.repositories.Ch4RecoveryRepository;
import com.darro_tech.revengproject.repositories.FarmRepository;
import com.darro_tech.revengproject.repositories.TemperatureRepository;
import com.darro_tech.revengproject.repositories.MassBalanceRepository;

@Service
public class AnomalyDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(AnomalyDetectionService.class);

    @Autowired
    private Ch4RecoveryRepository ch4RecoveryRepository;

    @Autowired
    private TemperatureRepository temperatureRepository;

    @Autowired
    private MassBalanceRepository massBalanceRepository;

    @Autowired
    private FarmRepository farmRepository;

    // Configurable threshold for what constitutes a "significant" change
    private static final double SIGNIFICANT_CHANGE_THRESHOLD = 15.0; // percent

    /**
     * Detects significant drops or increases in CH4 recovery values
     */
    public List<AnomalyDTO> detectCh4RecoveryAnomalies(String farmId, Instant startDate, Instant endDate) {
        System.out.println("🔍 Starting CH4 Recovery anomaly detection for farm: " + farmId + " from " + startDate + " to " + endDate);

        List<AnomalyDTO> anomalies = new ArrayList<>();

        // Get farm details
        Farm farm = farmRepository.findById(farmId).orElse(null);
        if (farm == null) {
            System.out.println("❌ Farm not found for ID: " + farmId);
            return anomalies;
        }

        // Get CH4 recovery data for the specified period
        List<Ch4Recovery> ch4Data = ch4RecoveryRepository.findByFarmIdAndTimestampBetweenOrderByTimestampAsc(
                farmId, startDate, endDate);

        System.out.println("📊 Retrieved " + ch4Data.size() + " CH4 recovery data points for analysis");

        if (ch4Data.size() < 2) {
            System.out.println("⚠️ Insufficient data for anomaly detection (need at least 2 points, got " + ch4Data.size() + ")");
            return anomalies; // Not enough data for anomaly detection
        }

        // Calculate baseline statistics (mean and standard deviation)
        DoubleSummaryStatistics stats = ch4Data.stream()
                .map(Ch4Recovery::getValue)
                .collect(Collectors.summarizingDouble(Double::doubleValue));

        double mean = stats.getAverage();
        double stdDev = calculateStandardDeviation(ch4Data.stream()
                .map(Ch4Recovery::getValue)
                .collect(Collectors.toList()), mean);

        System.out.println("📈 Statistical baseline - Mean: " + String.format("%.2f", mean) + ", StdDev: " + String.format("%.2f", stdDev));

        // Detect anomalies (values outside of 2 standard deviations)
        for (Ch4Recovery dataPoint : ch4Data) {
            double value = dataPoint.getValue();
            double zScore = (value - mean) / stdDev;

            if (Math.abs(zScore) > 2.0) {
                AnomalyDTO anomaly = new AnomalyDTO();
                anomaly.setFarmId(farmId);
                anomaly.setFarmName(farm.getName());
                anomaly.setMetricType("CH4 Recovery");
                anomaly.setValue(value);
                anomaly.setExpectedValue(mean);
                anomaly.setDeviationPercent((value - mean) / mean * 100);
                anomaly.setTimestamp(dataPoint.getTimestamp());

                // Determine severity
                if (Math.abs(zScore) > 3.0) {
                    anomaly.setSeverity("Critical");
                } else {
                    anomaly.setSeverity("Warning");
                }

                anomalies.add(anomaly);
                System.out.println("🚨 Anomaly detected - Value: " + String.format("%.2f", value) + ", Z-Score: " + String.format("%.2f", zScore) + ", Severity: " + anomaly.getSeverity());
            }
        }

        System.out.println("✅ CH4 Recovery anomaly detection completed - Found " + anomalies.size() + " anomalies");
        return anomalies;
    }

    /**
     * Detects anomalies in temperature readings
     */
    public List<AnomalyDTO> detectTemperatureAnomalies(String farmId, Instant startDate, Instant endDate) {
        List<AnomalyDTO> anomalies = new ArrayList<>();

        // Get farm details
        Farm farm = farmRepository.findById(farmId).orElse(null);
        if (farm == null) {
            return anomalies;
        }

        // Get temperature data for the specified period
        List<Temperature> tempData = temperatureRepository.findByFarmIdAndTimestampBetweenOrderByTimestampAsc(
                farmId, startDate, endDate);

        if (tempData.size() < 2) {
            return anomalies; // Not enough data for anomaly detection
        }

        // Calculate baseline statistics (mean and standard deviation)
        DoubleSummaryStatistics stats = tempData.stream()
                .map(Temperature::getValue)
                .collect(Collectors.summarizingDouble(Double::doubleValue));

        double mean = stats.getAverage();
        double stdDev = calculateStandardDeviation(tempData.stream()
                .map(Temperature::getValue)
                .collect(Collectors.toList()), mean);

        // Detect anomalies (values outside of 2 standard deviations)
        for (Temperature dataPoint : tempData) {
            double value = dataPoint.getValue();
            double zScore = (value - mean) / stdDev;

            if (Math.abs(zScore) > 2.0) {
                AnomalyDTO anomaly = new AnomalyDTO();
                anomaly.setFarmId(farmId);
                anomaly.setFarmName(farm.getName());
                anomaly.setMetricType("Temperature");
                anomaly.setValue(value);
                anomaly.setExpectedValue(mean);
                anomaly.setDeviationPercent((value - mean) / mean * 100);
                anomaly.setTimestamp(dataPoint.getTimestamp());

                // Determine severity
                if (Math.abs(zScore) > 3.0) {
                    anomaly.setSeverity("Critical");
                } else {
                    anomaly.setSeverity("Warning");
                }

                anomalies.add(anomaly);
            }
        }

        return anomalies;
    }

    /**
     * Detects anomalies in mass balance readings
     */
    public List<AnomalyDTO> detectMassBalanceAnomalies(String farmId, Instant startDate, Instant endDate) {
        System.out.println("🔍 Starting Mass Balance anomaly detection for farm: " + farmId + " from " + startDate + " to " + endDate);

        List<AnomalyDTO> anomalies = new ArrayList<>();

        // Get farm details
        Farm farm = farmRepository.findById(farmId).orElse(null);
        if (farm == null) {
            System.out.println("❌ Farm not found for ID: " + farmId);
            return anomalies;
        }

        // Get mass balance data for the specified period
        List<MassBalance> massBalanceData = massBalanceRepository.findByFarmIdAndTimestampBetweenOrderByTimestampAsc(
                farmId, startDate, endDate);

        System.out.println("📊 Retrieved " + massBalanceData.size() + " mass balance data points for analysis");

        if (massBalanceData.size() < 2) {
            System.out.println("⚠️ Insufficient data for anomaly detection (need at least 2 points, got " + massBalanceData.size() + ")");
            return anomalies; // Not enough data for anomaly detection
        }

        // Calculate baseline statistics (mean and standard deviation)
        DoubleSummaryStatistics stats = massBalanceData.stream()
                .map(MassBalance::getValue)
                .filter(value -> value != null)  // Filter out null values
                .collect(Collectors.summarizingDouble(Double::doubleValue));

        double mean = stats.getAverage();
        double stdDev = calculateStandardDeviation(massBalanceData.stream()
                .map(MassBalance::getValue)
                .filter(value -> value != null)  // Filter out null values
                .collect(Collectors.toList()), mean);

        System.out.println("📈 Statistical baseline - Mean: " + String.format("%.2f", mean) + ", StdDev: " + String.format("%.2f", stdDev));

        // Detect anomalies (values outside of 2 standard deviations)
        for (MassBalance dataPoint : massBalanceData) {
            Double valueObj = dataPoint.getValue();
            // Skip null values
            if (valueObj == null) {
                continue;
            }
            double value = valueObj;
            double zScore = (value - mean) / stdDev;

            if (Math.abs(zScore) > 2.0) {
                AnomalyDTO anomaly = new AnomalyDTO();
                anomaly.setFarmId(farmId);
                anomaly.setFarmName(farm.getName());
                anomaly.setMetricType("Mass Balance");
                anomaly.setValue(value);
                anomaly.setExpectedValue(mean);
                anomaly.setDeviationPercent((value - mean) / mean * 100);
                anomaly.setTimestamp(dataPoint.getTimestamp());

                // Determine severity
                if (Math.abs(zScore) > 3.0) {
                    anomaly.setSeverity("Critical");
                } else {
                    anomaly.setSeverity("Warning");
                }

                anomalies.add(anomaly);
                System.out.println("🚨 Anomaly detected - Value: " + String.format("%.2f", value) + ", Z-Score: " + String.format("%.2f", zScore) + ", Severity: " + anomaly.getSeverity());
            }
        }

        System.out.println("✅ Mass Balance anomaly detection completed - Found " + anomalies.size() + " anomalies");
        return anomalies;
    }

    /**
     * Calculate standard deviation for a list of values
     */
    private double calculateStandardDeviation(List<Double> values, double mean) {
        double variance = values.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .average()
                .orElse(0.0);

        return Math.sqrt(variance);
    }
}
