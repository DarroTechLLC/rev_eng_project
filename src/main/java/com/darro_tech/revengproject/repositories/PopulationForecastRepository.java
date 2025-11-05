package com.darro_tech.revengproject.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.darro_tech.revengproject.models.PopulationForecast;

@Repository
public interface PopulationForecastRepository extends JpaRepository<PopulationForecast, Integer> {

    List<PopulationForecast> findByFarmIdAndTimestampBetweenOrderByTimestampAsc(
            String farmId, Instant startDate, Instant endDate);

    @Query(value
            = "SELECT pf.* FROM population_forecast pf "
            + "JOIN farms f ON pf.farm_id = f.id "
            + "JOIN company_farms cf ON f.id = cf.farm_id "
            + "WHERE cf.company_id = :companyId "
            + "AND pf.timestamp BETWEEN :fromDate AND :toDate "
            + "ORDER BY pf.timestamp ASC",
            nativeQuery = true)
    List<PopulationForecast> findByCompanyIdAndTimestampBetween(
            @Param("companyId") String companyId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate);
}


