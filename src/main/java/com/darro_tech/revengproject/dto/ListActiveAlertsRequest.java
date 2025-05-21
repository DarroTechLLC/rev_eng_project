package com.darro_tech.revengproject.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request object for listing active alerts by company ID
 */
public class ListActiveAlertsRequest {
    
    @JsonProperty("companyId")
    private String companyId;
    
    public ListActiveAlertsRequest() {
    }
    
    public ListActiveAlertsRequest(String companyId) {
        this.companyId = companyId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
} 