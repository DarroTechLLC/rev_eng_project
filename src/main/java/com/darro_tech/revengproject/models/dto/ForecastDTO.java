package com.darro_tech.revengproject.models.dto;

import lombok.Data;
import java.util.List;

@Data
public class ForecastDTO {
    private String farmId;
    private String farmName;
    private String metricType;
    private List<TimeSeriesPointDTO> historicalData;
    private List<TimeSeriesPointDTO> forecastData;
    private Double confidenceLevel;
}