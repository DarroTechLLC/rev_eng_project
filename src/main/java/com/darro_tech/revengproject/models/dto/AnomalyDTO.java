package com.darro_tech.revengproject.models.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class AnomalyDTO {
    private String farmId;
    private String farmName;
    private String metricType;
    private Double value;
    private Double expectedValue;
    private Double deviationPercent;
    private String severity;
    private Instant timestamp;
}