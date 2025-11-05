package com.darro_tech.revengproject.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Comprehensive DTO for weekly report data containing all sections
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportDataDTO {

    private String companyId;
    private String companyName;
    private LocalDate reportDate;
    private LocalDate reportEndDate;
    private Integer reportEndYear;
    private Integer reportEndMonth;

    // Production Summary Section
    private ProductionSummaryDTO productionSummary;

    // Production Budget Comparison Section
    private ProductionBudgetComparisonDTO productionBudgetComparison;

    // Production Plan Section
    private List<ProductionPlanDTO> productionPlans;

    // YTD/MTD Pie Charts Section
    private YtdMtdPieDataDTO ytdMtdPieData;

    // Daily Volume Section
    private DailyVolumeDataDTO dailyVolumeData;

    // Weekly Volume vs Budget Section
    private WeeklyVolumeVsBudgetDTO weeklyVolumeVsBudget;

    // Multi-year Production vs Budget Section
    private MultiYearProductionVsBudgetDTO multiYearProductionVsBudget;

    // Year-over-year Monthly Production Section
    private YearOverYearMonthlyProductionDTO yearOverYearMonthlyProduction;

    // Current Year Production Forecast vs Budget Section
    private CurYearProductionForecastVsBudgetDTO curYearProductionForecastVsBudget;

    // Farm Biogas Inventory Section (Lagoon Levels)
    private LagoonLevelsDataDTO lagoonLevelsData;

    // Animal Inventory Population Section
    private PopulationDataDTO populationData;

    // Current Year Population Forecast vs Budget Section
    private CurYearPopulationForecastVsBudgetDTO curYearPopulationForecastVsBudget;

    // Gas production per Inventory Section
    private GasProductionPerInventoryDTO gasProductionPerInventory;

    // Historic 52-Week Section
    private Historic52WeekDTO historic52Week;

    // Error tracking
    private String errorMessage;
    private String errorSection;
}


