package com.darro_tech.revengproject.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darro_tech.revengproject.models.EmailLogView;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.repositories.EmailLogViewRepository;

@Service
@Transactional(readOnly = true)
public class EmailLogService {

    private static final Logger log = LoggerFactory.getLogger(EmailLogService.class);

    @Autowired
    private EmailLogViewRepository emailLogViewRepository;

    @Autowired
    private UserRoleService userRoleService;

    /**
     * List email logs (super admin only)
     *
     * @param user the current user
     * @param limit maximum number of records to return
     * @return list of email logs
     * @throws SecurityException if user is not super admin
     */
    public List<EmailLogView> listEmailLogs(User user, Integer limit) {
        // Check super admin authorization
        if (!userRoleService.isSuperAdmin(user)) {
            log.warn("❌ Unauthorized access attempt to email logs by user: {}",
                    user != null ? user.getUsername() : "unknown");
            throw new SecurityException("Super admin access required");
        }

        log.info("📧 Listing email logs (limit: {})", limit != null ? limit : 1000);

        int actualLimit = limit != null && limit > 0 ? limit : 1000;
        Pageable pageable = PageRequest.of(0, actualLimit);

        List<EmailLogView> logs = emailLogViewRepository.findAllOrderByCreatedAtDesc(pageable);

        log.info("✅ Retrieved {} email logs", logs.size());
        return logs;
    }
}



