package com.darro_tech.revengproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Production Plan section
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionPlanDTO {

    private String farmId;
    private String farmName;
    private Double estProduction; // MMBTUs
    private String farmStatus;
}


