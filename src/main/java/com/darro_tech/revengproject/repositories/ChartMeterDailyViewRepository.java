package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.views.ChartMeterDailyView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ChartMeterDailyViewRepository extends JpaRepository<ChartMeterDailyView, Integer> {

    @Query("SELECT c FROM ChartMeterDailyView c WHERE c.companyId = :companyId AND c.timestamp = :timestamp")
    List<ChartMeterDailyView> findByCompanyIdAndTimestamp(@Param("companyId") Integer companyId, @Param("timestamp") Integer timestamp);

    @Query("SELECT c.farmId as farmId, SUM(c.value) as totalValue FROM ChartMeterDailyView c WHERE c.companyId = :companyId AND c.timestamp = :timestamp GROUP BY c.farmId")
    List<Object[]> findTotalVolumeByFarmForDate(@Param("companyId") Integer companyId, @Param("timestamp") Integer timestamp);
}
