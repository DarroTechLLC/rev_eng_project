package com.darro_tech.revengproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darro_tech.revengproject.models.UserSubscription;
import com.darro_tech.revengproject.models.UserSubscriptionId;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UserSubscriptionId> {
}



