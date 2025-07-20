package com.darro_tech.revengproject.models.dto;

import java.util.List;

public class ForecastDTO {

    private String farmId;
    private String farmName;
    private String metricType;
    private List<TimeSeriesPointDTO> historicalData;
    private List<TimeSeriesPointDTO> forecastData;
    private Double confidenceLevel;

    // Getters
    public String getFarmId() {
        return farmId;
    }

    public String getFarmName() {
        return farmName;
    }

    public String getMetricType() {
        return metricType;
    }

    public List<TimeSeriesPointDTO> getHistoricalData() {
        return historicalData;
    }

    public List<TimeSeriesPointDTO> getForecastData() {
        return forecastData;
    }

    public Double getConfidenceLevel() {
        return confidenceLevel;
    }

    // Setters
    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public void setHistoricalData(List<TimeSeriesPointDTO> historicalData) {
        this.historicalData = historicalData;
    }

    public void setForecastData(List<TimeSeriesPointDTO> forecastData) {
        this.forecastData = forecastData;
    }

    public void setConfidenceLevel(Double confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }
}
