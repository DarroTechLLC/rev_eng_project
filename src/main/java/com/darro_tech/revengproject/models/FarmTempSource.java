package com.darro_tech.revengproject.models;

import jakarta.persistence.*;

@Entity
@Table(name = "farm_temp_source")
public class FarmTempSource {
    @Id
    @Column(name = "target_farm_id", nullable = false, length = 36)
    private String targetFarmId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_farm_id", nullable = false)
    private Farm farms;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_farm_id")
    private Farm sourceFarm;

    public String getTargetFarmId() {
        return targetFarmId;
    }

    public void setTargetFarmId(String targetFarmId) {
        this.targetFarmId = targetFarmId;
    }

    public Farm getFarms() {
        return farms;
    }

    public void setFarms(Farm farms) {
        this.farms = farms;
    }

    public Farm getSourceFarm() {
        return sourceFarm;
    }

    public void setSourceFarm(Farm sourceFarm) {
        this.sourceFarm = sourceFarm;
    }

}