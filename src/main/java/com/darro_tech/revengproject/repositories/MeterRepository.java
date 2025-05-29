package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.models.Meter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeterRepository extends JpaRepository<Meter, String> {
    List<Meter> findByFarmId(String farmId);
} 