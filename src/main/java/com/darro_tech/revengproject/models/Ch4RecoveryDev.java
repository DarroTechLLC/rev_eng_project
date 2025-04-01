package com.darro_tech.revengproject.models;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "ch4_recovery_dev")
public class Ch4RecoveryDev {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @Column(name = "value")
    private Double value;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "mass_balance")
    private Double massBalance;

    @Column(name = "totalizer")
    private Double totalizer;

    @Column(name = "feed_ch4")
    private Double feedCh4;

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

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Double getMassBalance() {
        return massBalance;
    }

    public void setMassBalance(Double massBalance) {
        this.massBalance = massBalance;
    }

    public Double getTotalizer() {
        return totalizer;
    }

    public void setTotalizer(Double totalizer) {
        this.totalizer = totalizer;
    }

    public Double getFeedCh4() {
        return feedCh4;
    }

    public void setFeedCh4(Double feedCh4) {
        this.feedCh4 = feedCh4;
    }

}