package com.darro_tech.revengproject.models;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "farms")
public class Farm {
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Lob
    @Column(name = "farm_type")
    private String farmType;

    @Column(name = "temp_source_id", length = 36)
    private String tempSourceId;

    @ColumnDefault("0")
    @Column(name = "is_temp_source", nullable = false)
    private Boolean isTempSource = false;

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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getFarmType() {
        return farmType;
    }

    public void setFarmType(String farmType) {
        this.farmType = farmType;
    }

    public String getTempSourceId() {
        return tempSourceId;
    }

    public void setTempSourceId(String tempSourceId) {
        this.tempSourceId = tempSourceId;
    }

    public Boolean getIsTempSource() {
        return isTempSource;
    }

    public void setIsTempSource(Boolean isTempSource) {
        this.isTempSource = isTempSource;
    }

}