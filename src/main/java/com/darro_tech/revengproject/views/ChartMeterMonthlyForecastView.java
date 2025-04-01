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
@Table(name = "chart_meter_monthly_forecast_view")
public class ChartMeterMonthlyForecastView {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "meter_id", nullable = false)
    private Integer meterId;

    @Column(name = "value", nullable = false)
    private Integer value;

    @Column(name = "timestamp", nullable = false)
    private Integer timestamp;

    @Column(name = "farm_id", nullable = false)
    private Integer farmId;

    @Column(name = "company_id", nullable = false)
    private Integer companyId;

    public Integer getId() {
        return id;
    }

    public Integer getMeterId() {
        return meterId;
    }

    public Integer getValue() {
        return value;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public Integer getFarmId() {
        return farmId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    protected ChartMeterMonthlyForecastView() {
    }
}