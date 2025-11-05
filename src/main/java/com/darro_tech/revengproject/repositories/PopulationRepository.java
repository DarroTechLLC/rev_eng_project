package com.darro_tech.revengproject.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.darro_tech.revengproject.models.Population;

@Repository
public interface PopulationRepository extends JpaRepository<Population, Integer> {

    List<Population> findByFarmIdAndTimestampBetweenOrderByTimestampAsc(
            String farmId, Instant startDate, Instant endDate);

    @Query(value
            = "SELECT p.* FROM population p "
            + "JOIN farms f ON p.farm_id = f.id "
            + "JOIN company_farms cf ON f.id = cf.farm_id "
            + "WHERE cf.company_id = :companyId "
            + "AND p.timestamp BETWEEN :fromDate AND :toDate "
            + "ORDER BY p.timestamp ASC",
            nativeQuery = true)
    List<Population> findByCompanyIdAndTimestampBetween(
            @Param("companyId") String companyId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate);

    @Query(value
            = "SELECT p.farm_id as farmId, SUM(p.value) as totalValue "
            + "FROM population p "
            + "JOIN farms f ON p.farm_id = f.id "
            + "JOIN company_farms cf ON f.id = cf.farm_id "
            + "WHERE cf.company_id = :companyId "
            + "AND p.timestamp BETWEEN :fromDate AND :toDate "
            + "GROUP BY p.farm_id",
            nativeQuery = true)
    List<Object[]> findTotalPopulationByFarmForCompanyAndDateRange(
            @Param("companyId") String companyId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate);
}


