package com.darro_tech.revengproject.services;

import com.darro_tech.revengproject.models.Ch4Recovery;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.models.dto.TrendDTO;
import com.darro_tech.revengproject.repositories.Ch4RecoveryRepository;
import com.darro_tech.revengproject.repositories.FarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TrendAnalysisService {

    @Autowired
    private Ch4RecoveryRepository ch4RecoveryRepository;
    
    @Autowired
    private FarmRepository farmRepository;
    
    /**
     * Analyzes trends in CH4 recovery data
     */
    public TrendDTO analyzeCh4RecoveryTrend(String farmId, Instant startDate, Instant endDate) {
        TrendDTO trend = new TrendDTO();
        
        // Get farm details
        Farm farm = farmRepository.findById(farmId).orElse(null);
        if (farm == null) {
            return trend;
        }
        
        trend.setFarmId(farmId);
        trend.setFarmName(farm.getName());
        trend.setMetricType("CH4 Recovery");
        
        // Get CH4 recovery data for the specified period
        List<Ch4Recovery> ch4Data = ch4RecoveryRepository.findByFarmIdAndTimestampBetweenOrderByTimestampAsc(
                farmId, startDate, endDate);
        
        if (ch4Data.size() < 5) {
            trend.setInterpretation("Insufficient data for trend analysis");
            return trend;
        }
        
        // Convert to x (days from start) and y (values) for linear regression
        Instant firstDay = ch4Data.get(0).getTimestamp();
        
        List<Double> xValues = ch4Data.stream()
                .map(data -> (double) ChronoUnit.DAYS.between(firstDay, data.getTimestamp()))
                .collect(Collectors.toList());
        
        List<Double> yValues = ch4Data.stream()
                .map(Ch4Recovery::getValue)
                .collect(Collectors.toList());
        
        // Perform linear regression
        double[] regressionParams = performLinearRegression(xValues, yValues);
        double slope = regressionParams[0];
        double intercept = regressionParams[1];
        double r2 = regressionParams[2];
        
        trend.setSlope(slope);
        trend.setR2Value(r2);
        
        // Determine direction and interpretation
        if (Math.abs(slope) < 0.01) {
            trend.setDirection("Stable");
            trend.setInterpretation("CH4 recovery is stable over the analyzed period");
        } else if (slope > 0) {
            trend.setDirection("Increasing");
            trend.setInterpretation("CH4 recovery is showing an increasing trend");
        } else {
            trend.setDirection("Decreasing");
            trend.setInterpretation("CH4 recovery is showing a decreasing trend");
        }
        
        // Add confidence based on R² value
        if (r2 < 0.3) {
            trend.setInterpretation(trend.getInterpretation() + " (low confidence)");
        } else if (r2 < 0.7) {
            trend.setInterpretation(trend.getInterpretation() + " (moderate confidence)");
        } else {
            trend.setInterpretation(trend.getInterpretation() + " (high confidence)");
        }
        
        return trend;
    }
    
    /**
     * Performs linear regression and returns [slope, intercept, R²]
     */
    private double[] performLinearRegression(List<Double> x, List<Double> y) {
        int n = x.size();
        
        // Calculate means
        double meanX = x.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double meanY = y.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        // Calculate slope and intercept
        double numerator = IntStream.range(0, n)
                .mapToDouble(i -> (x.get(i) - meanX) * (y.get(i) - meanY))
                .sum();
        
        double denominator = x.stream()
                .mapToDouble(xi -> Math.pow(xi - meanX, 2))
                .sum();
        
        double slope = numerator / denominator;
        double intercept = meanY - slope * meanX;
        
        // Calculate R²
        double totalSumSquares = IntStream.range(0, n)
                .mapToDouble(i -> Math.pow(y.get(i) - meanY, 2))
                .sum();
        
        double residualSumSquares = IntStream.range(0, n)
                .mapToDouble(i -> Math.pow(y.get(i) - (intercept + slope * x.get(i)), 2))
                .sum();
        
        double r2 = 1 - (residualSumSquares / totalSumSquares);
        
        return new double[] { slope, intercept, r2 };
    }
}