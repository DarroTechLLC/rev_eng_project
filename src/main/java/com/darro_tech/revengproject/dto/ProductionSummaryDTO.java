package com.darro_tech.revengproject.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Production Summary section (WTD/MTD/YTD tables + monthly breakdown)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionSummaryDTO {

    // WTD/MTD/YTD Summary
    private List<FarmVolumeSum> wtd;
    private List<FarmVolumeSum> mtd;
    private List<FarmVolumeSum> ytd;

    private LocalDate fromWTD;
    private LocalDate fromMTD;
    private LocalDate fromYTD;
    private LocalDate reportEndDate;

    // Monthly Breakdown
    private List<FarmWithMonths> farmMonths;
    private List<MonthlySummarizedValues> totals;
    private List<MonthlySummarizedValues> cumulative;
    private List<Integer> moNumbers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FarmVolumeSum {

        private String farmId;
        private String farmName;
        private Double volume;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FarmWithMonths {

        private String farmId;
        private String farmName;
        private List<MonthlyValue> months;
        private MonthlyValue totals;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyValue {

        private Integer mo;
        private Double volume;
        private Double budget;
        private Double forecast;
        private Double pctBudget;
        private Double pctForecast;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySummarizedValues {

        private Integer mo;
        private Double volume;
        private Double budget;
        private Double forecast;
        private Double pctBudget;
        private Double pctForecast;
    }
}


