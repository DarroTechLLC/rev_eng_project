package com.darro_tech.revengproject.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darro_tech.revengproject.dto.DailyReportDTO;
import com.darro_tech.revengproject.models.CompanyFarm;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.repositories.ChartMeterDailyViewRepository;
import com.darro_tech.revengproject.repositories.CompanyFarmRepository;

/**
 * Service for fetching comprehensive daily report data
 */
@Service
public class DailyReportDataService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DailyReportDataService.class);

    @Autowired
    private ChartMeterDailyViewRepository chartMeterDailyViewRepository;


    @Autowired
    private CompanyFarmRepository companyFarmRepository;

    /**
     * Get comprehensive daily report data
     */
    public DailyReportDTO getDailyReportData(String companyId, LocalDate reportDate) {
        log.info("📊 Fetching daily report data for company: {} on date: {}", companyId, reportDate);

        DailyReportDTO.Builder builder = DailyReportDTO.builder()
                .companyId(companyId)
                .reportDate(reportDate);

        try {
            // Get company name
            CompanyFarm companyFarm = companyFarmRepository.findByCompanyId(companyId).stream()
                    .findFirst()
                    .orElse(null);
            if (companyFarm != null) {
                builder.companyName(companyFarm.getCompany().getName());
            }

            // Fetch all sections
            List<DailyReportDTO.FarmProduction> dailyProduction = null;
            List<DailyReportDTO.FarmProduction> mtdProduction = null;
            List<DailyReportDTO.FarmPerformance> farmPerformance = null;

            try {
                dailyProduction = fetchDailyProductionSummary(companyId, reportDate);
                builder.dailyProduction(dailyProduction);
            } catch (Exception e) {
                log.error("Error fetching daily production summary", e);
            }

            try {
                mtdProduction = fetchMtdProductionSummary(companyId, reportDate);
                builder.mtdProduction(mtdProduction);
            } catch (Exception e) {
                log.error("Error fetching MTD production summary", e);
            }

            try {
                farmPerformance = fetchFarmPerformance(companyId, reportDate);
                builder.farmPerformance(farmPerformance);
            } catch (Exception e) {
                log.error("Error fetching farm performance", e);
            }

            // Calculate totals
            double dailyTotal = dailyProduction != null
                    ? dailyProduction.stream()
                            .mapToDouble(DailyReportDTO.FarmProduction::getVolume)
                            .sum()
                    : 0.0;
            double mtdTotal = mtdProduction != null
                    ? mtdProduction.stream()
                            .mapToDouble(DailyReportDTO.FarmProduction::getVolume)
                            .sum()
                    : 0.0;
            double ytdTotal = farmPerformance != null
                    ? farmPerformance.stream()
                            .mapToDouble(DailyReportDTO.FarmPerformance::getYtdVolume)
                            .sum()
                    : 0.0;

            builder.dailyTotal(dailyTotal)
                    .mtdTotal(mtdTotal)
                    .ytdTotal(ytdTotal);

        } catch (Exception e) {
            log.error("Error building daily report data", e);
        }

        return builder.build();
    }

    /**
     * Fetch daily production summary per farm
     */
    private List<DailyReportDTO.FarmProduction> fetchDailyProductionSummary(String companyId, LocalDate reportDate) {
        log.info("Fetching daily production summary for company: {} on date: {}", companyId, reportDate);

        try {
            List<Object[]> results = chartMeterDailyViewRepository.findTotalVolumeByFarmForDateIgnoringIncludeWebsite(
                    companyId, reportDate);

            List<Farm> farms = getCompanyFarms(companyId);

            double totalVolume = 0.0;
            Map<String, Double> farmVolumes = new HashMap<>();

            for (Object[] result : results) {
                String farmId = (String) result[0];
                Double volume = ((Number) result[1]).doubleValue();
                farmVolumes.put(farmId, volume);
                totalVolume += volume;
            }

            List<DailyReportDTO.FarmProduction> dailyProduction = new ArrayList<>();
            for (Farm farm : farms) {
                Double volume = farmVolumes.getOrDefault(farm.getId(), 0.0);
                double percentOfTotal = totalVolume > 0 ? (volume / totalVolume) * 100 : 0.0;

                dailyProduction.add(DailyReportDTO.FarmProduction.builder()
                        .farmName(farm.getName())
                        .volume(volume)
                        .percentOfTotal(percentOfTotal)
                        .build());
            }

            return dailyProduction;
        } catch (Exception e) {
            log.error("Error fetching daily production summary", e);
            return Collections.emptyList();
        }
    }

    /**
     * Fetch month-to-date production summary per farm
     */
    private List<DailyReportDTO.FarmProduction> fetchMtdProductionSummary(String companyId, LocalDate reportDate) {
        log.info("Fetching MTD production summary for company: {} up to date: {}", companyId, reportDate);

        try {
            LocalDate fromMTD = reportDate.withDayOfMonth(1);

            List<Object[]> results = chartMeterDailyViewRepository.findTotalVolumeByFarmForDateRangeIgnoringIncludeWebsite(
                    companyId, fromMTD, reportDate);

            List<Farm> farms = getCompanyFarms(companyId);

            double totalVolume = 0.0;
            Map<String, Double> farmVolumes = new HashMap<>();

            for (Object[] result : results) {
                String farmId = (String) result[0];
                Double volume = ((Number) result[1]).doubleValue();
                farmVolumes.put(farmId, volume);
                totalVolume += volume;
            }

            List<DailyReportDTO.FarmProduction> mtdProduction = new ArrayList<>();
            for (Farm farm : farms) {
                Double volume = farmVolumes.getOrDefault(farm.getId(), 0.0);
                double percentOfTotal = totalVolume > 0 ? (volume / totalVolume) * 100 : 0.0;

                mtdProduction.add(DailyReportDTO.FarmProduction.builder()
                        .farmName(farm.getName())
                        .volume(volume)
                        .percentOfTotal(percentOfTotal)
                        .build());
            }

            return mtdProduction;
        } catch (Exception e) {
            log.error("Error fetching MTD production summary", e);
            return Collections.emptyList();
        }
    }

    /**
     * Fetch farm performance (daily, MTD, YTD volumes per farm)
     */
    private List<DailyReportDTO.FarmPerformance> fetchFarmPerformance(String companyId, LocalDate reportDate) {
        log.info("Fetching farm performance for company: {} up to date: {}", companyId, reportDate);

        try {
            LocalDate fromMTD = reportDate.withDayOfMonth(1);
            LocalDate fromYTD = LocalDate.of(reportDate.getYear(), 1, 1);

            // Get daily volumes
            List<Object[]> dailyResults = chartMeterDailyViewRepository.findTotalVolumeByFarmForDateIgnoringIncludeWebsite(
                    companyId, reportDate);

            // Get MTD volumes
            List<Object[]> mtdResults = chartMeterDailyViewRepository.findTotalVolumeByFarmForDateRangeIgnoringIncludeWebsite(
                    companyId, fromMTD, reportDate);

            // Get YTD volumes
            List<Object[]> ytdResults = chartMeterDailyViewRepository.findTotalVolumeByFarmForDateRangeIgnoringIncludeWebsite(
                    companyId, fromYTD, reportDate);

            List<Farm> farms = getCompanyFarms(companyId);

            Map<String, Double> dailyVolumes = new HashMap<>();
            Map<String, Double> mtdVolumes = new HashMap<>();
            Map<String, Double> ytdVolumes = new HashMap<>();

            for (Object[] result : dailyResults) {
                String farmId = (String) result[0];
                Double volume = ((Number) result[1]).doubleValue();
                dailyVolumes.put(farmId, volume);
            }

            for (Object[] result : mtdResults) {
                String farmId = (String) result[0];
                Double volume = ((Number) result[1]).doubleValue();
                mtdVolumes.put(farmId, volume);
            }

            for (Object[] result : ytdResults) {
                String farmId = (String) result[0];
                Double volume = ((Number) result[1]).doubleValue();
                ytdVolumes.put(farmId, volume);
            }

            List<DailyReportDTO.FarmPerformance> farmPerformance = new ArrayList<>();
            for (Farm farm : farms) {
                double dailyVolume = dailyVolumes.getOrDefault(farm.getId(), 0.0);
                double mtdVolume = mtdVolumes.getOrDefault(farm.getId(), 0.0);
                double ytdVolume = ytdVolumes.getOrDefault(farm.getId(), 0.0);

                farmPerformance.add(DailyReportDTO.FarmPerformance.builder()
                        .farmName(farm.getName())
                        .dailyVolume(dailyVolume)
                        .mtdVolume(mtdVolume)
                        .ytdVolume(ytdVolume)
                        .build());
            }

            return farmPerformance;
        } catch (Exception e) {
            log.error("Error fetching farm performance", e);
            return Collections.emptyList();
        }
    }

    /**
     * Get all farms for a company
     */
    private List<Farm> getCompanyFarms(String companyId) {
        try {
            List<CompanyFarm> companyFarms = companyFarmRepository.findByCompanyId(companyId);
            List<Farm> farms = new ArrayList<>();
            for (CompanyFarm companyFarm : companyFarms) {
                farms.add(companyFarm.getFarm());
            }
            return farms;
        } catch (Exception e) {
            log.error("Error getting company farms", e);
            return Collections.emptyList();
        }
    }
}

