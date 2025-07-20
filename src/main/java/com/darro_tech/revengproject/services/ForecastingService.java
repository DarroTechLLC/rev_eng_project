package com.darro_tech.revengproject.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darro_tech.revengproject.models.Ch4Recovery;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.models.dto.ForecastDTO;
import com.darro_tech.revengproject.models.dto.TimeSeriesPointDTO;
import com.darro_tech.revengproject.repositories.Ch4RecoveryRepository;
import com.darro_tech.revengproject.repositories.FarmRepository;

@Service
public class ForecastingService {

    private static final Logger logger = LoggerFactory.getLogger(ForecastingService.class);

    @Autowired
    private Ch4RecoveryRepository ch4RecoveryRepository;

    @Autowired
    private FarmRepository farmRepository;

    /**
     * Forecasts CH4 recovery for the next 30 days
     */
    public ForecastDTO forecastCh4Recovery(String farmId, Instant startDate, Instant endDate) {
        logger.info("🔮 Starting CH4 Recovery forecasting for farm: {} from {} to {}", farmId, startDate, endDate);

        ForecastDTO forecast = new ForecastDTO();

        // Get farm details
        Farm farm = farmRepository.findById(farmId).orElse(null);
        if (farm == null) {
            logger.warn("❌ Farm not found for ID: {}", farmId);
            return forecast;
        }

        forecast.setFarmId(farmId);
        forecast.setFarmName(farm.getName());
        forecast.setMetricType("CH4 Recovery");

        // Get historical CH4 recovery data
        List<Ch4Recovery> historicalData = ch4RecoveryRepository.findByFarmIdAndTimestampBetweenOrderByTimestampAsc(
                farmId, startDate, endDate);

        logger.info("📊 Retrieved {} historical CH4 recovery data points for forecasting", historicalData.size());

        if (historicalData.size() < 10) {
            logger.warn("⚠️ Insufficient data for forecasting (need at least 10 points, got {})", historicalData.size());
            // Not enough data for forecasting, but initialize empty lists
            forecast.setHistoricalData(new ArrayList<>());
            forecast.setForecastData(new ArrayList<>());
            forecast.setConfidenceLevel(0.0);
            return forecast;
        }

        // Convert historical data to DTOs
        List<TimeSeriesPointDTO> historicalPoints = historicalData.stream()
                .map(data -> {
                    TimeSeriesPointDTO point = new TimeSeriesPointDTO();
                    point.setTimestamp(data.getTimestamp());
                    point.setValue(data.getValue());
                    return point;
                })
                .collect(Collectors.toList());

        forecast.setHistoricalData(historicalPoints);

        // Perform simple moving average forecasting
        List<TimeSeriesPointDTO> forecastPoints = new ArrayList<>();

        // Use the last 7 data points for the moving average
        int windowSize = Math.min(7, historicalData.size());
        double movingAverage = historicalData.subList(historicalData.size() - windowSize, historicalData.size())
                .stream()
                .mapToDouble(Ch4Recovery::getValue)
                .average()
                .orElse(0.0);

        logger.info("📈 Moving average calculated: {:.2f} (using last {} data points)", movingAverage, windowSize);

        // Generate forecast for the next 30 days
        Instant lastDate = historicalData.get(historicalData.size() - 1).getTimestamp();
        for (int i = 1; i <= 30; i++) {
            TimeSeriesPointDTO point = new TimeSeriesPointDTO();
            point.setTimestamp(lastDate.plus(i, ChronoUnit.DAYS));
            point.setValue(movingAverage);
            forecastPoints.add(point);
        }

        forecast.setForecastData(forecastPoints);
        forecast.setConfidenceLevel(0.7); // Placeholder confidence level

        logger.info("✅ Forecasting completed - Generated {} forecast points with {:.1f}% confidence",
                forecastPoints.size(), forecast.getConfidenceLevel() * 100);

        return forecast;
    }
}
