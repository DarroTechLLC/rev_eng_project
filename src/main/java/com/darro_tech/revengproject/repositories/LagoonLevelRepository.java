package com.darro_tech.revengproject.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.darro_tech.revengproject.models.LagoonLevel;

@Repository
public interface LagoonLevelRepository extends JpaRepository<LagoonLevel, Integer> {

    List<LagoonLevel> findByFarmIdAndTimestampBetweenOrderByTimestampAsc(
            String farmId, Instant startDate, Instant endDate);

    /**
     * Find latest lagoon level for each farm in a company
     */
    @Query(value
            = "SELECT ll.* FROM lagoon_level ll "
            + "JOIN farms f ON ll.farm_id = f.id "
            + "JOIN company_farms cf ON f.id = cf.farm_id "
            + "WHERE cf.company_id = :companyId "
            + "AND ll.timestamp <= :maxDate "
            + "AND ll.id IN ("
            + "  SELECT MAX(ll2.id) FROM lagoon_level ll2 "
            + "  JOIN farms f2 ON ll2.farm_id = f2.id "
            + "  JOIN company_farms cf2 ON f2.id = cf2.farm_id "
            + "  WHERE cf2.company_id = :companyId "
            + "  AND ll2.farm_id = ll.farm_id "
            + "  AND ll2.timestamp <= :maxDate "
            + "  GROUP BY ll2.farm_id"
            + ") "
            + "ORDER BY f.name",
            nativeQuery = true)
    List<LagoonLevel> findLatestLagoonLevelsByCompanyId(
            @Param("companyId") String companyId,
            @Param("maxDate") Instant maxDate);

    /**
     * Find lagoon levels for farms in a company for a date range
     */
    @Query(value
            = "SELECT ll.* FROM lagoon_level ll "
            + "JOIN farms f ON ll.farm_id = f.id "
            + "JOIN company_farms cf ON f.id = cf.farm_id "
            + "WHERE cf.company_id = :companyId "
            + "AND ll.timestamp BETWEEN :startDate AND :endDate "
            + "ORDER BY ll.timestamp ASC",
            nativeQuery = true)
    List<LagoonLevel> findByCompanyIdAndTimestampBetween(
            @Param("companyId") String companyId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);
}
