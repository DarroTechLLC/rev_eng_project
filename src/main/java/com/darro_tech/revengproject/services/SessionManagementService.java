package com.darro_tech.revengproject.services;

import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.repositories.UserRepository;

import jakarta.servlet.http.HttpSession;

/**
 * Service for managing session-related operations
 */
@Service
public class SessionManagementService {

    private static final Logger logger = Logger.getLogger(SessionManagementService.class.getName());

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get the currently authenticated user from the session
     */
    public User getUserFromSession(HttpSession session) {
        if (session == null) {
            logger.warning("⚠️ Session is null when attempting to get user");
            return null;
        }

        Object userAttr = session.getAttribute("user");

        if (userAttr == null) {
            logger.warning("⚠️ No user attribute found in session");
            return null;
        }

        logger.info("🔍 Retrieved user attribute from session of type: " + userAttr.getClass().getName());

        // Handle the case when the attribute is a String (user ID)
        if (userAttr instanceof String userId) {
            logger.info("🔄 Converting user ID to User object: " + userId);
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                logger.info("✅ Successfully retrieved User from repository: " + user.getUsername());
                return user;
            } else {
                logger.severe("❌ User with ID not found in database: " + userId);
                return null;
            }
        }

        // Handle the case when the attribute is already a User object
        if (userAttr instanceof User user) {
            logger.info("✅ User object already in session: " + user.getUsername());
            return user;
        }

        // If we get here, the attribute is neither a String nor a User
        logger.severe("❌ Session attribute 'user' is of unexpected type: " + userAttr.getClass().getName());
        return null;
    }

    /**
     * Set the currently authenticated user in the session Store only the user
     * ID to prevent serialization issues
     */
    public void setUserInSession(HttpSession session, User user) {
        if (session != null && user != null) {
            // Store only the user ID in the session for consistency with AuthenticationController
            session.setAttribute("user", user.getId());
            logger.info("🔑 User ID set in session: " + user.getId() + " (username: " + user.getUsername() + ")");
        } else {
            logger.warning("⚠️ Unable to set user in session - session or user is null");
        }
    }

    /**
     * Get the selected company ID from session
     */
    public String getSelectedCompanyId(HttpSession session) {
        if (session == null) {
            logger.warning("⚠️ Session is null when attempting to get selected company ID");
            return null;
        }

        String companyId = (String) session.getAttribute("selectedCompanyId");
        logger.info("🏢 Retrieved company ID from session: " + companyId);
        return companyId;
    }

    /**
     * Set the selected company in session
     */
    public void setSelectedCompany(HttpSession session, String companyId, String source) {
        if (session != null && companyId != null) {
            session.setAttribute("selectedCompanyId", companyId);
            logger.info("🏢 Company selected in session (by " + source + "): " + companyId);
        } else {
            logger.warning("⚠️ Unable to set company in session - session or companyId is null");
        }
    }

    /**
     * Get the selected farm ID from session
     */
    public String getSelectedFarmId(HttpSession session) {
        if (session == null) {
            logger.warning("⚠️ Session is null when attempting to get selected farm ID");
            return null;
        }

        String farmId = (String) session.getAttribute("selectedFarmKey");
        logger.info("🚜 Retrieved farm ID from session: " + farmId);
        return farmId;
    }

    /**
     * Set the selected farm in session
     */
    public void setSelectedFarm(HttpSession session, String farmId, String source) {
        if (session != null && farmId != null) {
            session.setAttribute("selectedFarmKey", farmId);
            logger.info("🚜 Farm selected in session (by " + source + "): " + farmId);
        } else {
            logger.warning("⚠️ Unable to set farm in session - session or farmId is null");
        }
    }

    /**
     * Clear user-related data from the session
     */
    public void clearUserSession(HttpSession session) {
        if (session != null) {
            Object userAttr = session.getAttribute("user");
            logger.info("🧹 Clearing user session data - current user attr type: "
                    + (userAttr != null ? userAttr.getClass().getName() : "null"));

            session.removeAttribute("user");
            session.removeAttribute("selectedCompanyId");
            session.removeAttribute("selectedFarmKey");
            logger.info("🧹 User session cleared successfully");
        } else {
            logger.warning("⚠️ Unable to clear user session - session is null");
        }
    }
}
