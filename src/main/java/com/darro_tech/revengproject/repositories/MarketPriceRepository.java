package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.models.MarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MarketPriceRepository extends JpaRepository<MarketPrice, Integer> {
    
    @Query("SELECT m FROM MarketPrice m WHERE m.timestamp >= :fromDate AND m.timestamp <= :toDate ORDER BY m.timestamp ASC")
    List<MarketPrice> findByTimestampBetweenOrderByTimestampAsc(
            @Param("fromDate") Instant fromDate, 
            @Param("toDate") Instant toDate);
}