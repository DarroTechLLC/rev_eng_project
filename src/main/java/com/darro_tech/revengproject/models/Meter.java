package com.darro_tech.revengproject.models;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "meters")
public class Meter {
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "include_website")
    private Boolean includeWebsite;

    @ColumnDefault("0")
    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived = false;

    @OneToMany(mappedBy = "meter")
    private Set<BuyBack> buyBacks = new LinkedHashSet<>();

    @OneToMany(mappedBy = "meter")
    private Set<CompanyMeter> companyMeters = new LinkedHashSet<>();

    @OneToMany(mappedBy = "meter")
    private Set<MeterDaily> meterDailies = new LinkedHashSet<>();

    @OneToMany(mappedBy = "meter")
    private Set<MeterHourly> meterHourlies = new LinkedHashSet<>();

    @OneToMany(mappedBy = "meter")
    private Set<MeterMonthlyForecast> meterMonthlyForecasts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "meter")
    private Set<WebsiteMeter> websiteMeters = new LinkedHashSet<>();

    public Set<WebsiteMeter> getWebsiteMeters() {
        return websiteMeters;
    }

    public void setWebsiteMeters(Set<WebsiteMeter> websiteMeters) {
        this.websiteMeters = websiteMeters;
    }

    public Set<MeterMonthlyForecast> getMeterMonthlyForecasts() {
        return meterMonthlyForecasts;
    }

    public void setMeterMonthlyForecasts(Set<MeterMonthlyForecast> meterMonthlyForecasts) {
        this.meterMonthlyForecasts = meterMonthlyForecasts;
    }

    public Set<MeterHourly> getMeterHourlies() {
        return meterHourlies;
    }

    public void setMeterHourlies(Set<MeterHourly> meterHourlies) {
        this.meterHourlies = meterHourlies;
    }

    public Set<MeterDaily> getMeterDailies() {
        return meterDailies;
    }

    public void setMeterDailies(Set<MeterDaily> meterDailies) {
        this.meterDailies = meterDailies;
    }

    public Set<CompanyMeter> getCompanyMeters() {
        return companyMeters;
    }

    public void setCompanyMeters(Set<CompanyMeter> companyMeters) {
        this.companyMeters = companyMeters;
    }

    public Set<BuyBack> getBuyBacks() {
        return buyBacks;
    }

    public void setBuyBacks(Set<BuyBack> buyBacks) {
        this.buyBacks = buyBacks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Farm getFarm() {
        return farm;
    }

    public void setFarm(Farm farm) {
        this.farm = farm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getIncludeWebsite() {
        return includeWebsite;
    }

    public void setIncludeWebsite(Boolean includeWebsite) {
        this.includeWebsite = includeWebsite;
    }

    public Boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }

}