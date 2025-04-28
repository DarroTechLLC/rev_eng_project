package com.darro_tech.revengproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darro_tech.revengproject.models.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {
    // Add custom queries if needed
} 