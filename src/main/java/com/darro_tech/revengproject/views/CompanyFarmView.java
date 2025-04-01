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
@Table(name = "company_farm_view")
public class CompanyFarmView {
    @Id
    @Column(name = "farm_id", nullable = false)
    private Integer farmId;

    @Column(name = "company_id", nullable = false)
    private Integer companyId;

    @Column(name = "farm_name", nullable = false)
    private Integer farmName;

    @Column(name = "display_name", nullable = false)
    private Integer displayName;

    @Column(name = "farm_type", nullable = false)
    private Integer farmType;

    @Column(name = "temp_source_id", nullable = false)
    private Integer tempSourceId;

    @Column(name = "is_temp_source", nullable = false)
    private Integer isTempSource;

    public Integer getFarmId() {
        return farmId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getFarmName() {
        return farmName;
    }

    public Integer getDisplayName() {
        return displayName;
    }

    public Integer getFarmType() {
        return farmType;
    }

    public Integer getTempSourceId() {
        return tempSourceId;
    }

    public Integer getIsTempSource() {
        return isTempSource;
    }

    protected CompanyFarmView() {
    }
}