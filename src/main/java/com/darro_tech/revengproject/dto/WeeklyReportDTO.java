package com.darro_tech.revengproject.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportDTO {

    private String companyId;
    private LocalDate reportDate;
    private String companyName;
    private List<ProductionSummaryDTO> productionSummaries;
    private BudgetComparisonDTO budgetComparison;
    private List<ProductionDetailDTO> productionDetails;
    private double wtdTotal;
    private double mtdTotal;
    private double ytdTotal;
    private List<FarmProduction> farmProductions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionSummaryDTO {

        private String period;
        private double actualProduction;
        private double targetProduction;
        private double percentOfTarget;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetComparisonDTO {

        private double actualBudget;
        private double plannedBudget;
        private double variance;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionDetailDTO {

        private String productName;
        private double quantity;
        private String unit;
        private double value;
    }

    @Data
    public static class FarmProduction {

        private String farmName;
        private double production;
    }
}
