package com.darro_tech.revengproject.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Production Budget Comparison section (charts + radial)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionBudgetComparisonDTO {

    private Double totalVolume;
    private List<FarmProductionBudget> farmProductions;
    
    // Radial chart data
    private Double annualBudget;
    private Double annualForecast;
    private Double ytdProduction;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FarmProductionBudget {

        private String farmName;
        private Double production;
        private Double pctBudget;
    }
}


