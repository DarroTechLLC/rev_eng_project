package com.darro_tech.revengproject.views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import jakarta.persistence.Id;
/**
 * Mapping for DB view
 */
@Entity
@Immutable
@Table(name = "chart_temperature_view")
public class ChartTemperatureView {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "value", nullable = false)
    private Integer value;

    @Column(name = "timestamp", nullable = false)
    private Integer timestamp;

    @Column(name = "source_farm_id", nullable = false)
    private Integer sourceFarmId;

    @Column(name = "target_farm_id", nullable = false)
    private Integer targetFarmId;

    public Integer getId() {
        return id;
    }

    public Integer getValue() {
        return value;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public Integer getSourceFarmId() {
        return sourceFarmId;
    }

    public Integer getTargetFarmId() {
        return targetFarmId;
    }

    protected ChartTemperatureView() {
    }
}