package com.darro_tech.revengproject.dto;

public class FarmVolumeData {
    private String farmId;
    private String farmName;
    private Double volume;
    
    // Getters and setters
    public String getFarmId() {
        return farmId;
    }
    
    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }
    
    public String getFarmName() {
        return farmName;
    }
    
    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }
    
    public Double getVolume() {
        return volume;
    }
    
    public void setVolume(Double volume) {
        this.volume = volume;
    }
}