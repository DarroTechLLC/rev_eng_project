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
    private String userId;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "logo_url")
    private String logoUrl;

    public String getUserId() {
        return userId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    protected UserCompanyView() {
    }
}