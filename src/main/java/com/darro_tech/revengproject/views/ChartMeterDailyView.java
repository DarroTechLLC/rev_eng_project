package com.darro_tech.revengproject.views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import jakarta.persistence.Id;

import java.time.Instant;

/**
 * Mapping for DB view
 */
@Entity
@Immutable
@Table(name = "chart_meter_daily_view")
public class ChartMeterDailyView {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "meter_id", nullable = false)
    private String meterId;

    @Column(name = "value", nullable = false)
    private Double value;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "farm_id", nullable = false)
    private String farmId;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    public Integer getId() {
        return id;
    }

    public String getMeterId() {
        return meterId;
    }

    public Double getValue() {
        return value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getFarmId() {
        return farmId;
    }

    public String getCompanyId() {
        return companyId;
    }

    protected ChartMeterDailyView() {
    }
}
