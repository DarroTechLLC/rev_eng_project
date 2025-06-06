package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.models.DailyReportCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DailyReportCompany entities
 */
@Repository
public interface DailyReportCompanyRepository extends JpaRepository<DailyReportCompany, Integer> {

    /**
     * Find the most recent daily report for a company
     * 
     * @param companyId The ID of the company
     * @return The most recent daily report, if any
     */
    @Query(value = "SELECT DISTINCT d FROM DailyReportCompany d JOIN FETCH d.company c WHERE c.id = :companyId ORDER BY d.timestamp DESC", nativeQuery = false)
    List<DailyReportCompany> findLatestByCompanyId(@Param("companyId") String companyId);

    /**
     * Find the daily report for a company closest to the given date
     * 
     * @param companyId The ID of the company
     * @param date The date to find the report for
     * @return The daily report closest to the given date, if any
     */
    @Query(value = "SELECT DISTINCT d FROM DailyReportCompany d JOIN FETCH d.company c WHERE c.id = :companyId AND d.timestamp <= :date ORDER BY d.timestamp DESC", nativeQuery = false)
    List<DailyReportCompany> findByCompanyIdAndDate(@Param("companyId") String companyId, @Param("date") Instant date);
}
