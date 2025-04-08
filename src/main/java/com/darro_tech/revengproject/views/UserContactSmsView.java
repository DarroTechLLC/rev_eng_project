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
@Table(name = "user_contact_sms_view")
public class UserContactSmsView {
    @Id
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "contact_id", nullable = false)
    private Integer contactId;

    @Column(name = "username", nullable = false)
    private Integer username;

    @Column(name = "sms", nullable = false)
    private Integer sms;

    public Integer getUserId() {
        return userId;
    }

    public Integer getContactId() {
        return contactId;
    }

    public Integer getUsername() {
        return username;
    }

    public Integer getSms() {
        return sms;
    }

    protected UserContactSmsView() {
    }
}