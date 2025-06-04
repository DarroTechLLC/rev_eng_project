package com.darro_tech.revengproject.dto;

import java.time.LocalDate;

/**
 * Request DTO for chart API endpoints that require a company ID and a date range.
 * Matches the NextJS request format.
 */
public class CompanyDateRangeRequest {
    private String company_id;
    private LocalDate from;
    private LocalDate to;
    
    // Getters and setters
    public String getCompany_id() {
        return company_id;
    }
    
    public void setCompany_id(String company_id) {
        this.company_id = company_id;
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