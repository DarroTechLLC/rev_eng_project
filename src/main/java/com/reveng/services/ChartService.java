package com.reveng.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Service
public class ChartService {
    private static final Logger logger = LoggerFactory.getLogger(ChartService.class);
    
    @Autowired
    private EntityManager entityManager;
    
    /**
     * Retrieves daily volume data for charting
     * @return Map containing chart data and labels
     */
    public Map<String, Object> getDailyVolumeData() {
        logger.info("📊 Fetching daily volume data for chart");
        
        try {
            // Query to get daily volumes
            String queryStr = "SELECT DATE(created_at) as date, COUNT(*) as volume " +
                            "FROM your_table_name " + // Replace with actual table name
                            "WHERE created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY) " +
                            "GROUP BY DATE(created_at) " +
                            "ORDER BY date";
            
            Query query = entityManager.createNativeQuery(queryStr);
            List<Object[]> results = query.getResultList();
            
            // Process results
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
            List<String> labels = new ArrayList<>();
            List<Long> data = new ArrayList<>();
            
            for (Object[] row : results) {
                LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
                labels.add(date.format(formatter));
                data.add(((Number) row[1]).longValue());
            }
            
            logger.info("📈 Successfully processed {} data points", results.size());
            
            // Return processed data
            Map<String, Object> chartData = new LinkedHashMap<>();
            chartData.put("labels", labels);
            chartData.put("data", data);
            return chartData;
            
        } catch (Exception e) {
            logger.error("❌ Error fetching chart data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch chart data", e);
        }
    }
} 