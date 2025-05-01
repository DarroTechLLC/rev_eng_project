package com.darro_tech.revengproject.models.dto;

/**
 * Data Transfer Object for Company information Used for API responses
 */
public class CompanyDTO {

    private String companyId;
    private String companyName;
    private String displayName;
    private String logoUrl;

    public CompanyDTO(String companyId, String companyName, String displayName, String logoUrl) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.displayName = displayName;
        this.logoUrl = logoUrl;
    }

    // Getters and setters
    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}
