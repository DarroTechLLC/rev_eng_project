package com.darro_tech.revengproject.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.darro_tech.revengproject.dto.DailyReportDTO;

@Service
public class DailyReportService {

    private static final Logger logger = LoggerFactory.getLogger(DailyReportService.class);

    public DailyReportDTO getDailyReport(String companyId, LocalDate reportDate) {
        logger.info("📊 Generating daily report for company {} on date {}", companyId, reportDate);

        // TODO: Implement actual data fetching from database
        return DailyReportDTO.builder()
                .companyId(companyId)
                .reportDate(reportDate)
                .companyName("Sample Company")
                .dailyProduction(generateSampleDailyProduction())
                .mtdProduction(generateSampleMtdProduction())
                .farmPerformance(generateSampleFarmPerformance())
                .build();
    }

    private List<DailyReportDTO.FarmProduction> generateSampleDailyProduction() {
        List<DailyReportDTO.FarmProduction> production = new ArrayList<>();
        production.add(DailyReportDTO.FarmProduction.builder()
                .farmName("Farm A")
                .volume(150.5)
                .percentOfTotal(45.2)
                .build());
        production.add(DailyReportDTO.FarmProduction.builder()
                .farmName("Farm B")
                .volume(120.3)
                .percentOfTotal(36.1)
                .build());
        production.add(DailyReportDTO.FarmProduction.builder()
                .farmName("Farm C")
                .volume(62.8)
                .percentOfTotal(18.7)
                .build());
        return production;
    }

    private List<DailyReportDTO.FarmProduction> generateSampleMtdProduction() {
        List<DailyReportDTO.FarmProduction> production = new ArrayList<>();
        production.add(DailyReportDTO.FarmProduction.builder()
                .farmName("Farm A")
                .volume(3200.5)
                .percentOfTotal(42.3)
                .build());
        production.add(DailyReportDTO.FarmProduction.builder()
                .farmName("Farm B")
                .volume(2800.2)
                .percentOfTotal(37.0)
                .build());
        production.add(DailyReportDTO.FarmProduction.builder()
                .farmName("Farm C")
                .volume(1560.8)
                .percentOfTotal(20.7)
                .build());
        return production;
    }

    private List<DailyReportDTO.FarmPerformance> generateSampleFarmPerformance() {
        List<DailyReportDTO.FarmPerformance> performance = new ArrayList<>();
        performance.add(DailyReportDTO.FarmPerformance.builder()
                .farmName("Farm A")
                .dailyVolume(150.5)
                .mtdVolume(3200.5)
                .ytdVolume(45000.0)
                .build());
        performance.add(DailyReportDTO.FarmPerformance.builder()
                .farmName("Farm B")
                .dailyVolume(120.3)
                .mtdVolume(2800.2)
                .ytdVolume(38000.0)
                .build());
        performance.add(DailyReportDTO.FarmPerformance.builder()
                .farmName("Farm C")
                .dailyVolume(62.8)
                .mtdVolume(1560.8)
                .ytdVolume(22000.0)
                .build());
        return performance;
    }
}
