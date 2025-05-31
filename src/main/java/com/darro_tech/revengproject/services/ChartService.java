package com.darro_tech.revengproject.services;

import com.darro_tech.revengproject.dto.FarmVolumeData;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.repositories.ChartMeterDailyViewRepository;
import com.darro_tech.revengproject.repositories.FarmRepository;
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

    @Autowired
    private ChartMeterDailyViewRepository chartMeterDailyViewRepository;

    @Autowired
    private FarmRepository farmRepository;

    /**
     * Get daily volume data for all farms in a company for a specific date
     */
    public List<FarmVolumeData> getDailyVolumeByFarmForDate(String companyId, LocalDate date) {
        System.out.println("=== ChartService: getDailyVolumeByFarmForDate ===");
        System.out.println("Input parameters: companyId=" + companyId + ", date=" + date);

        try {
            // Convert LocalDate to Instant at start of day
            Instant instantTimestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
            System.out.println("Converted date to instant: " + instantTimestamp);

            // Convert String companyId to Integer
            Integer companyIdInt = Integer.parseInt(companyId);
            System.out.println("Converted companyId to integer: " + companyIdInt);

            // Convert Instant to Integer timestamp (seconds since epoch)
            Integer timestamp = (int) instantTimestamp.getEpochSecond();
            System.out.println("Converted instant to epoch seconds: " + timestamp);

            // Get volume data grouped by farm
            System.out.println("Querying database for farm volumes...");
            List<Object[]> results = chartMeterDailyViewRepository.findTotalVolumeByFarmForDate(companyIdInt, timestamp);
            System.out.println("Query returned " + (results != null ? results.size() : 0) + " results");

            if (results == null || results.isEmpty()) {
                System.out.println("WARNING: No data found in database for the given parameters");
                return new ArrayList<>();
            }

            // Create a map of farm IDs to their names
            System.out.println("Loading farm names from database...");
            List<Farm> allFarms = farmRepository.findAll();
            System.out.println("Found " + allFarms.size() + " farms in database");

            Map<String, String> farmNames = allFarms.stream()
                .collect(Collectors.toMap(Farm::getId, Farm::getName));

            // Convert results to DTOs
            List<FarmVolumeData> farmVolumeDataList = new ArrayList<>();
            System.out.println("Converting query results to DTOs...");

            for (Object[] result : results) {
                Integer farmIdInt = (Integer) result[0];
                String farmId = farmIdInt.toString(); // Convert to String for DTO
                Double volume = ((Number) result[1]).doubleValue();

                System.out.println("Processing farm: ID=" + farmId + ", volume=" + volume);

                FarmVolumeData farmVolumeData = new FarmVolumeData();
                farmVolumeData.setFarmId(farmId);
                farmVolumeData.setFarmName(farmNames.getOrDefault(farmId, farmId));
                farmVolumeData.setVolume(volume);

                System.out.println("Created DTO: farmId=" + farmVolumeData.getFarmId() + 
                                   ", farmName=" + farmVolumeData.getFarmName() + 
                                   ", volume=" + farmVolumeData.getVolume());

                farmVolumeDataList.add(farmVolumeData);
            }

            System.out.println("Returning " + farmVolumeDataList.size() + " farm volume records");
            return farmVolumeDataList;
        } catch (Exception e) {
            System.out.println("ERROR in getDailyVolumeByFarmForDate: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
