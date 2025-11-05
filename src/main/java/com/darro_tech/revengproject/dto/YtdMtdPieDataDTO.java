package com.darro_tech.revengproject.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for YTD/MTD Pie Charts section
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YtdMtdPieDataDTO {

    private List<YtdPieData> ytdPieData;
    private List<MtdPieData> mtdPieData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YtdPieData {

        private String farmName;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MtdPieData {

        private String farmName;
        private Double total;
    }
}


