package com.darro_tech.revengproject.models.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class TimeSeriesPointDTO {
    private Instant timestamp;
    private Double value;
}