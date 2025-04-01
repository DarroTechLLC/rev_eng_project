package com.darro_tech.revengproject.models;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "population_104week")
public class Population104week {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @Column(name = "animal_headcount", nullable = false)
    private Double animalHeadcount;

    @Column(name = "animal_weight_unit", nullable = false)
    private Double animalWeightUnit;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Farm getFarm() {
        return farm;
    }

    public void setFarm(Farm farm) {
        this.farm = farm;
    }

    public Double getAnimalHeadcount() {
        return animalHeadcount;
    }

    public void setAnimalHeadcount(Double animalHeadcount) {
        this.animalHeadcount = animalHeadcount;
    }

    public Double getAnimalWeightUnit() {
        return animalWeightUnit;
    }

    public void setAnimalWeightUnit(Double animalWeightUnit) {
        this.animalWeightUnit = animalWeightUnit;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

}