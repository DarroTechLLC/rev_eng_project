package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.models.Ch4Recovery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface Ch4RecoveryRepository extends JpaRepository<Ch4Recovery, Integer> {

    List<Ch4Recovery> findByFarmIdAndTimestampBetweenOrderByTimestampAsc(
            String farmId, Instant startDate, Instant endDate);

    @Query(value
            = "SELECT cr.* FROM ch4_recovery cr "
            + "JOIN farms f ON cr.farm_id = f.id "
            + "JOIN company_farms cf ON f.id = cf.farm_id "
            + "WHERE cf.company_id = :companyId "
            + "AND cr.timestamp BETWEEN :fromDate AND :toDate "
            + "ORDER BY cr.timestamp ASC",
            nativeQuery = true)
    List<Ch4Recovery> findByCompanyIdAndTimestampBetween(
            @Param("companyId") String companyId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate);
}