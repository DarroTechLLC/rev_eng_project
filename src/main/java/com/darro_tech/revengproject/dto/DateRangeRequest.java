package com.darro_tech.revengproject.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.darro_tech.revengproject.utils.SafeLocalDateDeserializer;

import java.time.LocalDate;

/**
 * Request DTO for chart API endpoints that require a date range.
 * Matches the NextJS request format.
 */
public class DateRangeRequest {
    @JsonDeserialize(using = SafeLocalDateDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate from;

    @JsonDeserialize(using = SafeLocalDateDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd")
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
