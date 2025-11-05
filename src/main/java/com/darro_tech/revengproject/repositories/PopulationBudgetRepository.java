package com.darro_tech.revengproject.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.darro_tech.revengproject.models.PopulationBudget;

@Repository
public interface PopulationBudgetRepository extends JpaRepository<PopulationBudget, Integer> {

    List<PopulationBudget> findByFarmIdAndTimestampBetweenOrderByTimestampAsc(
            String farmId, Instant startDate, Instant endDate);

    @Query(value
            = "SELECT pb.* FROM population_budget pb "
            + "JOIN farms f ON pb.farm_id = f.id "
            + "JOIN company_farms cf ON f.id = cf.farm_id "
            + "WHERE cf.company_id = :companyId "
            + "AND pb.timestamp BETWEEN :fromDate AND :toDate "
            + "ORDER BY pb.timestamp ASC",
            nativeQuery = true)
    List<PopulationBudget> findByCompanyIdAndTimestampBetween(
            @Param("companyId") String companyId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate);
}


