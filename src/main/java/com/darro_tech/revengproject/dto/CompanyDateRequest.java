package com.darro_tech.revengproject.dto;

import java.time.LocalDate;

/**
 * Request DTO for chart API endpoints that require a company ID and a date.
 * Matches the NextJS request format.
 */
public class CompanyDateRequest {
    private String company_id;
    private LocalDate date;
    
    // Getters and setters
    public String getCompany_id() {
        return company_id;
    }
    
    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
}