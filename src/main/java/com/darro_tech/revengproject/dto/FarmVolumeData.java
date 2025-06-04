package com.darro_tech.revengproject.dto;

public class FarmVolumeData {
    private String farm_id;
    private String farmName;
    private Double volume;

    // Getters and setters
    public String getFarm_id() {
        return farm_id;
    }

    public void setFarm_id(String farm_id) {
        this.farm_id = farm_id;
    }

    // For backward compatibility
    public String getFarmId() {
        return farm_id;
    }

    public void setFarmId(String farmId) {
        this.farm_id = farmId;
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
