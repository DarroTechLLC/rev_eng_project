package com.darro_tech.revengproject.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Daily Volume section
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyVolumeDataDTO {

    private LocalDate fromDate;
    private LocalDate toDate;
    private List<DailyVolumePoint> dataPoints;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyVolumePoint {

        private LocalDate date;
        private Double volume;
        private String farmId;
        private String farmName;
    }
}


