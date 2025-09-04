package com.darro_tech.revengproject.services;

import com.darro_tech.revengproject.dto.WeeklyReportDTO;
import com.darro_tech.revengproject.models.WeeklyReportCompany;
import com.darro_tech.revengproject.repositories.WeeklyReportCompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeeklyReportService {

    @Autowired
    private WeeklyReportCompanyRepository weeklyReportRepository;

    /**
     * Get the latest timestamp for a weekly report
     * 
     * @param companyId The ID of the company
     * @param date The date to find the report for
     * @return The timestamp of the latest report, or null if no report is found
     */
    public Date getLatestTimestamp(String companyId, LocalDate date) {
        log.info("🕒 Getting latest timestamp for company {} on date {}", companyId, date);

        try {
            // Convert LocalDate to Instant for the start of the day
            Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant startOfNextDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            // Find reports for the given date range
            List<WeeklyReportCompany> reports = weeklyReportRepository.findByCompanyIdAndDateBetween(
                    companyId, startOfDay, startOfNextDay);

            if (reports.isEmpty()) {
                log.info("❌ No reports found for company {} on date {}", companyId, date);
                return null;
            }

            // Get the first report's timestamp
            WeeklyReportCompany report = reports.get(0);
            Instant timestamp = report.getTimestamp();

            log.info("✅ Successfully retrieved timestamp for company {} on date {}: {}", companyId, date, timestamp);
            return Date.from(timestamp);
        } catch (Exception e) {
            log.error("❌ Error getting timestamp", e);
            return null;
        }
    }

    /**
     * Get the PDF data for a weekly report
     * 
     * @param companyId The ID of the company
     * @param date The date to find the report for
     * @return The PDF data as a byte array, or an empty array if no report is found
     */
    public byte[] getPdfData(String companyId, LocalDate date) {
        log.info("📊 Getting PDF data for company {} on date {}", companyId, date);

        try {
            // Convert LocalDate to Instant for the start of the day
            Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant startOfNextDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            // Find reports for the given date range
            List<WeeklyReportCompany> reports = weeklyReportRepository.findByCompanyIdAndDateBetween(
                    companyId, startOfDay, startOfNextDay);

            if (reports.isEmpty()) {
                log.info("❌ No reports found for company {} on date {}", companyId, date);
                return new byte[0];
            }

            // Get the first report's PDF data
            WeeklyReportCompany report = reports.get(0);
            byte[] pdfData = report.getPdf();

            if (pdfData == null || pdfData.length == 0) {
                log.warn("❌ PDF data is null or empty for report ID: {}", report.getId());
                return new byte[0];
            }

            log.info("✅ Successfully retrieved PDF data for company {} on date {}", companyId, date);
            return pdfData;
        } catch (Exception e) {
            log.error("❌ Error getting PDF data", e);
            return new byte[0];
        }
    }

    public WeeklyReportDTO getWeeklyReport(String companyId, LocalDate reportDate) {
        log.info("📊 Generating weekly report for company {} on date {}", companyId, reportDate);

        // TODO: Implement actual data fetching from database
        return WeeklyReportDTO.builder()
                .companyId(companyId)
                .reportDate(reportDate)
                .companyName("Sample Company")
                .productionSummaries(generateSampleProductionSummaries())
                .budgetComparison(generateSampleBudgetComparison())
                .productionDetails(generateSampleProductionDetails())
                .build();
    }

    private List<WeeklyReportDTO.ProductionSummaryDTO> generateSampleProductionSummaries() {
        List<WeeklyReportDTO.ProductionSummaryDTO> summaries = new ArrayList<>();

        // WTD Summary
        summaries.add(WeeklyReportDTO.ProductionSummaryDTO.builder()
                .period("WTD")
                .actualProduction(85.5)
                .targetProduction(100.0)
                .percentOfTarget(85.5)
                .status("Good")
                .build());

        // MTD Summary
        summaries.add(WeeklyReportDTO.ProductionSummaryDTO.builder()
                .period("MTD")
                .actualProduction(340.0)
                .targetProduction(400.0)
                .percentOfTarget(85.0)
                .status("Good")
                .build());

        // YTD Summary
        summaries.add(WeeklyReportDTO.ProductionSummaryDTO.builder()
                .period("YTD")
                .actualProduction(4250.0)
                .targetProduction(5000.0)
                .percentOfTarget(85.0)
                .status("Good")
                .build());

        return summaries;
    }

    private WeeklyReportDTO.BudgetComparisonDTO generateSampleBudgetComparison() {
        return WeeklyReportDTO.BudgetComparisonDTO.builder()
                .actualBudget(95000.0)
                .plannedBudget(100000.0)
                .variance(-5000.0)
                .status("Good")
                .build();
    }

    private List<WeeklyReportDTO.ProductionDetailDTO> generateSampleProductionDetails() {
        List<WeeklyReportDTO.ProductionDetailDTO> details = new ArrayList<>();

        details.add(WeeklyReportDTO.ProductionDetailDTO.builder()
                .productName("Product A")
                .quantity(150.0)
                .unit("units")
                .value(45000.0)
                .build());

        details.add(WeeklyReportDTO.ProductionDetailDTO.builder()
                .productName("Product B")
                .quantity(200.0)
                .unit("units")
                .value(50000.0)
                .build());

        return details;
    }
} 
