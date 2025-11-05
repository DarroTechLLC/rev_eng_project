package com.darro_tech.revengproject.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Historic 52-Week Gas Production per 52-Week Inventory Population
 * section
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Historic52WeekDTO {

    private List<Categorized52WeekData> categorizedData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Categorized52WeekData {

        private String category;
        private List<Week52Point> dataPoints;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Week52Point {

        private String period;
        private Double gasProduction;
        private Double inventoryPopulation;
        private Double ratio;
    }
}


