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
@Table(name = "user_contact_view")
public class UserContactView {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "username", nullable = false)
    private Integer username;

    @Column(name = "firstName", nullable = false)
    private Integer firstName;

    @Column(name = "lastName", nullable = false)
    private Integer lastName;

    @Column(name = "email", nullable = false)
    private Integer email;

    @Column(name = "sms", nullable = false)
    private Integer sms;

    public Integer getId() {
        return id;
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

    public Integer getSms() {
        return sms;
    }

    protected UserContactView() {
    }
}