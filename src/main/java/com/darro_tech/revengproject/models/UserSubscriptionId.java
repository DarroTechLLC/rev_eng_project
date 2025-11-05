package com.darro_tech.revengproject.models;

import java.io.Serializable;
import java.util.Objects;

public class UserSubscriptionId implements Serializable {

    private String userId;
    private String companyId;
    private String subscriptionKey;

    public UserSubscriptionId() {
    }

    public UserSubscriptionId(String userId, String companyId, String subscriptionKey) {
        this.userId = userId;
        this.companyId = companyId;
        this.subscriptionKey = subscriptionKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getSubscriptionKey() {
        return subscriptionKey;
    }

    public void setSubscriptionKey(String subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserSubscriptionId that = (UserSubscriptionId) o;
        return Objects.equals(userId, that.userId)
                && Objects.equals(companyId, that.companyId)
                && Objects.equals(subscriptionKey, that.subscriptionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, companyId, subscriptionKey);
    }
}



