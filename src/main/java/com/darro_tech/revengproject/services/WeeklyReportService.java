package com.darro_tech.revengproject.services;

import com.darro_tech.revengproject.dto.WeeklyReportDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeeklyReportService {

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