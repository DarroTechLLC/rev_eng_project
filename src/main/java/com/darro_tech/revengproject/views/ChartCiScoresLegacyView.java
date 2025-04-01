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
@Table(name = "chart_ci_scores_legacy_view")
public class ChartCiScoresLegacyView {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "farm_id", nullable = false)
    private Integer farmId;

    @Column(name = "value", nullable = false)
    private Integer value;

    @Column(name = "timestamp", nullable = false)
    private Integer timestamp;

    @Column(name = "company_id", nullable = false)
    private Integer companyId;

    public Integer getId() {
        return id;
    }

    public Integer getFarmId() {
        return farmId;
    }

    public Integer getValue() {
        return value;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    protected ChartCiScoresLegacyView() {
    }
}