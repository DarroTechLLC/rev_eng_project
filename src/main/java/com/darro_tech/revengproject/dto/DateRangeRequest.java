package com.darro_tech.revengproject.dto;

import java.time.LocalDate;

/**
 * Request DTO for chart API endpoints that require a date range.
 * Matches the NextJS request format.
 */
public class DateRangeRequest {
    private LocalDate from;
    private LocalDate to;
    
    // Getters and setters
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