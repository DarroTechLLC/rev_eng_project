package com.darro_tech.revengproject.models.dto;

import java.time.Instant;

public class TimeSeriesPointDTO {

    private Instant timestamp;
    private Double value;

    // Getters
    public Instant getTimestamp() {
        return timestamp;
    }

    public Double getValue() {
        return value;
    }

    // Setters
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
