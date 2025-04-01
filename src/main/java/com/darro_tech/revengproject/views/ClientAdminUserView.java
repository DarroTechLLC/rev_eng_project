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
@Table(name = "client_admin_user_view")
public class ClientAdminUserView {
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

    @Column(name = "email")
    private Integer email;

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

    public Integer getEmail() {
        return email;
    }

    protected ClientAdminUserView() {
    }
}