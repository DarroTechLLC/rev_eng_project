package com.darro_tech.revengproject.models.dto;

/**
 * Data Transfer Object for Company information Used for API responses
 */
public class CompanyDTO {

    private String id;
    private String name;
    private String displayName;
    private String logoUrl;

    public CompanyDTO() {
    }

    public CompanyDTO(String id, String name, String displayName, String logoUrl) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.logoUrl = logoUrl;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
