package com.darro_tech.revengproject.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Weekly Volume vs Budget section
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyVolumeVsBudgetDTO {

    private LocalDate fromDate;
    private LocalDate toDate;
    private List<WeeklyVolumeVsBudgetPoint> dataPoints;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyVolumeVsBudgetPoint {

        private LocalDate weekStartDate;
        private Double volume;
        private Double budget;
        private Double pctBudget;
    }
}


