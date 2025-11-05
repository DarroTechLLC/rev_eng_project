package com.darro_tech.revengproject.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.darro_tech.revengproject.models.Budget;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {

    /**
     * Find budgets for a specific farm within a date range
     */
    @Query("SELECT b FROM Budget b WHERE b.farm.id = :farmId AND b.timestamp BETWEEN :fromDate AND :toDate")
    List<Budget> findByFarmIdAndTimestampBetween(
            @Param("farmId") String farmId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate);

    /**
     * Find budgets for farms in a company within a date range
     */
    @Query(value
            = "SELECT b.* FROM budget b "
            + "JOIN farms f ON b.farm_id = f.id "
            + "JOIN company_farms cf ON f.id = cf.farm_id "
            + "WHERE cf.company_id = :companyId "
            + "AND b.timestamp BETWEEN :fromDate AND :toDate",
            nativeQuery = true)
    List<Budget> findByCompanyIdAndTimestampBetween(
            @Param("companyId") String companyId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate);

    /**
     * Get total budget by farm for a company within a date range
     */
    @Query(value
            = "SELECT f.id as farmId, SUM(b.value) as totalBudget "
            + "FROM budget b "
            + "JOIN farms f ON b.farm_id = f.id "
            + "JOIN company_farms cf ON f.id = cf.farm_id "
            + "WHERE cf.company_id = :companyId "
            + "AND b.timestamp BETWEEN :fromDate AND :toDate "
            + "GROUP BY f.id",
            nativeQuery = true)
    List<Object[]> findTotalBudgetByFarmForCompanyAndDateRange(
            @Param("companyId") String companyId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate);

    /**
     * Find monthly budget totals for farms in a company
     */
    @Query(value
            = "SELECT f.id as farmId, "
            + "YEAR(b.timestamp) as year, "
            + "MONTH(b.timestamp) as month, "
            + "SUM(b.value) as totalBudget "
            + "FROM budget b "
            + "JOIN farms f ON b.farm_id = f.id "
            + "JOIN company_farms cf ON f.id = cf.farm_id "
            + "WHERE cf.company_id = :companyId "
            + "AND b.timestamp BETWEEN :fromDate AND :toDate "
            + "GROUP BY f.id, YEAR(b.timestamp), MONTH(b.timestamp) "
            + "ORDER BY f.id, YEAR(b.timestamp), MONTH(b.timestamp)",
            nativeQuery = true)
    List<Object[]> findMonthlyBudgetByFarmForCompanyAndDateRange(
            @Param("companyId") String companyId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate);
}
