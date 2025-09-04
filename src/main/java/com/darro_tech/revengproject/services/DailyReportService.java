package com.darro_tech.revengproject.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darro_tech.revengproject.dto.DailyReportDTO;
import com.darro_tech.revengproject.models.DailyReportCompany;
import com.darro_tech.revengproject.repositories.DailyReportCompanyRepository;

@Service
public class DailyReportService {

    private static final Logger logger = LoggerFactory.getLogger(DailyReportService.class);

    @Autowired
    private DailyReportCompanyRepository dailyReportRepository;

    /**
     * Get the latest timestamp for a daily report
     * 
     * @param companyId The ID of the company
     * @param date The date to find the report for
     * @return The timestamp of the latest report, or null if no report is found
     */
    public Date getLatestTimestamp(String companyId, LocalDate date) {
        logger.info("🕒 Getting latest timestamp for company {} on date {}", companyId, date);

        try {
            // Convert LocalDate to Instant for the start of the day
            Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant startOfNextDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            // Find reports for the given date range
            List<DailyReportCompany> reports = dailyReportRepository.findByCompanyIdAndDateBetween(
                    companyId, startOfDay, startOfNextDay);

            if (reports.isEmpty()) {
                logger.info("❌ No reports found for company {} on date {}", companyId, date);
                return null;
            }

            // Get the first report's timestamp
            DailyReportCompany report = reports.get(0);
            Instant timestamp = report.getTimestamp();

            logger.info("✅ Successfully retrieved timestamp for company {} on date {}: {}", companyId, date, timestamp);
            return Date.from(timestamp);
        } catch (Exception e) {
            logger.error("❌ Error getting timestamp", e);
            return null;
        }
    }

    /**
     * Get a daily report DTO
     * 
     * @param companyId The ID of the company
     * @param reportDate The date for the report
     * @return A DailyReportDTO object
     */
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
                .dailyTotal(1250.0)
                .mtdTotal(28750.0)
                .ytdTotal(345000.0)
                .build();
    }

    private List<DailyReportDTO.FarmProduction> generateSampleDailyProduction() {
        List<DailyReportDTO.FarmProduction> production = new ArrayList<>();

        DailyReportDTO.FarmProduction farm1 = new DailyReportDTO.FarmProduction();
        farm1.setFarmName("Farm A");
        farm1.setVolume(500.0);
        farm1.setPercentOfTotal(40.0);
        production.add(farm1);

        DailyReportDTO.FarmProduction farm2 = new DailyReportDTO.FarmProduction();
        farm2.setFarmName("Farm B");
        farm2.setVolume(750.0);
        farm2.setPercentOfTotal(60.0);
        production.add(farm2);

        return production;
    }

    private List<DailyReportDTO.FarmProduction> generateSampleMtdProduction() {
        List<DailyReportDTO.FarmProduction> production = new ArrayList<>();

        DailyReportDTO.FarmProduction farm1 = new DailyReportDTO.FarmProduction();
        farm1.setFarmName("Farm A");
        farm1.setVolume(11500.0);
        farm1.setPercentOfTotal(40.0);
        production.add(farm1);

        DailyReportDTO.FarmProduction farm2 = new DailyReportDTO.FarmProduction();
        farm2.setFarmName("Farm B");
        farm2.setVolume(17250.0);
        farm2.setPercentOfTotal(60.0);
        production.add(farm2);

        return production;
    }

    private List<DailyReportDTO.FarmPerformance> generateSampleFarmPerformance() {
        List<DailyReportDTO.FarmPerformance> performance = new ArrayList<>();

        DailyReportDTO.FarmPerformance farm1 = new DailyReportDTO.FarmPerformance();
        farm1.setFarmName("Farm A");
        farm1.setDailyVolume(500.0);
        farm1.setMtdVolume(11500.0);
        farm1.setYtdVolume(138000.0);
        performance.add(farm1);

        DailyReportDTO.FarmPerformance farm2 = new DailyReportDTO.FarmPerformance();
        farm2.setFarmName("Farm B");
        farm2.setDailyVolume(750.0);
        farm2.setMtdVolume(17250.0);
        farm2.setYtdVolume(207000.0);
        performance.add(farm2);

        return performance;
    }

    /**
     * Get the PDF data for a daily report
     * 
     * @param companyId The ID of the company
     * @param date The date to find the report for
     * @return The PDF data as a byte array, or an empty array if no report is found
     */
    public byte[] getPdfData(String companyId, LocalDate date) {
        logger.info("📊 Getting PDF data for company {} on date {}", companyId, date);

        try {
            // Convert LocalDate to Instant for the start of the day
            Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant startOfNextDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            // Find reports for the given date range
            List<DailyReportCompany> reports = dailyReportRepository.findByCompanyIdAndDateBetween(
                    companyId, startOfDay, startOfNextDay);

            if (reports.isEmpty()) {
                logger.info("❌ No reports found for company {} on date {}", companyId, date);
                return new byte[0];
            }

            // Get the first report's PDF data
            DailyReportCompany report = reports.get(0);
            byte[] pdfData = report.getPdf();

            if (pdfData == null || pdfData.length == 0) {
                logger.warn("❌ PDF data is null or empty for report ID: {}", report.getId());
                return new byte[0];
            }

            logger.info("✅ Successfully retrieved PDF data for company {} on date {}", companyId, date);
            return pdfData;
        } catch (Exception e) {
            logger.error("❌ Error getting PDF data", e);
            return new byte[0];
        }
    }
}
