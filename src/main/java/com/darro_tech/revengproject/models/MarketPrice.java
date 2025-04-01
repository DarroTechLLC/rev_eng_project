package com.darro_tech.revengproject.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "market_prices")
public class MarketPrice {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "lcfs")
    private Double lcfs;

    @Column(name = "d3")
    private Double d3;

    @Column(name = "d5")
    private Double d5;

    @Column(name = "natural_gas")
    private Double naturalGas;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getLcfs() {
        return lcfs;
    }

    public void setLcfs(Double lcfs) {
        this.lcfs = lcfs;
    }

    public Double getD3() {
        return d3;
    }

    public void setD3(Double d3) {
        this.d3 = d3;
    }

    public Double getD5() {
        return d5;
    }

    public void setD5(Double d5) {
        this.d5 = d5;
    }

    public Double getNaturalGas() {
        return naturalGas;
    }

    public void setNaturalGas(Double naturalGas) {
        this.naturalGas = naturalGas;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

}