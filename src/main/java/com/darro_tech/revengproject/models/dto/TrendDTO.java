package com.darro_tech.revengproject.models.dto;

public class TrendDTO {

    private String farmId;
    private String farmName;
    private String metricType;
    private Double slope;
    private String direction;
    private Double r2Value;
    private String interpretation;

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

    public Double getSlope() {
        return slope;
    }

    public String getDirection() {
        return direction;
    }

    public Double getR2Value() {
        return r2Value;
    }

    public String getInterpretation() {
        return interpretation;
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

    public void setSlope(Double slope) {
        this.slope = slope;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setR2Value(Double r2Value) {
        this.r2Value = r2Value;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }
}
