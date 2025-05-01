package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.models.CompanyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyUserRepository extends JpaRepository<CompanyUser, String> {
    
    /**
     * Find all company-user associations for a specific user
     * @param userId The user ID
     * @return List of CompanyUser objects
     */
    List<CompanyUser> findByUserId(String userId);
    
    /**
     * Find all company-user associations for a specific company
     * @param companyId The company ID
     * @return List of CompanyUser objects
     */
    List<CompanyUser> findByCompanyId(String companyId);
    
    /**
     * Find a specific company-user association
     * @param companyId The company ID
     * @param userId The user ID
     * @return Optional containing the association if found
     */
    Optional<CompanyUser> findByCompanyIdAndUserId(String companyId, String userId);
} 