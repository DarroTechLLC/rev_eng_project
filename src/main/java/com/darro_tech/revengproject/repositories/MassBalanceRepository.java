package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.models.MassBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MassBalanceRepository extends JpaRepository<MassBalance, Integer> {
    
    List<MassBalance> findByFarmIdAndTimestampBetweenOrderByTimestampAsc(
            String farmId, Instant startDate, Instant endDate);
}