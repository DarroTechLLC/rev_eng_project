package com.darro_tech.revengproject.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.UserSubscriptionView;
import com.darro_tech.revengproject.repositories.UserSubscriptionViewRepository;

@Service
@Transactional(readOnly = true)
public class UserSubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(UserSubscriptionService.class);

    @Autowired
    private UserSubscriptionViewRepository userSubscriptionViewRepository;

    @Autowired
    private UserRoleService userRoleService;

    /**
     * List user subscriptions (super admin only)
     *
     * @param user the current user
     * @return list of user subscriptions
     * @throws SecurityException if user is not super admin
     */
    public List<UserSubscriptionView> listUserSubscriptions(User user) {
        // Check super admin authorization
        if (!userRoleService.isSuperAdmin(user)) {
            log.warn("❌ Unauthorized access attempt to user subscriptions by user: {}",
                    user != null ? user.getUsername() : "unknown");
            throw new SecurityException("Super admin access required");
        }

        log.info("📋 Listing user subscriptions");

        List<UserSubscriptionView> subscriptions = userSubscriptionViewRepository.findAllOrdered();

        log.info("✅ Retrieved {} user subscriptions", subscriptions.size());
        return subscriptions;
    }
}



