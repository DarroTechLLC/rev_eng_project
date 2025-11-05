package com.darro_tech.revengproject.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Multi-year Production vs Budget section
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiYearProductionVsBudgetDTO {

    private List<Integer> years;
    private List<YearProductionBudget> yearData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearProductionBudget {

        private Integer year;
        private Double production;
        private Double budget;
        private Double pctBudget;
    }
}


