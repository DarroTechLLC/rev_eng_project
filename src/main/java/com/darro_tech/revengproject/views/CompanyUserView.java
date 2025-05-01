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
    private String userId;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    public String getUserId() {
        return userId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getDisplayName() {
        return displayName;
    }

    protected CompanyUserView() {
    }
}