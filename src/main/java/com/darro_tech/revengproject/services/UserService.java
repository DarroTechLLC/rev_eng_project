package com.darro_tech.revengproject.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.CompanyUser;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.repositories.CompanyRepository;
import com.darro_tech.revengproject.repositories.CompanyUserRepository;
import com.darro_tech.revengproject.repositories.UserRepository;

@Service
public class UserService {

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
    public List<Company> getUserCompanies(String userId) {
        System.out.println("UserService: Getting companies for user ID: " + userId);

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

        System.out.println("UserService: Found " + companies.size() + " companies for user ID: " + userId);
        return companies;
    }

    /**
     * Get user by ID
     *
     * @param id user ID
     * @return Optional containing User if found
     */
    public Optional<User> getUserById(String id) {
        System.out.println("UserService: Getting user by ID: " + id);
        return userRepository.findById(id);
    }

    /**
     * Find a user by username
     *
     * @param username the username to search for
     * @return the User object or null if not found
     */
    public User findUserByUsername(String username) {
        System.out.println("UserService: Finding user by username: " + username);
        return userRepository.findByUsername(username);
    }
}
