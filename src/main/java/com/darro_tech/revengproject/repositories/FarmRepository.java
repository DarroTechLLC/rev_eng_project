package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.models.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FarmRepository extends JpaRepository<Farm, String> {
    // All basic CRUD operations are provided by JpaRepository
} 