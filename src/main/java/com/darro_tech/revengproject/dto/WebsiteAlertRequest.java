package com.darro_tech.revengproject.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for Website Alert operations
 */
public class WebsiteAlertRequest {

    private Integer id;

    private String message;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("company_ids")
    private List<String> companyIds;

    // Default constructor
    public WebsiteAlertRequest() {
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<String> getCompanyIds() {
        return companyIds;
    }

    public void setCompanyIds(List<String> companyIds) {
        this.companyIds = companyIds;
    }
}
