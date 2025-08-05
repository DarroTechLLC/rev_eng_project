package com.darro_tech.revengproject.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.CompanyUser;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.repositories.CompanyRepository;
import com.darro_tech.revengproject.repositories.CompanyUserRepository;
import com.darro_tech.revengproject.repositories.UserRepository;
import com.darro_tech.revengproject.util.LoggerUtils;

@Service
public class UserService {

    private static final Logger logger = LoggerUtils.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyUserRepository companyUserRepository;

    @Autowired
    private CompanyRepository companyRepository;

    /**
     * Get all companies that the user has access to
     *
     * @param userId user ID
     * @return List of Company objects
     */
    @Transactional(readOnly = true)
    public List<Company> getUserCompanies(String userId) {
        logger.debug("Getting companies for user ID: {}", userId);

        // Get all company-user associations for this user
        List<CompanyUser> companyUsers = companyUserRepository.findByUserId(userId);

        // Get the actual Company objects
        List<Company> companies = new ArrayList<>();
        for (CompanyUser cu : companyUsers) {
            Company company = cu.getCompany();
            if (company != null) {
                companies.add(company);
            }
        }

        logger.debug("Found {} companies for user ID: {}", companies.size(), userId);
        return companies;
    }

    /**
     * Get user by ID
     *
     * @param id user ID
     * @return Optional containing User if found
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(String id) {
        logger.debug("Getting user by ID: {}", id);
        return userRepository.findById(id);
    }

    /**
     * Get user by ID with roles and companies initialized
     *
     * @param id user ID
     * @return Optional containing User if found with initialized collections
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserWithCollections(String id) {
        logger.debug("Getting user with initialized collections by ID: {}", id);
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Initialize the lazy collections
            Hibernate.initialize(user.getRoles());
            Hibernate.initialize(user.getCompanies());
            logger.debug("Successfully initialized collections for user: {}", user.getUsername());
        }

        return userOpt;
    }

    /**
     * Find a user by username
     *
     * @param username the username to search for
     * @return the User object or null if not found
     */
    @Transactional(readOnly = true)
    public User findUserByUsername(String username) {
        logger.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Find a user by username with roles and companies initialized
     *
     * @param username the username to search for
     * @return the User object with initialized collections or null if not found
     */
    @Transactional(readOnly = true)
    public User findUserByUsernameWithCollections(String username) {
        logger.debug("Finding user with initialized collections by username: {}", username);
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Initialize the lazy collections
            Hibernate.initialize(user.getRoles());
            Hibernate.initialize(user.getCompanies());
            logger.debug("Successfully initialized collections for user: {}", user.getUsername());
            return user;
        }

        return null;
    }
}
