package com.darro_tech.revengproject.views;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/**
 * Mapping for DB view
 */
@Entity
@Immutable
@Table(name = "admin_user_view")
public class AdminUserView {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(nullable = false)
    private Long id;

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

    @Column(name = "email", nullable = false)
    private Integer email;

    @Column(name = "sms", nullable = false)
    private Integer sms;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getEmail() {
        return email;
    }

    public Integer getSms() {
        return sms;
    }

    protected AdminUserView() {
    }
}