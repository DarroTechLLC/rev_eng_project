package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.models.CompanyFarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyFarmRepository extends JpaRepository<CompanyFarm, String> {
    
    List<CompanyFarm> findByCompanyId(String companyId);
    
    List<CompanyFarm> findByFarmId(String farmId);
    
    Optional<CompanyFarm> findByCompanyIdAndFarmId(String companyId, String farmId);
    
    void deleteByFarmId(String farmId);
    
    void deleteByCompanyId(String companyId);
} 