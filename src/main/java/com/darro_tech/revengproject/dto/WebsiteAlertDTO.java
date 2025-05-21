package com.darro_tech.revengproject.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Website Alert data Structured to match the format used in the NextJS
 * application
 */
public class WebsiteAlertDTO {

    private Integer id;

    private String message;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("company_ids")
    private List<String> companyIds;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    // Default constructor
    public WebsiteAlertDTO() {
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
