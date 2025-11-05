package com.darro_tech.revengproject.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Farm Biogas Inventory (Lagoon Levels) section
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LagoonLevelsDataDTO {

    // Latest Lagoon Levels
    private List<LagoonLevelData> latestLevels;

    // Year-over-year Lagoon Levels
    private List<YearlyLagoonData> yearOverYearData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LagoonLevelData {

        private String farmId;
        private String farmName;
        private Double value;
        private LocalDate timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearlyLagoonData {

        private Integer year;
        private List<LagoonLevelData> levels;
    }
}


