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
@Table(name = "chart_monthly_farm_production_view")
public class ChartMonthlyFarmProductionView {
    @Id
    @Column(name = "production", nullable = false)
    private Integer production;

    @Column(name = "farm_id", nullable = false)
    private Integer farmId;

    @Column(name = "company_id", nullable = false)
    private Integer companyId;

    @Column(name = "timestamp", nullable = false)
    private Integer timestamp;

    public Integer getProduction() {
        return production;
    }

    public Integer getFarmId() {
        return farmId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    protected ChartMonthlyFarmProductionView() {
    }
}