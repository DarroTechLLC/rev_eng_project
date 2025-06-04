package com.darro_tech.revengproject.services;

import com.darro_tech.revengproject.dto.FarmVolumeData;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.repositories.ChartMeterDailyViewRepository;
import com.darro_tech.revengproject.repositories.FarmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChartService {

    private static final Logger logger = LoggerFactory.getLogger(ChartService.class);

    @Autowired
    private ChartMeterDailyViewRepository chartMeterDailyViewRepository;

    @Autowired
    private FarmRepository farmRepository;

    /**
     * Get daily volume data for all farms in a company for a specific date
     */
    public List<FarmVolumeData> getDailyVolumeByFarmForDate(String companyId, LocalDate date) {
        logger.info("🔍 Fetching daily volume data for company: {} on date: {}", companyId, date);

        try {
            // Get volume data grouped by farm using the method that ignores include_website flag
            logger.info("🔍 Using custom query that ignores include_website flag");
            logger.debug("Querying database for farm volumes...");
            List<Object[]> results = chartMeterDailyViewRepository.findTotalVolumeByFarmForDateIgnoringIncludeWebsite(companyId, date);
            logger.info("Query returned {} results", results != null ? results.size() : 0);

            if (results == null || results.isEmpty()) {
                logger.warn("No data found in database for the given parameters");
                return new ArrayList<>();
            }

            // Create a map of farm IDs to their names
            logger.debug("Loading farm names from database...");
            List<Farm> allFarms = farmRepository.findAll();
            logger.debug("Found {} farms in database", allFarms.size());

            Map<String, String> farmNames = allFarms.stream()
                .collect(Collectors.toMap(Farm::getId, Farm::getName));

            // Convert results to DTOs
            List<FarmVolumeData> farmVolumeDataList = new ArrayList<>();
            logger.debug("Converting query results to DTOs...");

            for (Object[] result : results) {
                String farmId = (String) result[0]; // Now directly a String
                Double volume = ((Number) result[1]).doubleValue();

                logger.debug("Processing farm: ID={}, volume={}", farmId, volume);

                FarmVolumeData farmVolumeData = new FarmVolumeData();
                farmVolumeData.setFarm_id(farmId);
                farmVolumeData.setFarmName(farmNames.getOrDefault(farmId, farmId));
                farmVolumeData.setVolume(volume);

                logger.debug("Created DTO: farm_id={}, farmName={}, volume={}", 
                           farmVolumeData.getFarm_id(), 
                           farmVolumeData.getFarmName(), 
                           farmVolumeData.getVolume());

                farmVolumeDataList.add(farmVolumeData);
            }

            // Log data presence verification
            logger.info("📊 Data verification:");
            logger.info("✓ Total records: {}", farmVolumeDataList.size());
            logger.info("✓ Total volume: {}", 
                farmVolumeDataList.stream().mapToDouble(FarmVolumeData::getVolume).sum());

            farmVolumeDataList.forEach(data -> {
                logger.info("✓ Farm {} data present: {}", 
                    data.getFarm_id(), 
                    data.getVolume() != null);
            });

            return farmVolumeDataList;
        } catch (Exception e) {
            logger.error("❌ Error in getDailyVolumeByFarmForDate: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get volume data for all farms in a company for a date range
     */
    public List<FarmVolumeData> getVolumeByFarmForDateRange(String companyId, LocalDate fromDate, LocalDate toDate) {
        logger.info("🔍 Fetching volume data for company: {} from {} to {}", companyId, fromDate, toDate);

        try {
            // Get volume data grouped by farm using the method that ignores include_website flag
            logger.info("🔍 Using custom query that ignores include_website flag");
            logger.debug("Querying database for farm volumes...");
            List<Object[]> results = chartMeterDailyViewRepository.findTotalVolumeByFarmForDateRangeIgnoringIncludeWebsite(
                companyId, fromDate, toDate);
            logger.info("Query returned {} results", results != null ? results.size() : 0);

            if (results == null || results.isEmpty()) {
                logger.warn("No data found in database for the given parameters");
                return new ArrayList<>();
            }

            // Create a map of farm IDs to their names
            logger.debug("Loading farm names from database...");
            List<Farm> allFarms = farmRepository.findAll();
            logger.debug("Found {} farms in database", allFarms.size());

            Map<String, String> farmNames = allFarms.stream()
                .collect(Collectors.toMap(Farm::getId, Farm::getName));

            // Convert results to DTOs
            List<FarmVolumeData> farmVolumeDataList = new ArrayList<>();
            logger.debug("Converting query results to DTOs...");

            for (Object[] result : results) {
                String farmId = (String) result[0];
                Double volume = ((Number) result[1]).doubleValue();

                logger.debug("Processing farm: ID={}, volume={}", farmId, volume);

                FarmVolumeData farmVolumeData = new FarmVolumeData();
                farmVolumeData.setFarm_id(farmId);
                farmVolumeData.setFarmName(farmNames.getOrDefault(farmId, farmId));
                farmVolumeData.setVolume(volume);

                logger.debug("Created DTO: farm_id={}, farmName={}, volume={}", 
                           farmVolumeData.getFarm_id(), 
                           farmVolumeData.getFarmName(), 
                           farmVolumeData.getVolume());

                farmVolumeDataList.add(farmVolumeData);
            }

            // Log data presence verification
            logger.info("📊 Data verification:");
            logger.info("✓ Total records: {}", farmVolumeDataList.size());
            logger.info("✓ Total volume: {}", 
                farmVolumeDataList.stream().mapToDouble(FarmVolumeData::getVolume).sum());

            farmVolumeDataList.forEach(data -> {
                logger.info("✓ Farm {} data present: {}", 
                    data.getFarm_id(), 
                    data.getVolume() != null);
            });

            return farmVolumeDataList;
        } catch (Exception e) {
            logger.error("❌ Error in getVolumeByFarmForDateRange: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
