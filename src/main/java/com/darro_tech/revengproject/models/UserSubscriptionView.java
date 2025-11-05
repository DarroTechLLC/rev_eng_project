package com.darro_tech.revengproject.models;

import java.time.Instant;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * Mapping for DB view user_subscriptions_view
 */
@Entity
@Immutable
@Table(name = "user_subscriptions_view")
@IdClass(UserSubscriptionViewId.class)
public class UserSubscriptionView {

    @Id
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Id
    @Column(name = "company_id", nullable = false, length = 36)
    private String companyId;

    @Id
    @Column(name = "subscription_key", nullable = false, length = 50)
    private String subscriptionKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "username")
    private String username;

    @Column(name = "firstName", nullable = false)
    private String firstName;

    @Column(name = "lastName", nullable = false)
    private String lastName;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_display_name", nullable = false)
    private String companyDisplayName;

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getSubscriptionKey() {
        return subscriptionKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
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

    public String getCompanyDisplayName() {
        return companyDisplayName;
    }

    protected UserSubscriptionView() {
    }
}
