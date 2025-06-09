package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.models.Ch4RecoveryDev;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface Ch4RecoveryDevRepository extends JpaRepository<Ch4RecoveryDev, Integer> {
    
    List<Ch4RecoveryDev> findByFarmIdAndTimestampBetweenOrderByTimestampAsc(
            String farmId, Instant startDate, Instant endDate);
}