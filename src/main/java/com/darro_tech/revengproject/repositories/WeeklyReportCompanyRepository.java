package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.models.WeeklyReportCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for WeeklyReportCompany entities
 */
@Repository
public interface WeeklyReportCompanyRepository extends JpaRepository<WeeklyReportCompany, Integer> {

    /**
     * Find the most recent weekly report for a company
     * 
     * @param companyId The ID of the company
     * @return The most recent weekly report, if any
     */
    @Query(value = "SELECT DISTINCT w FROM WeeklyReportCompany w JOIN FETCH w.company c WHERE c.id = :companyId ORDER BY w.timestamp DESC", nativeQuery = false)
    List<WeeklyReportCompany> findLatestByCompanyId(@Param("companyId") String companyId);

    /**
     * Find the weekly report for a company closest to the given date
     * 
     * @param companyId The ID of the company
     * @param date The date to find the report for
     * @return The weekly report closest to the given date, if any
     */
    @Query(value = "SELECT DISTINCT w FROM WeeklyReportCompany w JOIN FETCH w.company c WHERE c.id = :companyId AND w.timestamp <= :date ORDER BY w.timestamp DESC", nativeQuery = false)
    List<WeeklyReportCompany> findByCompanyIdAndDate(@Param("companyId") String companyId, @Param("date") Instant date);

    /**
     * Find weekly reports for a company within a date range
     * 
     * @param companyId The ID of the company
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return Weekly reports within the date range, if any
     */
    @Query(value = "SELECT DISTINCT w FROM WeeklyReportCompany w JOIN FETCH w.company c WHERE c.id = :companyId AND w.timestamp >= :startDate AND w.timestamp < :endDate ORDER BY w.timestamp DESC", nativeQuery = false)
    List<WeeklyReportCompany> findByCompanyIdAndDateBetween(@Param("companyId") String companyId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}
