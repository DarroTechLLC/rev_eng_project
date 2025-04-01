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
@Table(name = "chart_population_104week_view")
public class ChartPopulation104weekView {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "farm_id", nullable = false)
    private Integer farmId;

    @Column(name = "animal_headcount", nullable = false)
    private Integer animalHeadcount;

    @Column(name = "animal_weight_unit", nullable = false)
    private Integer animalWeightUnit;

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

    public Integer getAnimalHeadcount() {
        return animalHeadcount;
    }

    public Integer getAnimalWeightUnit() {
        return animalWeightUnit;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    protected ChartPopulation104weekView() {
    }
}