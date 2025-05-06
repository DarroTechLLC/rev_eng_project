package com.darro_tech.revengproject.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darro_tech.revengproject.models.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {

    // Find a company by name, case insensitive
    Optional<Company> findByNameIgnoreCase(String name);

    // Find all companies with names matching a list of values
    List<Company> findByNameIgnoreCaseIn(List<String> names);
}
