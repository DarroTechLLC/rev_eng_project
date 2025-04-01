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
@Table(name = "company_user_view")
public class CompanyUserView {
    @Id
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "company_id", nullable = false)
    private Integer companyId;

    @Column(name = "username", nullable = false)
    private Integer username;

    @Column(name = "firstName", nullable = false)
    private Integer firstName;

    @Column(name = "lastName", nullable = false)
    private Integer lastName;

    @Column(name = "company_name", nullable = false)
    private Integer companyName;

    @Column(name = "display_name", nullable = false)
    private Integer displayName;

    public Integer getUserId() {
        return userId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getUsername() {
        return username;
    }

    public Integer getFirstName() {
        return firstName;
    }

    public Integer getLastName() {
        return lastName;
    }

    public Integer getCompanyName() {
        return companyName;
    }

    public Integer getDisplayName() {
        return displayName;
    }

    protected CompanyUserView() {
    }
}