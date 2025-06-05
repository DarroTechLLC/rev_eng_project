package com.darro_tech.revengproject.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.darro_tech.revengproject.utils.SafeLocalDateDeserializer;

import java.time.LocalDate;

/**
 * Request DTO for chart API endpoints that require a farm ID and a date range.
 * Matches the NextJS request format.
 */
public class FarmDateRangeRequest {
    private String farm_id;
    
    @JsonDeserialize(using = SafeLocalDateDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate from;
    
    @JsonDeserialize(using = SafeLocalDateDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate to;
    
    // Getters and setters
    public String getFarm_id() {
        return farm_id;
    }
    
    public void setFarm_id(String farm_id) {
        this.farm_id = farm_id;
    }
    
    public LocalDate getFrom() {
        return from;
    }
    
    public void setFrom(LocalDate from) {
        this.from = from;
    }
    
    public LocalDate getTo() {
        return to;
    }
    
    public void setTo(LocalDate to) {
        this.to = to;
    }
}