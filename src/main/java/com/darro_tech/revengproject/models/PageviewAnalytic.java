package com.darro_tech.revengproject.models;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "pageview_analytics")
public class PageviewAnalytic {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "page", nullable = false, length = 36)
    private String page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm", columnDefinition = "varchar(36)")
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company", columnDefinition = "varchar(36)")
    private Company company;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "data_date")
    private Instant dataDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public Farm getFarm() {
        return farm;
    }

    public void setFarm(Farm farm) {
        this.farm = farm;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Instant getDataDate() {
        return dataDate;
    }

    public void setDataDate(Instant dataDate) {
        this.dataDate = dataDate;
    }

}
