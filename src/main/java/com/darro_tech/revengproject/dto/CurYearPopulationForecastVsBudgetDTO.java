package com.darro_tech.revengproject.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Current Year Population Forecast vs Budget section
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurYearPopulationForecastVsBudgetDTO {

    private Integer year;
    private List<MonthlyPopulationForecastBudget> monthlyData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyPopulationForecastBudget {

        private Integer month;
        private Double forecast;
        private Double budget;
        private Double actual;
        private Double pctForecast;
        private Double pctBudget;
    }
}


