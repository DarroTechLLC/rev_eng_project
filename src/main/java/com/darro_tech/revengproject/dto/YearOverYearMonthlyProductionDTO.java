package com.darro_tech.revengproject.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Year-over-year Monthly Production section
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearOverYearMonthlyProductionDTO {

    private List<Integer> years;
    private List<YearlyMonthlyData> yearData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearlyMonthlyData {

        private Integer year;
        private List<MonthlyProduction> monthlyData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyProduction {

        private Integer month;
        private Double production;
    }
}


