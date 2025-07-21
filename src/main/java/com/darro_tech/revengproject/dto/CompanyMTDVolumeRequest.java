package com.darro_tech.revengproject.dto;

import java.time.LocalDate;

public class CompanyMTDVolumeRequest {
    private String companyId;
    private LocalDate date;

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
} 