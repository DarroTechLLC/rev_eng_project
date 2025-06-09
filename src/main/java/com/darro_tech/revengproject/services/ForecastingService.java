package com.darro_tech.revengproject.services;

import com.darro_tech.revengproject.models.Ch4Recovery;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.models.dto.ForecastDTO;
import com.darro_tech.revengproject.models.dto.TimeSeriesPointDTO;
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
public class ForecastingService {

    @Autowired
    private Ch4RecoveryRepository ch4RecoveryRepository;

    @Autowired
    private FarmRepository farmRepository;

    /**
     * Forecasts CH4 recovery for the next 30 days
     */
    public ForecastDTO forecastCh4Recovery(String farmId, Instant startDate, Instant endDate) {
        ForecastDTO forecast = new ForecastDTO();

        // Get farm details
        Farm farm = farmRepository.findById(farmId).orElse(null);
        if (farm == null) {
            return forecast;
        }

        forecast.setFarmId(farmId);
        forecast.setFarmName(farm.getName());
        forecast.setMetricType("CH4 Recovery");

        // Get historical CH4 recovery data
        List<Ch4Recovery> historicalData = ch4RecoveryRepository.findByFarmIdAndTimestampBetweenOrderByTimestampAsc(
                farmId, startDate, endDate);

        if (historicalData.size() < 10) {
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

        return forecast;
    }
}
