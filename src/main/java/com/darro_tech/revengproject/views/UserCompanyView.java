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
@Table(name = "user_company_view")
public class UserCompanyView {
    @Id
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "company_id", nullable = false)
    private Integer companyId;

    @Column(name = "company_name", nullable = false)
    private Integer companyName;

    @Column(name = "display_name", nullable = false)
    private Integer displayName;

    @Column(name = "logo_url", nullable = false)
    private Integer logoUrl;

    public Integer getUserId() {
        return userId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getCompanyName() {
        return companyName;
    }

    public Integer getDisplayName() {
        return displayName;
    }

    public Integer getLogoUrl() {
        return logoUrl;
    }

    protected UserCompanyView() {
    }
}