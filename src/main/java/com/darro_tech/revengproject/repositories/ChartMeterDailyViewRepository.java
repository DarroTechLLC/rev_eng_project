package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.views.ChartMeterDailyView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChartMeterDailyViewRepository extends JpaRepository<ChartMeterDailyView, Integer> {

    @Query("SELECT c FROM ChartMeterDailyView c WHERE c.companyId = :companyId AND DATE(c.timestamp) = :date")
    List<ChartMeterDailyView> findByCompanyIdAndDate(@Param("companyId") String companyId, @Param("date") LocalDate date);

    @Query("SELECT c.farmId as farmId, SUM(c.value) as totalValue FROM ChartMeterDailyView c WHERE c.companyId = :companyId AND DATE(c.timestamp) = :date GROUP BY c.farmId")
    List<Object[]> findTotalVolumeByFarmForDate(@Param("companyId") String companyId, @Param("date") LocalDate date);

    /**
     * Custom query that bypasses the chart_meter_daily_view and directly queries the underlying tables
     * without filtering by include_website flag
     */
    @Query(value = 
        "SELECT m.farm_id as farmId, SUM(md.value) as totalValue " +
        "FROM meter_daily md " +
        "JOIN meters m ON md.meter_id = m.id " +
        "JOIN farms f ON m.farm_id = f.id " +
        "JOIN company_meters cm ON m.id = cm.meter_id " +
        "WHERE cm.company_id = :companyId AND DATE(md.timestamp) = :date " +
        "GROUP BY m.farm_id", 
        nativeQuery = true)
    List<Object[]> findTotalVolumeByFarmForDateIgnoringIncludeWebsite(
        @Param("companyId") String companyId, 
        @Param("date") LocalDate date);

    /**
     * Custom query that bypasses the chart_meter_daily_view and directly queries the underlying tables
     * for a date range without filtering by include_website flag
     */
    @Query(value = 
        "SELECT m.farm_id as farmId, SUM(md.value) as totalValue " +
        "FROM meter_daily md " +
        "JOIN meters m ON md.meter_id = m.id " +
        "JOIN farms f ON m.farm_id = f.id " +
        "JOIN company_meters cm ON m.id = cm.meter_id " +
        "WHERE cm.company_id = :companyId " +
        "AND DATE(md.timestamp) >= :fromDate " +
        "AND DATE(md.timestamp) <= :toDate " +
        "GROUP BY m.farm_id", 
        nativeQuery = true)
    List<Object[]> findTotalVolumeByFarmForDateRangeIgnoringIncludeWebsite(
        @Param("companyId") String companyId, 
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate);

    /**
     * Custom query that gets daily total production for a company over a date range
     * without filtering by include_website flag
     */
    @Query(value = 
        "SELECT DATE(md.timestamp) as date, SUM(md.value) as totalValue " +
        "FROM meter_daily md " +
        "JOIN meters m ON md.meter_id = m.id " +
        "JOIN company_meters cm ON m.id = cm.meter_id " +
        "WHERE cm.company_id = :companyId " +
        "AND DATE(md.timestamp) >= :fromDate " +
        "AND DATE(md.timestamp) <= :toDate " +
        "GROUP BY DATE(md.timestamp) " +
        "ORDER BY DATE(md.timestamp)", 
        nativeQuery = true)
    List<Object[]> findDailyProductionForCompanyDateRange(
        @Param("companyId") String companyId, 
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate);
}
