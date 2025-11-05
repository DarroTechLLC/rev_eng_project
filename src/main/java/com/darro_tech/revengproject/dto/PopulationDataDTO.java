package com.darro_tech.revengproject.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Animal Inventory Population section
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopulationDataDTO {

    // Multi-year Population Comparison
    private List<YearlyPopulationData> multiYearData;

    // Year-over-year Population
    private List<FarmYearlyPopulation> yearOverYearData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearlyPopulationData {

        private Integer year;
        private Double totalPopulation;
        private List<FarmPopulation> farmData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FarmPopulation {

        private String farmId;
        private String farmName;
        private Double population;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FarmYearlyPopulation {

        private String farmId;
        private String farmName;
        private List<PopulationPoint> populationPoints;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopulationPoint {

        private LocalDate date;
        private Double value;
    }
}


