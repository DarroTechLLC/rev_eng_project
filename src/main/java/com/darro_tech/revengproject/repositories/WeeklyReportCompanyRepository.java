package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.models.WeeklyReportCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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
    @Query("SELECT w FROM WeeklyReportCompany w WHERE w.companyId = :companyId ORDER BY w.timestamp DESC")
    Optional<WeeklyReportCompany> findLatestByCompanyId(@Param("companyId") String companyId);
    
    /**
     * Find the weekly report for a company closest to the given date
     * 
     * @param companyId The ID of the company
     * @param date The date to find the report for
     * @return The weekly report closest to the given date, if any
     */
    @Query("SELECT w FROM WeeklyReportCompany w WHERE w.companyId = :companyId AND w.timestamp <= :date ORDER BY w.timestamp DESC")
    Optional<WeeklyReportCompany> findByCompanyIdAndDate(@Param("companyId") String companyId, @Param("date") Instant date);
}