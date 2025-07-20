package com.darro_tech.revengproject.models.dto;

import java.time.Instant;

public class AnomalyDTO {

    private String farmId;
    private String farmName;
    private String metricType;
    private Double value;
    private Double expectedValue;
    private Double deviationPercent;
    private String severity;
    private Instant timestamp;

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

    public Double getValue() {
        return value;
    }

    public Double getExpectedValue() {
        return expectedValue;
    }

    public Double getDeviationPercent() {
        return deviationPercent;
    }

    public String getSeverity() {
        return severity;
    }

    public Instant getTimestamp() {
        return timestamp;
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

    public void setValue(Double value) {
        this.value = value;
    }

    public void setExpectedValue(Double expectedValue) {
        this.expectedValue = expectedValue;
    }

    public void setDeviationPercent(Double deviationPercent) {
        this.deviationPercent = deviationPercent;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
