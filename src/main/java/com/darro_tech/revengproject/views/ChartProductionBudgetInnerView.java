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
@Table(name = "chart_production_budget_inner_view")
public class ChartProductionBudgetInnerView {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "budget", nullable = false)
    private Integer budget;

    @Column(name = "production", nullable = false)
    private Integer production;

    @Column(name = "farm_id", nullable = false)
    private Integer farmId;

    @Column(name = "company_id", nullable = false)
    private Integer companyId;

    @Column(name = "timestamp", nullable = false)
    private Integer timestamp;

    public Integer getId() {
        return id;
    }

    public Integer getBudget() {
        return budget;
    }

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

    protected ChartProductionBudgetInnerView() {
    }
}