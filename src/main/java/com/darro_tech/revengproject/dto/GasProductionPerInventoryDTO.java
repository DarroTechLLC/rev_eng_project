package com.darro_tech.revengproject.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Gas production per Inventory section
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GasProductionPerInventoryDTO {

    // Latest 52-week data
    private List<GasProductionPerInventoryPoint> latest52Week;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GasProductionPerInventoryPoint {

        private LocalDate date;
        private Double gasProduction;
        private Double inventoryPopulation;
        private Double ratio;
    }
}


