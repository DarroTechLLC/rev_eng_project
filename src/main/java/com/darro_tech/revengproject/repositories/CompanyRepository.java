package com.darro_tech.revengproject.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.darro_tech.revengproject.models.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {
    // Find a company by name, case insensitive
    Optional<Company> findByNameIgnoreCase(String name);
    
    // Find by normalized name (useful for URL slugs)
    @Query("SELECT c FROM Company c WHERE LOWER(REPLACE(REPLACE(c.name, ' ', '-'), '_', '-')) = LOWER(:key)")
    Optional<Company> findByNameKey(@Param("key") String key);
} 