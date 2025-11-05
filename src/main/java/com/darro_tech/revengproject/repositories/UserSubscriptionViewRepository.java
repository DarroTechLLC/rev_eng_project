package com.darro_tech.revengproject.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.darro_tech.revengproject.models.UserSubscriptionView;
import com.darro_tech.revengproject.models.UserSubscriptionViewId;

@Repository
public interface UserSubscriptionViewRepository extends JpaRepository<UserSubscriptionView, UserSubscriptionViewId> {

    @Query("SELECT u FROM UserSubscriptionView u ORDER BY u.companyDisplayName ASC, u.lastName ASC, u.firstName ASC, u.subscriptionKey ASC")
    List<UserSubscriptionView> findAllOrdered();
}



