package com.darro_tech.revengproject.models.dto;

import lombok.Data;

@Data
public class TrendDTO {
    private String farmId;
    private String farmName;
    private String metricType;
    private Double slope;
    private String direction;
    private Double r2Value;
    private String interpretation;
}