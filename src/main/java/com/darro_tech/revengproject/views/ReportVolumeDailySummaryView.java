package com.darro_tech.revengproject.views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;

/**
 * Entity mapping for the report_volume_daily_summary_view database view.
 * This view aggregates daily production data by company, farm, and date.
 */
@Entity
@Immutable
@Table(name = "report_volume_daily_summary_view")
public class ReportVolumeDailySummaryView {
    
    @Id
    @Column(name = "company_id")
    private String companyId;
    
    @Column(name = "farm_id")
    private String farmId;
    
    @Column(name = "volume")
    private Double volume;
    
    @Column(name = "date")
    private LocalDate date;
    
    // Getters
    
    public String getCompanyId() {
        return companyId;
    }
    
    public String getFarmId() {
        return farmId;
    }
    
    public Double getVolume() {
        return volume;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    // Protected constructor for JPA
    protected ReportVolumeDailySummaryView() {
    }
}