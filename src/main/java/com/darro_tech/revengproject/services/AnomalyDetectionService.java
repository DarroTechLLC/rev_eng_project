package com.darro_tech.revengproject.services;

import com.darro_tech.revengproject.models.Ch4Recovery;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.models.Temperature;
import com.darro_tech.revengproject.models.dto.AnomalyDTO;
import com.darro_tech.revengproject.repositories.Ch4RecoveryRepository;
import com.darro_tech.revengproject.repositories.FarmRepository;
import com.darro_tech.revengproject.repositories.TemperatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnomalyDetectionService {

    @Autowired
    private Ch4RecoveryRepository ch4RecoveryRepository;
    
    @Autowired
    private TemperatureRepository temperatureRepository;
    
    @Autowired
    private FarmRepository farmRepository;
    
    // Configurable threshold for what constitutes a "significant" change
    private static final double SIGNIFICANT_CHANGE_THRESHOLD = 15.0; // percent
    
    /**
     * Detects significant drops or increases in CH4 recovery values
     */
    public List<AnomalyDTO> detectCh4RecoveryAnomalies(String farmId, Instant startDate, Instant endDate) {
        List<AnomalyDTO> anomalies = new ArrayList<>();
        
        // Get farm details
        Farm farm = farmRepository.findById(farmId).orElse(null);
        if (farm == null) {
            return anomalies;
        }
        
        // Get CH4 recovery data for the specified period
        List<Ch4Recovery> ch4Data = ch4RecoveryRepository.findByFarmIdAndTimestampBetweenOrderByTimestampAsc(
                farmId, startDate, endDate);
        
        if (ch4Data.size() < 2) {
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
            }
        }
        
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