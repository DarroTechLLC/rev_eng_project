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
@Table(name = "chart_ci_scores_combined_view")
public class ChartCiScoresCombinedView {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "current", nullable = false)
    private Integer current;

    @Column(name = "forecast", nullable = false)
    private Integer forecast;

    @Column(name = "legacy", nullable = false)
    private Integer legacy;

    @Column(name = "farm_id", nullable = false)
    private Integer farmId;

    @Column(name = "company_id", nullable = false)
    private Integer companyId;

    public Integer getId() {
        return id;
    }

    public Integer getCurrent() {
        return current;
    }

    public Integer getForecast() {
        return forecast;
    }

    public Integer getLegacy() {
        return legacy;
    }

    public Integer getFarmId() {
        return farmId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    protected ChartCiScoresCombinedView() {
    }
}