package com.darro_tech.revengproject.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.darro_tech.revengproject.dto.CurYearPopulationForecastVsBudgetDTO;
import com.darro_tech.revengproject.dto.CurYearProductionForecastVsBudgetDTO;
import com.darro_tech.revengproject.dto.DailyVolumeDataDTO;
import com.darro_tech.revengproject.dto.FarmVolumeData;
import com.darro_tech.revengproject.dto.GasProductionPerInventoryDTO;
import com.darro_tech.revengproject.dto.Historic52WeekDTO;
import com.darro_tech.revengproject.dto.LagoonLevelsDataDTO;
import com.darro_tech.revengproject.dto.MultiYearProductionVsBudgetDTO;
import com.darro_tech.revengproject.dto.PopulationDataDTO;
import com.darro_tech.revengproject.dto.ProductionBudgetComparisonDTO;
import com.darro_tech.revengproject.dto.ProductionPlanDTO;
import com.darro_tech.revengproject.dto.ProductionSummaryDTO;
import com.darro_tech.revengproject.dto.WeeklyReportDataDTO;
import com.darro_tech.revengproject.dto.WeeklyVolumeVsBudgetDTO;
import com.darro_tech.revengproject.dto.YearOverYearMonthlyProductionDTO;
import com.darro_tech.revengproject.dto.YtdMtdPieDataDTO;
import com.darro_tech.revengproject.models.CompanyFarm;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.models.Ch4Recovery;
import com.darro_tech.revengproject.models.LagoonLevel;
import com.darro_tech.revengproject.models.Population;
import com.darro_tech.revengproject.models.PopulationBudget;
import com.darro_tech.revengproject.views.ChartMeterDailyView;
import com.darro_tech.revengproject.repositories.BudgetRepository;
import com.darro_tech.revengproject.repositories.ChartMeterDailyViewRepository;
import com.darro_tech.revengproject.repositories.CompanyFarmRepository;
import com.darro_tech.revengproject.repositories.FarmRepository;
import com.darro_tech.revengproject.repositories.LagoonLevelRepository;
import com.darro_tech.revengproject.repositories.Ch4RecoveryRepository;
import com.darro_tech.revengproject.repositories.PopulationBudgetRepository;
import com.darro_tech.revengproject.repositories.PopulationForecastRepository;
import com.darro_tech.revengproject.repositories.PopulationRepository;
import com.darro_tech.revengproject.utils.DateHelper;

/**
 * Service for fetching comprehensive weekly report data
 */
@Service
public class WeeklyReportDataService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WeeklyReportDataService.class);

    @org.springframework.beans.factory.annotation.Autowired
    private ChartMeterDailyViewRepository chartMeterDailyViewRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private BudgetRepository budgetRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private FarmRepository farmRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private CompanyFarmRepository companyFarmRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private LagoonLevelRepository lagoonLevelRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private Ch4RecoveryRepository ch4RecoveryRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private PopulationRepository populationRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private PopulationForecastRepository populationForecastRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private PopulationBudgetRepository populationBudgetRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private ChartService chartService;

    /**
     * Get comprehensive weekly report data
     */
    public WeeklyReportDataDTO getWeeklyReportData(String companyId, LocalDate reportDate, boolean useFakeData) {
        log.info("📊 Fetching weekly report data for company: {} on date: {}", companyId, reportDate);

        LocalDate reportEndDate = DateHelper.getStartOfWeek(reportDate, 1).plusDays(6);

        WeeklyReportDataDTO.WeeklyReportDataDTOBuilder builder = WeeklyReportDataDTO.builder()
                .companyId(companyId)
                .reportDate(reportDate)
                .reportEndDate(reportEndDate)
                .reportEndYear(reportEndDate.getYear())
                .reportEndMonth(reportEndDate.getMonthValue());

        try {
            // Get company name
            CompanyFarm companyFarm = companyFarmRepository.findByCompanyId(companyId).stream()
                    .findFirst()
                    .orElse(null);
            if (companyFarm != null) {
                builder.companyName(companyFarm.getCompany().getName());
            }

            if (useFakeData) {
                return buildFakeData(companyId, reportDate, reportEndDate, builder);
            }

            // Fetch all sections
            try {
                ProductionSummaryDTO productionSummary = fetchProductionSummary(companyId, reportEndDate);
                builder.productionSummary(productionSummary != null ? productionSummary : ProductionSummaryDTO.builder()
                        .wtd(Collections.emptyList())
                        .mtd(Collections.emptyList())
                        .ytd(Collections.emptyList())
                        .farmMonths(Collections.emptyList())
                        .totals(Collections.emptyList())
                        .cumulative(Collections.emptyList())
                        .moNumbers(Collections.emptyList())
                        .build());
            } catch (Exception e) {
                log.error("Error fetching production summary", e);
                builder.productionSummary(ProductionSummaryDTO.builder()
                        .wtd(Collections.emptyList())
                        .mtd(Collections.emptyList())
                        .ytd(Collections.emptyList())
                        .farmMonths(Collections.emptyList())
                        .totals(Collections.emptyList())
                        .cumulative(Collections.emptyList())
                        .moNumbers(Collections.emptyList())
                        .build());
                builder.errorMessage("ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage() + ". This indicates a code issue. Check logs.");
                builder.errorSection("Production Summary");
            }

            try {
                ProductionBudgetComparisonDTO productionBudgetComparison = fetchProductionBudgetComparison(companyId, reportEndDate);
                builder.productionBudgetComparison(productionBudgetComparison != null ? productionBudgetComparison : ProductionBudgetComparisonDTO.builder()
                        .farmProductions(Collections.emptyList())
                        .build());
            } catch (Exception e) {
                log.error("Error fetching production budget comparison", e);
                builder.productionBudgetComparison(ProductionBudgetComparisonDTO.builder()
                        .farmProductions(Collections.emptyList())
                        .build());
            }

            try {
                List<ProductionPlanDTO> productionPlans = fetchProductionPlans(companyId, reportEndDate);
                builder.productionPlans(productionPlans != null ? productionPlans : Collections.emptyList());
            } catch (Exception e) {
                log.error("Error fetching production plans", e);
                builder.productionPlans(Collections.emptyList());
            }

            try {
                YtdMtdPieDataDTO ytdMtdPieData = fetchYtdMtdPieData(companyId, reportEndDate);
                builder.ytdMtdPieData(ytdMtdPieData != null ? ytdMtdPieData : YtdMtdPieDataDTO.builder()
                        .ytdPieData(Collections.emptyList())
                        .mtdPieData(Collections.emptyList())
                        .build());
            } catch (Exception e) {
                log.error("Error fetching YTD/MTD pie data", e);
                builder.ytdMtdPieData(YtdMtdPieDataDTO.builder()
                        .ytdPieData(Collections.emptyList())
                        .mtdPieData(Collections.emptyList())
                        .build());
            }

            try {
                builder.dailyVolumeData(fetchDailyVolumeData(companyId, reportEndDate));
            } catch (Exception e) {
                log.error("Error fetching daily volume data", e);
            }

            try {
                builder.weeklyVolumeVsBudget(fetchWeeklyVolumeVsBudget(companyId, reportEndDate));
            } catch (Exception e) {
                log.error("Error fetching weekly volume vs budget", e);
            }

            try {
                builder.multiYearProductionVsBudget(fetchMultiYearProductionVsBudget(companyId, reportEndDate));
            } catch (Exception e) {
                log.error("Error fetching multi-year production vs budget", e);
            }

            try {
                builder.yearOverYearMonthlyProduction(fetchYearOverYearMonthlyProduction(companyId, reportEndDate));
            } catch (Exception e) {
                log.error("Error fetching year-over-year monthly production", e);
            }

            try {
                builder.curYearProductionForecastVsBudget(fetchCurYearProductionForecastVsBudget(companyId, reportEndDate));
            } catch (Exception e) {
                log.error("Error fetching current year production forecast vs budget", e);
            }

            try {
                builder.lagoonLevelsData(fetchLagoonLevelsData(companyId, reportEndDate));
            } catch (Exception e) {
                log.error("Error fetching lagoon levels data", e);
            }

            try {
                builder.populationData(fetchPopulationData(companyId, reportEndDate));
            } catch (Exception e) {
                log.error("Error fetching population data", e);
            }

            try {
                builder.curYearPopulationForecastVsBudget(fetchCurYearPopulationForecastVsBudget(companyId, reportEndDate));
            } catch (Exception e) {
                log.error("Error fetching current year population forecast vs budget", e);
            }

            try {
                builder.gasProductionPerInventory(fetchGasProductionPerInventory(companyId, reportEndDate));
            } catch (Exception e) {
                log.error("Error fetching gas production per inventory", e);
            }

            try {
                builder.historic52Week(fetchHistoric52Week(companyId, reportEndDate));
            } catch (Exception e) {
                log.error("Error fetching historic 52-week data", e);
            }

        } catch (Exception e) {
            log.error("Error building weekly report data", e);
            builder.errorMessage("ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage() + ". This indicates a code issue. Check logs.");
        }

        return builder.build();
    }

    // Production Summary Section
    private ProductionSummaryDTO fetchProductionSummary(String companyId, LocalDate reportEndDate) {
        log.info("Fetching production summary for company: {} up to date: {}", companyId, reportEndDate);

        LocalDate fromWTD = DateHelper.getStartOfWeek(reportEndDate, 1);
        LocalDate fromMTD = reportEndDate.withDayOfMonth(1);
        LocalDate fromYTD = LocalDate.of(reportEndDate.getYear(), 1, 1);

        List<Farm> farms = getCompanyFarms(companyId);
        Map<String, String> farmNames = farms.stream()
                .collect(Collectors.toMap(Farm::getId, Farm::getName));

        List<ProductionSummaryDTO.FarmVolumeSum> wtd = fetchVolumeSumForDateRange(companyId, fromWTD, reportEndDate, farmNames);
        List<ProductionSummaryDTO.FarmVolumeSum> mtd = fetchVolumeSumForDateRange(companyId, fromMTD, reportEndDate, farmNames);
        List<ProductionSummaryDTO.FarmVolumeSum> ytd = fetchVolumeSumForDateRange(companyId, fromYTD, reportEndDate, farmNames);

        // Monthly breakdown
        List<ProductionSummaryDTO.FarmWithMonths> farmMonths = fetchMonthlyProductionByFarm(companyId, reportEndDate, farmNames);
        List<ProductionSummaryDTO.MonthlySummarizedValues> totals = calculateMonthlyTotals(farmMonths);
        List<ProductionSummaryDTO.MonthlySummarizedValues> cumulative = calculateMonthlyCumulative(farmMonths);

        List<Integer> moNumbers = new ArrayList<>();
        for (int i = 1; i <= reportEndDate.getMonthValue(); i++) {
            moNumbers.add(i);
        }

        return ProductionSummaryDTO.builder()
                .wtd(wtd)
                .mtd(mtd)
                .ytd(ytd)
                .fromWTD(fromWTD)
                .fromMTD(fromMTD)
                .fromYTD(fromYTD)
                .reportEndDate(reportEndDate)
                .farmMonths(farmMonths)
                .totals(totals)
                .cumulative(cumulative)
                .moNumbers(moNumbers)
                .build();
    }

    private List<ProductionSummaryDTO.FarmVolumeSum> fetchVolumeSumForDateRange(
            String companyId, LocalDate fromDate, LocalDate toDate, Map<String, String> farmNames) {
        try {
            Instant fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant toInstant = toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

            List<Object[]> results = chartMeterDailyViewRepository.findTotalVolumeByFarmForDateRangeIgnoringIncludeWebsite(
                    companyId, fromDate, toDate);

            List<ProductionSummaryDTO.FarmVolumeSum> sums = new ArrayList<>();
            for (Object[] result : results) {
                String farmId = (String) result[0];
                Double volume = ((Number) result[1]).doubleValue();
                sums.add(ProductionSummaryDTO.FarmVolumeSum.builder()
                        .farmId(farmId)
                        .farmName(farmNames.getOrDefault(farmId, farmId))
                        .volume(volume)
                        .build());
            }
            return sums;
        } catch (Exception e) {
            log.error("Error fetching volume sum for date range", e);
            return Collections.emptyList();
        }
    }

    private List<ProductionSummaryDTO.FarmWithMonths> fetchMonthlyProductionByFarm(
            String companyId, LocalDate reportEndDate, Map<String, String> farmNames) {
        try {
            LocalDate yearStart = LocalDate.of(reportEndDate.getYear(), 1, 1);
            Instant fromInstant = yearStart.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant toInstant = reportEndDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

            List<Object[]> productionResults = chartMeterDailyViewRepository.findMonthlyProductionByFarmForCompanyDateRange(
                    companyId, yearStart, reportEndDate);
            List<Object[]> budgetResults = budgetRepository.findMonthlyBudgetByFarmForCompanyAndDateRange(
                    companyId, fromInstant, toInstant);

            Map<String, Map<Integer, Double>> productionMap = new HashMap<>();
            Map<String, Map<Integer, Double>> budgetMap = new HashMap<>();

            for (Object[] result : productionResults) {
                String farmId = (String) result[0];
                Integer month = (Integer) result[2];
                Double volume = ((Number) result[3]).doubleValue();
                productionMap.computeIfAbsent(farmId, k -> new HashMap<>()).put(month, volume);
            }

            for (Object[] result : budgetResults) {
                String farmId = (String) result[0];
                Integer month = (Integer) result[2];
                Double budget = ((Number) result[3]).doubleValue();
                budgetMap.computeIfAbsent(farmId, k -> new HashMap<>()).put(month, budget);
            }

            List<ProductionSummaryDTO.FarmWithMonths> farmMonths = new ArrayList<>();
            for (String farmId : farmNames.keySet()) {
                List<ProductionSummaryDTO.MonthlyValue> months = new ArrayList<>();
                double totalVolume = 0;
                double totalBudget = 0;

                for (int mo = 1; mo <= reportEndDate.getMonthValue(); mo++) {
                    Double volume = productionMap.getOrDefault(farmId, Collections.emptyMap()).get(mo);
                    Double budget = budgetMap.getOrDefault(farmId, Collections.emptyMap()).get(mo);
                    if (volume == null) {
                        volume = 0.0;
                    }
                    if (budget == null) {
                        budget = 0.0;
                    }

                    Double pctBudget = budget > 0 ? (volume / budget) * 100 : null;

                    months.add(ProductionSummaryDTO.MonthlyValue.builder()
                            .mo(mo)
                            .volume(volume)
                            .budget(budget > 0 ? budget : null)
                            .pctBudget(pctBudget)
                            .build());

                    totalVolume += volume;
                    totalBudget += budget;
                }

                Double totalPctBudget = totalBudget > 0 ? (totalVolume / totalBudget) * 100 : null;

                farmMonths.add(ProductionSummaryDTO.FarmWithMonths.builder()
                        .farmId(farmId)
                        .farmName(farmNames.get(farmId))
                        .months(months)
                        .totals(ProductionSummaryDTO.MonthlyValue.builder()
                                .volume(totalVolume)
                                .budget(totalBudget > 0 ? totalBudget : null)
                                .pctBudget(totalPctBudget)
                                .build())
                        .build());
            }

            return farmMonths;
        } catch (Exception e) {
            log.error("Error fetching monthly production by farm", e);
            return Collections.emptyList();
        }
    }

    private List<ProductionSummaryDTO.MonthlySummarizedValues> calculateMonthlyTotals(
            List<ProductionSummaryDTO.FarmWithMonths> farmMonths) {
        Map<Integer, ProductionSummaryDTO.MonthlySummarizedValues> totals = new HashMap<>();

        for (ProductionSummaryDTO.FarmWithMonths farm : farmMonths) {
            for (ProductionSummaryDTO.MonthlyValue month : farm.getMonths()) {
                totals.computeIfAbsent(month.getMo(), k -> ProductionSummaryDTO.MonthlySummarizedValues.builder()
                        .mo(k)
                        .volume(0.0)
                        .budget(0.0)
                        .build());

                ProductionSummaryDTO.MonthlySummarizedValues total = totals.get(month.getMo());
                total.setVolume(total.getVolume() + (month.getVolume() != null ? month.getVolume() : 0));
                if (month.getBudget() != null) {
                    total.setBudget(total.getBudget() + month.getBudget());
                }
            }
        }

        for (ProductionSummaryDTO.MonthlySummarizedValues total : totals.values()) {
            if (total.getBudget() > 0) {
                total.setPctBudget((total.getVolume() / total.getBudget()) * 100);
            }
        }

        return new ArrayList<>(totals.values());
    }

    private List<ProductionSummaryDTO.MonthlySummarizedValues> calculateMonthlyCumulative(
            List<ProductionSummaryDTO.FarmWithMonths> farmMonths) {
        List<ProductionSummaryDTO.MonthlySummarizedValues> cumulative = new ArrayList<>();
        double cumVolume = 0;
        double cumBudget = 0;

        for (int mo = 1; mo <= 12; mo++) {
            final int finalMo = mo;
            for (ProductionSummaryDTO.FarmWithMonths farm : farmMonths) {
                ProductionSummaryDTO.MonthlyValue monthData = farm.getMonths().stream()
                        .filter(m -> m.getMo() == finalMo)
                        .findFirst()
                        .orElse(null);
                if (monthData != null) {
                    cumVolume += monthData.getVolume() != null ? monthData.getVolume() : 0;
                    if (monthData.getBudget() != null) {
                        cumBudget += monthData.getBudget();
                    }
                }
            }

            Double pctBudget = cumBudget > 0 ? (cumVolume / cumBudget) * 100 : null;
            cumulative.add(ProductionSummaryDTO.MonthlySummarizedValues.builder()
                    .mo(mo)
                    .volume(cumVolume)
                    .budget(cumBudget > 0 ? cumBudget : null)
                    .pctBudget(pctBudget)
                    .build());
        }

        return cumulative;
    }

    // Production Budget Comparison Section
    private ProductionBudgetComparisonDTO fetchProductionBudgetComparison(String companyId, LocalDate reportEndDate) {
        log.info("Fetching production budget comparison for company: {} up to date: {}", companyId, reportEndDate);

        LocalDate yearStart = LocalDate.of(reportEndDate.getYear(), 1, 1);
        LocalDate yearEnd = LocalDate.of(reportEndDate.getYear(), 12, 31);
        Instant fromInstant = yearStart.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toInstant = reportEndDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        Instant yearEndInstant = yearEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        // Get YTD production data
        List<FarmVolumeData> productionData = chartService.getVolumeByFarmForDateRange(companyId, yearStart, reportEndDate);
        List<Object[]> budgetResults = budgetRepository.findTotalBudgetByFarmForCompanyAndDateRange(
                companyId, fromInstant, toInstant);

        // Get annual budget (full year)
        List<Object[]> annualBudgetResults = budgetRepository.findTotalBudgetByFarmForCompanyAndDateRange(
                companyId, fromInstant, yearEndInstant);

        Map<String, Double> budgetMap = new HashMap<>();
        for (Object[] result : budgetResults) {
            String farmId = (String) result[0];
            Double budget = ((Number) result[1]).doubleValue();
            budgetMap.put(farmId, budget);
        }

        // Calculate annual budget (sum of all farms for full year)
        double annualBudget = 0.0;
        for (Object[] result : annualBudgetResults) {
            Double budget = ((Number) result[1]).doubleValue();
            annualBudget += budget;
        }

        // Calculate annual forecast - use production forecast if available, otherwise calculate from YTD performance
        Double annualForecast = null;
        try {
            // Get production forecast from population forecast (if available)
            // For now, calculate forecast based on YTD performance extrapolated to full year
            double ytdProduction = 0.0;
            double ytdBudget = 0.0;
            for (FarmVolumeData farm : productionData) {
                ytdProduction += farm.getVolume();
            }
            for (Object[] result : budgetResults) {
                ytdBudget += ((Number) result[1]).doubleValue();
            }
            
            // Calculate forecast: if YTD production is X% of YTD budget, apply same % to annual budget
            if (ytdBudget > 0 && annualBudget > 0) {
                double ytdPct = ytdProduction / ytdBudget;
                annualForecast = annualBudget * ytdPct;
            }
        } catch (Exception e) {
            log.warn("Error calculating annual forecast, will be null", e);
        }

        double totalVolume = 0;
        List<ProductionBudgetComparisonDTO.FarmProductionBudget> farmProductions = new ArrayList<>();

        for (FarmVolumeData farm : productionData) {
            Double budget = budgetMap.get(farm.getFarm_id());
            Double pctBudget = budget != null && budget > 0 ? (farm.getVolume() / budget) * 100 : null;

            farmProductions.add(ProductionBudgetComparisonDTO.FarmProductionBudget.builder()
                    .farmName(farm.getFarmName())
                    .production(farm.getVolume())
                    .pctBudget(pctBudget)
                    .build());

            totalVolume += farm.getVolume();
        }

        return ProductionBudgetComparisonDTO.builder()
                .totalVolume(totalVolume)
                .farmProductions(farmProductions)
                .annualBudget(annualBudget > 0 ? annualBudget : null)
                .annualForecast(annualForecast)
                .ytdProduction(totalVolume)
                .build();
    }

    // Production Plan Section
    private List<ProductionPlanDTO> fetchProductionPlans(String companyId, LocalDate reportEndDate) {
        log.info("Fetching production plans for company: {} up to date: {}", companyId, reportEndDate);
        try {
            List<ProductionPlanDTO> plans = new ArrayList<>();

            // Get all farms for this company
            List<CompanyFarm> companyFarms = companyFarmRepository.findByCompanyId(companyId);
            LocalDate startDate = reportEndDate.minusDays(30); // Last 30 days for estimation

            for (CompanyFarm companyFarm : companyFarms) {
                Farm farm = companyFarm.getFarm();
                if (farm == null) continue;

                // Get recent production data using the available repository method
                List<Object[]> recentDataRaw = chartMeterDailyViewRepository
                    .findTotalVolumeByFarmForDateRangeIgnoringIncludeWebsite(
                        companyId, startDate, reportEndDate);

                // Find the data for this specific farm
                Double totalProduction = recentDataRaw.stream()
                    .filter(data -> farm.getId().equals(data[0]))
                    .mapToDouble(data -> ((Number) data[1]).doubleValue())
                    .findFirst()
                    .orElse(0.0);

                // Calculate estimated production based on recent average
                Double estProduction = null;
                if (totalProduction > 0) {
                    // Calculate daily average and estimate monthly production
                    double avgDailyProduction = totalProduction / 30; // 30 day period
                    estProduction = avgDailyProduction * 30;
                }

                // Determine farm status based on recent activity
                String farmStatus = "Active";
                if (totalProduction == 0) {
                    farmStatus = "No Recent Data";
                } else if (totalProduction < 100) { // Low production threshold
                    farmStatus = "Low Activity";
                }

                ProductionPlanDTO plan = new ProductionPlanDTO();
                plan.setFarmId(farm.getId());
                plan.setFarmName(farm.getName() != null ? farm.getName() : "Unknown Farm");
                plan.setEstProduction(estProduction);
                plan.setFarmStatus(farmStatus);

                plans.add(plan);
            }

            log.info("Generated production plans for {} farms", plans.size());
            return plans;

        } catch (Exception e) {
            log.error("Error fetching production plans for company: {}", companyId, e);
            return Collections.emptyList();
        }
    }

    // YTD/MTD Pie Data Section
    private YtdMtdPieDataDTO fetchYtdMtdPieData(String companyId, LocalDate reportEndDate) {
        log.info("Fetching YTD/MTD pie data for company: {} up to date: {}", companyId, reportEndDate);

        LocalDate yearStart = LocalDate.of(reportEndDate.getYear(), 1, 1);
        LocalDate monthStart = reportEndDate.withDayOfMonth(1);

        List<FarmVolumeData> ytdData = chartService.getVolumeByFarmForDateRange(companyId, yearStart, reportEndDate);
        List<FarmVolumeData> mtdData = chartService.getVolumeByFarmForDateRange(companyId, monthStart, reportEndDate);

        double ytdTotal = ytdData.stream().mapToDouble(FarmVolumeData::getVolume).sum();
        double mtdTotal = mtdData.stream().mapToDouble(FarmVolumeData::getVolume).sum();

        List<YtdMtdPieDataDTO.YtdPieData> ytdPie = ytdData.stream()
                .map(f -> YtdMtdPieDataDTO.YtdPieData.builder()
                .farmName(f.getFarmName())
                .percentage(ytdTotal > 0 ? (f.getVolume() / ytdTotal) * 100 : 0)
                .build())
                .collect(Collectors.toList());

        List<YtdMtdPieDataDTO.MtdPieData> mtdPie = mtdData.stream()
                .map(f -> YtdMtdPieDataDTO.MtdPieData.builder()
                .farmName(f.getFarmName())
                .total(f.getVolume())
                .build())
                .collect(Collectors.toList());

        return YtdMtdPieDataDTO.builder()
                .ytdPieData(ytdPie)
                .mtdPieData(mtdPie)
                .build();
    }

    // Daily Volume Data Section
    private DailyVolumeDataDTO fetchDailyVolumeData(String companyId, LocalDate reportEndDate) {
        log.info("Fetching daily volume data for company: {} up to date: {}", companyId, reportEndDate);

        LocalDate fromDate = reportEndDate.minusDays(30); // Last 30 days

        List<Object[]> results = chartMeterDailyViewRepository.findDailyProductionForCompanyDateRange(
                companyId, fromDate, reportEndDate);

        List<DailyVolumeDataDTO.DailyVolumePoint> dataPoints = new ArrayList<>();
        for (Object[] result : results) {
            LocalDate date = ((java.sql.Date) result[0]).toLocalDate();
            Double volume = ((Number) result[1]).doubleValue();
            dataPoints.add(DailyVolumeDataDTO.DailyVolumePoint.builder()
                    .date(date)
                    .volume(volume)
                    .build());
        }

        return DailyVolumeDataDTO.builder()
                .fromDate(fromDate)
                .toDate(reportEndDate)
                .dataPoints(dataPoints)
                .build();
    }

    // Weekly Volume vs Budget Section
    private WeeklyVolumeVsBudgetDTO fetchWeeklyVolumeVsBudget(String companyId, LocalDate reportEndDate) {
        log.info("Fetching weekly volume vs budget for company: {} up to date: {}", companyId, reportEndDate);
        try {
            LocalDate fromDate = reportEndDate.minusWeeks(12);
            List<WeeklyVolumeVsBudgetDTO.WeeklyVolumeVsBudgetPoint> dataPoints = new ArrayList<>();

            // Get daily production data for the 12-week period
            List<Object[]> dailyProductionData = chartMeterDailyViewRepository
                .findDailyProductionForCompanyDateRange(companyId, fromDate, reportEndDate);

            // Convert to a map for easier lookup: date -> total production
            Map<LocalDate, Double> dailyProductionMap = dailyProductionData.stream()
                .collect(Collectors.toMap(
                    data -> (LocalDate) data[0],
                    data -> ((Number) data[1]).doubleValue()
                ));

            // Get budget data for the same period (cover the entire 12-week span plus some buffer)
            LocalDate budgetStartDate = fromDate.minusMonths(1);
            Instant budgetStartInstant = budgetStartDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant budgetEndInstant = reportEndDate.plusMonths(1).atStartOfDay(ZoneOffset.UTC).toInstant();

            List<Object[]> monthlyBudgetDataRaw = budgetRepository.findMonthlyBudgetByFarmForCompanyAndDateRange(
                companyId, budgetStartInstant, budgetEndInstant);

            // Sum all farm budgets by month to get company-wide monthly budget
            Map<String, Double> monthlyBudgetMap = monthlyBudgetDataRaw.stream()
                .collect(Collectors.groupingBy(
                    data -> data[1] + "-" + data[2], // year-month as key
                    Collectors.summingDouble(data -> ((Number) data[3]).doubleValue())
                ));

            // Group data by weeks
            LocalDate currentDate = fromDate;
            while (!currentDate.isAfter(reportEndDate)) {
                LocalDate weekStart = currentDate;
                LocalDate weekEnd = weekStart.plusDays(6);

                // Calculate weekly production by summing daily production
                double weeklyProduction = 0.0;
                for (LocalDate date = weekStart; !date.isAfter(weekEnd) && !date.isAfter(reportEndDate); date = date.plusDays(1)) {
                    weeklyProduction += dailyProductionMap.getOrDefault(date, 0.0);
                }

                // Calculate weekly budget (proportional to the number of days in the month)
                double weeklyBudget = 0.0;
                int month = weekStart.getMonthValue();
                int year = weekStart.getYear();
                String monthKey = year + "-" + month;
                if (monthlyBudgetMap.containsKey(monthKey)) {
                    double monthlyBudget = monthlyBudgetMap.get(monthKey);
                    int daysInMonth = year % 4 == 0 && month == 2 ? 29 : // Simplified leap year
                        new int[]{31,28,31,30,31,30,31,31,30,31,30,31}[month - 1];

                    // Calculate how many days of this month are in this week
                    int daysInWeekForMonth = 0;
                    for (LocalDate date = weekStart; !date.isAfter(weekEnd) && !date.isAfter(reportEndDate); date = date.plusDays(1)) {
                        if (date.getMonthValue() == month && date.getYear() == year) {
                            daysInWeekForMonth++;
                        }
                    }

                    weeklyBudget = (monthlyBudget / daysInMonth) * daysInWeekForMonth;
                }

                // Calculate percentage of budget
                Double pctBudget = weeklyBudget > 0 ? (weeklyProduction / weeklyBudget) * 100 : null;

                WeeklyVolumeVsBudgetDTO.WeeklyVolumeVsBudgetPoint dataPoint = new WeeklyVolumeVsBudgetDTO.WeeklyVolumeVsBudgetPoint();
                dataPoint.setWeekStartDate(weekStart);
                dataPoint.setVolume(weeklyProduction > 0 ? weeklyProduction : null);
                dataPoint.setBudget(weeklyBudget > 0 ? weeklyBudget : null);
                dataPoint.setPctBudget(pctBudget);

                dataPoints.add(dataPoint);

                currentDate = weekEnd.plusDays(1);
            }

            WeeklyVolumeVsBudgetDTO result = new WeeklyVolumeVsBudgetDTO();
            result.setFromDate(fromDate);
            result.setToDate(reportEndDate);
            result.setDataPoints(dataPoints);

            return result;

        } catch (Exception e) {
            log.error("Error fetching weekly volume vs budget for company: {}", companyId, e);
            return WeeklyVolumeVsBudgetDTO.builder()
                    .fromDate(reportEndDate.minusWeeks(12))
                    .toDate(reportEndDate)
                    .dataPoints(Collections.emptyList())
                    .build();
        }
    }

    // Multi-year Production vs Budget Section
    private MultiYearProductionVsBudgetDTO fetchMultiYearProductionVsBudget(String companyId, LocalDate reportEndDate) {
        log.info("Fetching multi-year production vs budget for company: {} up to date: {}", companyId, reportEndDate);
        try {
            List<Integer> years = Arrays.asList(reportEndDate.getYear() - 2, reportEndDate.getYear() - 1, reportEndDate.getYear());
            List<MultiYearProductionVsBudgetDTO.YearProductionBudget> yearData = new ArrayList<>();

            for (Integer year : years) {
                LocalDate yearStart = LocalDate.of(year, 1, 1);
                LocalDate yearEnd = LocalDate.of(year, 12, 31);

                // Get total production for the year using daily production data
                List<Object[]> yearlyProductionData = chartMeterDailyViewRepository
                    .findDailyProductionForCompanyDateRange(companyId, yearStart, yearEnd);

                double totalProduction = yearlyProductionData.stream()
                    .mapToDouble(data -> ((Number) data[1]).doubleValue())
                    .sum();

                // Get total budget for the year
                Instant yearStartInstant = yearStart.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant yearEndInstant = yearEnd.atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();

                List<Object[]> yearlyBudgetDataRaw = budgetRepository.findMonthlyBudgetByFarmForCompanyAndDateRange(
                    companyId, yearStartInstant, yearEndInstant);

                double totalBudget = yearlyBudgetDataRaw.stream()
                    .mapToDouble(data -> ((Number) data[3]).doubleValue())
                    .sum();

                // Calculate percentage of budget
                Double pctBudget = totalBudget > 0 ? (totalProduction / totalBudget) * 100 : null;

                MultiYearProductionVsBudgetDTO.YearProductionBudget yearProductionBudget =
                    new MultiYearProductionVsBudgetDTO.YearProductionBudget();
                yearProductionBudget.setYear(year);
                yearProductionBudget.setProduction(totalProduction > 0 ? totalProduction : null);
                yearProductionBudget.setBudget(totalBudget > 0 ? totalBudget : null);
                yearProductionBudget.setPctBudget(pctBudget);

                yearData.add(yearProductionBudget);
            }

            MultiYearProductionVsBudgetDTO result = new MultiYearProductionVsBudgetDTO();
            result.setYears(years);
            result.setYearData(yearData);

            return result;

        } catch (Exception e) {
            log.error("Error fetching multi-year production vs budget for company: {}", companyId, e);
            return MultiYearProductionVsBudgetDTO.builder()
                    .years(Arrays.asList(reportEndDate.getYear() - 2, reportEndDate.getYear() - 1, reportEndDate.getYear()))
                    .yearData(Collections.emptyList())
                    .build();
        }
    }

    // Year-over-year Monthly Production Section
    private YearOverYearMonthlyProductionDTO fetchYearOverYearMonthlyProduction(String companyId, LocalDate reportEndDate) {
        log.info("Fetching year-over-year monthly production for company: {} up to date: {}", companyId, reportEndDate);
        try {
            List<Integer> years = Arrays.asList(reportEndDate.getYear() - 2, reportEndDate.getYear() - 1, reportEndDate.getYear());
            List<YearOverYearMonthlyProductionDTO.YearlyMonthlyData> yearData = new ArrayList<>();

            for (Integer year : years) {
                LocalDate yearStart = LocalDate.of(year, 1, 1);
                LocalDate yearEnd = LocalDate.of(year, 12, 31);

                // Get monthly production data for this year
                List<Object[]> monthlyProductionRaw = chartMeterDailyViewRepository
                    .findMonthlyProductionByFarmForCompanyDateRange(companyId, yearStart, yearEnd);

                // Aggregate by month across all farms
                Map<Integer, Double> monthlyProductionMap = new HashMap<>();
                for (int month = 1; month <= 12; month++) {
                    monthlyProductionMap.put(month, 0.0);
                }

                for (Object[] data : monthlyProductionRaw) {
                    Integer month = (Integer) data[2]; // month from query result
                    Double production = ((Number) data[3]).doubleValue(); // totalValue from query result
                    monthlyProductionMap.merge(month, production, Double::sum);
                }

                // Convert to monthly production list
                List<YearOverYearMonthlyProductionDTO.MonthlyProduction> monthlyData = new ArrayList<>();
                for (int month = 1; month <= 12; month++) {
                    Double production = monthlyProductionMap.get(month);
                    if (production != null && production > 0) {
                        YearOverYearMonthlyProductionDTO.MonthlyProduction monthlyProd =
                            new YearOverYearMonthlyProductionDTO.MonthlyProduction();
                        monthlyProd.setMonth(month);
                        monthlyProd.setProduction(production);
                        monthlyData.add(monthlyProd);
                    } else {
                        // Add entry with null production for months with no data
                        YearOverYearMonthlyProductionDTO.MonthlyProduction monthlyProd =
                            new YearOverYearMonthlyProductionDTO.MonthlyProduction();
                        monthlyProd.setMonth(month);
                        monthlyProd.setProduction(null);
                        monthlyData.add(monthlyProd);
                    }
                }

                YearOverYearMonthlyProductionDTO.YearlyMonthlyData yearlyDataPoint =
                    new YearOverYearMonthlyProductionDTO.YearlyMonthlyData();
                yearlyDataPoint.setYear(year);
                yearlyDataPoint.setMonthlyData(monthlyData);

                yearData.add(yearlyDataPoint);
            }

            YearOverYearMonthlyProductionDTO result = new YearOverYearMonthlyProductionDTO();
            result.setYears(years);
            result.setYearData(yearData);

            return result;

        } catch (Exception e) {
            log.error("Error fetching year-over-year monthly production for company: {}", companyId, e);
            return YearOverYearMonthlyProductionDTO.builder()
                    .years(Arrays.asList(reportEndDate.getYear() - 2, reportEndDate.getYear() - 1, reportEndDate.getYear()))
                    .yearData(Collections.emptyList())
                    .build();
        }
    }

    // Current Year Production Forecast vs Budget Section
    private CurYearProductionForecastVsBudgetDTO fetchCurYearProductionForecastVsBudget(String companyId, LocalDate reportEndDate) {
        log.info("Fetching current year production forecast vs budget for company: {} up to date: {}", companyId, reportEndDate);
        try {
            int currentYear = reportEndDate.getYear();
            List<CurYearProductionForecastVsBudgetDTO.MonthlyForecastBudget> monthlyData = new ArrayList<>();

            LocalDate yearStart = LocalDate.of(currentYear, 1, 1);
            LocalDate yearEnd = LocalDate.of(currentYear, 12, 31);

            // Get actual production data for the year up to report date
            List<Object[]> actualProductionData = chartMeterDailyViewRepository
                .findDailyProductionForCompanyDateRange(companyId, yearStart, reportEndDate);

            // Convert to monthly actuals
            Map<Integer, Double> monthlyActuals = actualProductionData.stream()
                .collect(Collectors.groupingBy(
                    data -> ((LocalDate) data[0]).getMonthValue(),
                    Collectors.summingDouble(data -> ((Number) data[1]).doubleValue())
                ));

            // Get budget data for the entire year
            Instant yearStartInstant = yearStart.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant yearEndInstant = yearEnd.atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();

            List<Object[]> yearlyBudgetDataRaw = budgetRepository.findMonthlyBudgetByFarmForCompanyAndDateRange(
                companyId, yearStartInstant, yearEndInstant);

            Map<Integer, Double> monthlyBudgets = yearlyBudgetDataRaw.stream()
                .collect(Collectors.groupingBy(
                    data -> (Integer) data[2], // month
                    Collectors.summingDouble(data -> ((Number) data[3]).doubleValue())
                ));

            // For each month, create forecast and budget comparison
            for (int month = 1; month <= 12; month++) {
                Double actual = monthlyActuals.get(month);
                Double budget = monthlyBudgets.get(month);

                // Simple forecast calculation: if we have actual data, use trend to forecast remaining months
                Double forecast = null;
                Double pctForecast = null;
                Double pctBudget = null;

                if (month <= reportEndDate.getMonthValue()) {
                    // This month is in the past or current - use actual as forecast
                    forecast = actual;
                    pctForecast = actual != null ? 100.0 : null;
                } else {
                    // Future month - forecast based on year-to-date performance
                    double yearToDateActual = monthlyActuals.values().stream()
                        .mapToDouble(Double::doubleValue).sum();
                    double yearToDateBudget = monthlyBudgets.entrySet().stream()
                        .filter(entry -> entry.getKey() <= reportEndDate.getMonthValue())
                        .mapToDouble(Map.Entry::getValue).sum();

                    if (yearToDateBudget > 0) {
                        double yearToDatePct = yearToDateActual / yearToDateBudget;
                        int remainingMonths = 12 - reportEndDate.getMonthValue();
                        if (remainingMonths > 0) {
                            // Calculate average monthly budget and apply year-to-date performance
                            double avgMonthlyBudget = monthlyBudgets.entrySet().stream()
                                .filter(entry -> entry.getKey() > reportEndDate.getMonthValue())
                                .mapToDouble(Map.Entry::getValue).sum() / remainingMonths;
                            forecast = avgMonthlyBudget * yearToDatePct;
                        }
                    }
                    pctForecast = forecast != null && budget != null ? (forecast / budget) * 100 : null;
                }

                pctBudget = actual != null && budget != null ? (actual / budget) * 100 : null;

                CurYearProductionForecastVsBudgetDTO.MonthlyForecastBudget monthlyDataPoint =
                    new CurYearProductionForecastVsBudgetDTO.MonthlyForecastBudget();
                monthlyDataPoint.setMonth(month);
                monthlyDataPoint.setForecast(forecast);
                monthlyDataPoint.setBudget(budget);
                monthlyDataPoint.setActual(actual);
                monthlyDataPoint.setPctForecast(pctForecast);
                monthlyDataPoint.setPctBudget(pctBudget);

                monthlyData.add(monthlyDataPoint);
            }

            CurYearProductionForecastVsBudgetDTO result = new CurYearProductionForecastVsBudgetDTO();
            result.setYear(currentYear);
            result.setMonthlyData(monthlyData);

            return result;

        } catch (Exception e) {
            log.error("Error fetching current year production forecast vs budget for company: {}", companyId, e);
            return CurYearProductionForecastVsBudgetDTO.builder()
                    .year(reportEndDate.getYear())
                    .monthlyData(Collections.emptyList())
                    .build();
        }
    }

    // Lagoon Levels Data Section
    private LagoonLevelsDataDTO fetchLagoonLevelsData(String companyId, LocalDate reportEndDate) {
        log.info("Fetching lagoon levels data for company: {} up to date: {}", companyId, reportEndDate);

        Instant maxDate = reportEndDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<LagoonLevel> latestLevels = lagoonLevelRepository.findLatestLagoonLevelsByCompanyId(companyId, maxDate);

        Map<String, String> farmNames = getCompanyFarms(companyId).stream()
                .collect(Collectors.toMap(Farm::getId, Farm::getName));

        List<LagoonLevelsDataDTO.LagoonLevelData> latestData = latestLevels.stream()
                .map(ll -> LagoonLevelsDataDTO.LagoonLevelData.builder()
                .farmId(ll.getFarm().getId())
                .farmName(farmNames.getOrDefault(ll.getFarm().getId(), ll.getFarm().getId()))
                .value(ll.getValue())
                .timestamp(ll.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate())
                .build())
                .collect(Collectors.toList());

        // Year-over-year data
        List<LagoonLevelsDataDTO.YearlyLagoonData> yearOverYearData = new ArrayList<>();
        for (int year = reportEndDate.getYear() - 2; year <= reportEndDate.getYear(); year++) {
            LocalDate yearStart = LocalDate.of(year, 1, 1);
            LocalDate yearEnd = LocalDate.of(year, 12, 31);
            Instant startInstant = yearStart.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endInstant = yearEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

            List<LagoonLevel> levels = lagoonLevelRepository.findByCompanyIdAndTimestampBetween(companyId, startInstant, endInstant);
            List<LagoonLevelsDataDTO.LagoonLevelData> yearData = levels.stream()
                    .map(ll -> LagoonLevelsDataDTO.LagoonLevelData.builder()
                    .farmId(ll.getFarm().getId())
                    .farmName(farmNames.getOrDefault(ll.getFarm().getId(), ll.getFarm().getId()))
                    .value(ll.getValue())
                    .timestamp(ll.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate())
                    .build())
                    .collect(Collectors.toList());

            yearOverYearData.add(LagoonLevelsDataDTO.YearlyLagoonData.builder()
                    .year(year)
                    .levels(yearData)
                    .build());
        }

        return LagoonLevelsDataDTO.builder()
                .latestLevels(latestData)
                .yearOverYearData(yearOverYearData)
                .build();
    }

    // Population Data Section
    private PopulationDataDTO fetchPopulationData(String companyId, LocalDate reportEndDate) {
        log.info("Fetching population data for company: {} up to date: {}", companyId, reportEndDate);

        // Multi-year population comparison
        List<PopulationDataDTO.YearlyPopulationData> multiYearData = new ArrayList<>();
        for (int year = reportEndDate.getYear() - 2; year <= reportEndDate.getYear(); year++) {
            LocalDate yearStart = LocalDate.of(year, 1, 1);
            LocalDate yearEnd = LocalDate.of(year, 12, 31);
            Instant startInstant = yearStart.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endInstant = yearEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

            List<Object[]> results = populationRepository.findTotalPopulationByFarmForCompanyAndDateRange(
                    companyId, startInstant, endInstant);

            Map<String, String> farmNames = getCompanyFarms(companyId).stream()
                    .collect(Collectors.toMap(Farm::getId, Farm::getName));

            double totalPopulation = 0;
            List<PopulationDataDTO.FarmPopulation> farmData = new ArrayList<>();
            for (Object[] result : results) {
                String farmId = (String) result[0];
                Double population = ((Number) result[1]).doubleValue();
                totalPopulation += population;
                farmData.add(PopulationDataDTO.FarmPopulation.builder()
                        .farmId(farmId)
                        .farmName(farmNames.getOrDefault(farmId, farmId))
                        .population(population)
                        .build());
            }

            multiYearData.add(PopulationDataDTO.YearlyPopulationData.builder()
                    .year(year)
                    .totalPopulation(totalPopulation)
                    .farmData(farmData)
                    .build());
        }

        // Year-over-year population
        LocalDate fromDate = LocalDate.of(reportEndDate.getYear() - 2, 1, 1);
        Instant fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toInstant = reportEndDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<Population> populations = populationRepository.findByCompanyIdAndTimestampBetween(companyId, fromInstant, toInstant);
        Map<String, String> farmNames = getCompanyFarms(companyId).stream()
                .collect(Collectors.toMap(Farm::getId, Farm::getName));

        Map<String, List<PopulationDataDTO.PopulationPoint>> farmPopulationMap = new HashMap<>();
        for (Population pop : populations) {
            String farmId = pop.getFarm().getId();
            farmPopulationMap.computeIfAbsent(farmId, k -> new ArrayList<>())
                    .add(PopulationDataDTO.PopulationPoint.builder()
                            .date(pop.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate())
                            .value(pop.getValue())
                            .build());
        }

        List<PopulationDataDTO.FarmYearlyPopulation> yearOverYearData = farmPopulationMap.entrySet().stream()
                .map(entry -> PopulationDataDTO.FarmYearlyPopulation.builder()
                .farmId(entry.getKey())
                .farmName(farmNames.getOrDefault(entry.getKey(), entry.getKey()))
                .populationPoints(entry.getValue())
                .build())
                .collect(Collectors.toList());

        return PopulationDataDTO.builder()
                .multiYearData(multiYearData)
                .yearOverYearData(yearOverYearData)
                .build();
    }

    // Current Year Population Forecast vs Budget Section
    private CurYearPopulationForecastVsBudgetDTO fetchCurYearPopulationForecastVsBudget(String companyId, LocalDate reportEndDate) {
        log.info("Fetching current year population forecast vs budget for company: {} up to date: {}", companyId, reportEndDate);

        int currentYear = reportEndDate.getYear();
        LocalDate yearStart = LocalDate.of(currentYear, 1, 1);
        LocalDate yearEnd = LocalDate.of(currentYear, 12, 31);
        Instant yearStartInstant = yearStart.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant yearEndInstant = yearEnd.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

        // Get all company farms
        List<Farm> companyFarms = getCompanyFarms(companyId);

        // Get population budgets for the entire year
        List<PopulationBudget> populationBudgets = populationBudgetRepository.findByCompanyIdAndTimestampBetween(
                companyId, yearStartInstant, yearEndInstant);

        // Get actual populations for the entire year
        List<Population> populations = populationRepository.findByCompanyIdAndTimestampBetween(
                companyId, yearStartInstant, yearEndInstant);

        // Create monthly aggregated data
        Map<Integer, Double> monthlyBudgets = new HashMap<>();
        Map<Integer, Double> monthlyPopulations = new HashMap<>();

        // Aggregate budgets by month
        for (PopulationBudget budget : populationBudgets) {
            LocalDate budgetDate = budget.getTimestamp().atZone(ZoneOffset.UTC).toLocalDate();
            int month = budgetDate.getMonthValue();
            monthlyBudgets.put(month, monthlyBudgets.getOrDefault(month, 0.0) + budget.getValue());
        }

        // Aggregate populations by month
        for (Population population : populations) {
            LocalDate popDate = population.getTimestamp().atZone(ZoneOffset.UTC).toLocalDate();
            int month = popDate.getMonthValue();
            monthlyPopulations.put(month, monthlyPopulations.getOrDefault(month, 0.0) + population.getValue());
        }

        // Calculate year-to-date actual population for trend analysis
        double ytdTotalPopulation = 0.0;
        int currentMonth = reportEndDate.getMonthValue();

        for (int mo = 1; mo <= currentMonth; mo++) {
            ytdTotalPopulation += monthlyPopulations.getOrDefault(mo, 0.0);
        }

        // Calculate monthly trend based on YTD data
        double monthlyTrend = currentMonth > 0 ? ytdTotalPopulation / currentMonth : 0.0;

        List<CurYearPopulationForecastVsBudgetDTO.MonthlyPopulationForecastBudget> monthlyData = new ArrayList<>();

        // Process each month
        for (int mo = 1; mo <= 12; mo++) {
            // Get budget for this month
            double monthlyBudget = monthlyBudgets.getOrDefault(mo, 0.0);

            // Get actual population for completed months, or forecast for remaining months
            double actualPopulation = 0.0;
            double forecastPopulation = 0.0;

            if (mo <= currentMonth) {
                // Completed month - use actual data
                actualPopulation = monthlyPopulations.getOrDefault(mo, 0.0);
                forecastPopulation = actualPopulation; // For completed months, forecast equals actual
            } else {
                // Future month - use trend-based forecast
                // Apply seasonal adjustment (simple version: slight increase through the year)
                double seasonalFactor = 1.0 + (mo - currentMonth) * 0.02; // 2% increase per remaining month
                forecastPopulation = monthlyTrend * seasonalFactor;
            }

            // Calculate percentages
            Double pctForecast = monthlyBudget > 0 ? (forecastPopulation / monthlyBudget) * 100 : 0.0;
            Double pctBudget = monthlyBudget > 0 ? (actualPopulation / monthlyBudget) * 100 : 0.0;

            CurYearPopulationForecastVsBudgetDTO.MonthlyPopulationForecastBudget monthData =
                    CurYearPopulationForecastVsBudgetDTO.MonthlyPopulationForecastBudget.builder()
                            .month(mo)
                            .forecast(forecastPopulation)
                            .budget(monthlyBudget)
                            .actual(actualPopulation)
                            .pctForecast(pctForecast)
                            .pctBudget(pctBudget)
                            .build();

            monthlyData.add(monthData);
        }

        return CurYearPopulationForecastVsBudgetDTO.builder()
                .year(currentYear)
                .monthlyData(monthlyData)
                .build();
    }

    // Gas Production per Inventory Section
    private GasProductionPerInventoryDTO fetchGasProductionPerInventory(String companyId, LocalDate reportEndDate) {
        log.info("Fetching gas production per inventory for company: {} up to date: {}", companyId, reportEndDate);

        // Calculate 52-week period
        LocalDate weekStart = reportEndDate.minusWeeks(51);
        Instant weekStartInstant = weekStart.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant weekEndInstant = reportEndDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

        // Get CH4 recovery data (gas production) for the 52-week period
        List<Ch4Recovery> ch4Recoveries = ch4RecoveryRepository.findByCompanyIdAndTimestampBetween(
                companyId, weekStartInstant, weekEndInstant);

        // Get population data for the same period
        List<Population> populations = populationRepository.findByCompanyIdAndTimestampBetween(
                companyId, weekStartInstant, weekEndInstant);

        // Create daily aggregated data maps
        Map<LocalDate, Double> dailyGasProduction = new HashMap<>();
        Map<LocalDate, Double> dailyPopulation = new HashMap<>();

        // Aggregate CH4 recovery by day
        for (Ch4Recovery ch4 : ch4Recoveries) {
            LocalDate date = ch4.getTimestamp().atZone(ZoneOffset.UTC).toLocalDate();
            dailyGasProduction.put(date, dailyGasProduction.getOrDefault(date, 0.0) + ch4.getValue());
        }

        // Aggregate population by day
        for (Population pop : populations) {
            LocalDate date = pop.getTimestamp().atZone(ZoneOffset.UTC).toLocalDate();
            dailyPopulation.put(date, dailyPopulation.getOrDefault(date, 0.0) + pop.getValue());
        }

        List<GasProductionPerInventoryDTO.GasProductionPerInventoryPoint> latest52Week = new ArrayList<>();

        // Process each day in the 52-week period
        LocalDate currentDate = weekStart;
        while (!currentDate.isAfter(reportEndDate)) {
            double gasProduction = dailyGasProduction.getOrDefault(currentDate, 0.0);
            double inventoryPopulation = dailyPopulation.getOrDefault(currentDate, 0.0);

            // Calculate ratio (gas per unit of population)
            double ratio = inventoryPopulation > 0 ? gasProduction / inventoryPopulation : 0.0;

            GasProductionPerInventoryDTO.GasProductionPerInventoryPoint point =
                    GasProductionPerInventoryDTO.GasProductionPerInventoryPoint.builder()
                            .date(currentDate)
                            .gasProduction(gasProduction)
                            .inventoryPopulation(inventoryPopulation)
                            .ratio(ratio)
                            .build();

            latest52Week.add(point);
            currentDate = currentDate.plusDays(1);
        }

        return GasProductionPerInventoryDTO.builder()
                .latest52Week(latest52Week)
                .build();
    }

    // Historic 52-Week Section
    private Historic52WeekDTO fetchHistoric52Week(String companyId, LocalDate reportEndDate) {
        log.info("Fetching historic 52-week data for company: {} up to date: {}", companyId, reportEndDate);

        List<Historic52WeekDTO.Categorized52WeekData> categorizedData = new ArrayList<>();

        // Define historic periods (last 5 years as example)
        int currentYear = reportEndDate.getYear();
        String[] periods = {
            String.valueOf(currentYear),           // Current year
            String.valueOf(currentYear - 1),       // Last year
            String.valueOf(currentYear - 2),       // 2 years ago
            String.valueOf(currentYear - 3),       // 3 years ago
            String.valueOf(currentYear - 4)        // 4 years ago
        };

        // Categories for different types of analysis
        String[] categories = {
            "Annual Performance",
            "Seasonal Analysis",
            "Trend Comparison",
            "Production Efficiency",
            "Inventory Utilization"
        };

        // Generate categorized data for each analysis type
        for (String category : categories) {
            List<Historic52WeekDTO.Week52Point> dataPoints = new ArrayList<>();

            for (String period : periods) {
                // Calculate data for each period based on category
                Historic52WeekDTO.Week52Point point = calculateHistoricPoint(
                        companyId, category, period, reportEndDate);
                dataPoints.add(point);
            }

            Historic52WeekDTO.Categorized52WeekData categoryData =
                    Historic52WeekDTO.Categorized52WeekData.builder()
                            .category(category)
                            .dataPoints(dataPoints)
                            .build();

            categorizedData.add(categoryData);
        }

        return Historic52WeekDTO.builder()
                .categorizedData(categorizedData)
                .build();
    }

    // Helper method to calculate historic data points
    private Historic52WeekDTO.Week52Point calculateHistoricPoint(
            String companyId, String category, String period, LocalDate reportEndDate) {

        try {
            int year = Integer.parseInt(period);
            LocalDate yearStart = LocalDate.of(year, 1, 1);
            LocalDate yearEnd = LocalDate.of(year, 12, 31);

            // Adjust for partial current year
            if (year == reportEndDate.getYear()) {
                yearEnd = reportEndDate;
            }

            Instant yearStartInstant = yearStart.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant yearEndInstant = yearEnd.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

            // Get CH4 recovery data for the period
            List<Ch4Recovery> ch4Recoveries = ch4RecoveryRepository.findByCompanyIdAndTimestampBetween(
                    companyId, yearStartInstant, yearEndInstant);

            // Get population data for the period
            List<Population> populations = populationRepository.findByCompanyIdAndTimestampBetween(
                    companyId, yearStartInstant, yearEndInstant);

            // Calculate totals
            double totalGasProduction = ch4Recoveries.stream()
                    .mapToDouble(Ch4Recovery::getValue)
                    .sum();

            double totalPopulation = populations.stream()
                    .mapToDouble(Population::getValue)
                    .sum();

            // Calculate ratio
            double ratio = totalPopulation > 0 ? totalGasProduction / totalPopulation : 0.0;

            // Apply category-specific adjustments
            switch (category) {
                case "Annual Performance":
                    // Use raw annual data
                    break;
                case "Seasonal Analysis":
                    // Apply seasonal factors (simplified)
                    ratio *= 1.1; // Example seasonal adjustment
                    break;
                case "Trend Comparison":
                    // Apply trend normalization
                    ratio *= 1.05;
                    break;
                case "Production Efficiency":
                    // Focus on efficiency metrics
                    ratio = Math.max(ratio, 0.8); // Minimum efficiency threshold
                    break;
                case "Inventory Utilization":
                    // Focus on utilization metrics
                    ratio = Math.min(ratio, 1.2); // Maximum utilization factor
                    break;
                default:
                    // No adjustment
                    break;
            }

            return Historic52WeekDTO.Week52Point.builder()
                    .period(period)
                    .gasProduction(totalGasProduction)
                    .inventoryPopulation(totalPopulation)
                    .ratio(ratio)
                    .build();

        } catch (Exception e) {
            // If there's an error (e.g., parsing year), return empty data
            return Historic52WeekDTO.Week52Point.builder()
                    .period(period)
                    .gasProduction(0.0)
                    .inventoryPopulation(0.0)
                    .ratio(0.0)
                    .build();
        }
    }

    // Helper methods
    private List<Farm> getCompanyFarms(String companyId) {
        return companyFarmRepository.findByCompanyId(companyId).stream()
                .map(CompanyFarm::getFarm)
                .collect(Collectors.toList());
    }

    // Fake data generation
    private WeeklyReportDataDTO buildFakeData(String companyId, LocalDate reportDate, LocalDate reportEndDate,
            WeeklyReportDataDTO.WeeklyReportDataDTOBuilder builder) {
        log.info("Building fake weekly report data for testing");

        // Generate fake data for all sections
        int currentYear = reportEndDate.getYear();

        // Production Summary
        List<ProductionSummaryDTO.FarmVolumeSum> wtdData = Arrays.asList(
                ProductionSummaryDTO.FarmVolumeSum.builder()
                        .farmId("farm-001")
                        .farmName("Farm Alpha")
                        .volume(500.0)
                        .build(),
                ProductionSummaryDTO.FarmVolumeSum.builder()
                        .farmId("farm-002")
                        .farmName("Farm Beta")
                        .volume(450.0)
                        .build(),
                ProductionSummaryDTO.FarmVolumeSum.builder()
                        .farmId("farm-003")
                        .farmName("Farm Gamma")
                        .volume(550.0)
                        .build()
        );

        builder.productionSummary(ProductionSummaryDTO.builder()
                .wtd(wtdData)
                .mtd(wtdData) // Using same data for simplicity
                .ytd(wtdData) // Using same data for simplicity
                .fromWTD(reportEndDate.minusDays(6))
                .fromMTD(reportEndDate.minusMonths(1))
                .fromYTD(LocalDate.of(currentYear, 1, 1))
                .reportEndDate(reportEndDate)
                .farmMonths(Arrays.asList(
                        ProductionSummaryDTO.FarmWithMonths.builder()
                                .farmId("farm-001")
                                .farmName("Farm Alpha")
                                .months(Arrays.asList(
                                        ProductionSummaryDTO.MonthlyValue.builder().mo(1).volume(3800.0).budget(4000.0).build(),
                                        ProductionSummaryDTO.MonthlyValue.builder().mo(2).volume(3900.0).budget(4100.0).build()
                                ))
                                .build()
                ))
                .build());

        // Weekly Volume vs Budget
        List<WeeklyVolumeVsBudgetDTO.WeeklyVolumeVsBudgetPoint> weeklyVolumeData = new ArrayList<>();
        LocalDate weekStart = reportEndDate.minusWeeks(11);
        for (int i = 0; i < 12; i++) {
            LocalDate weekDate = weekStart.plusWeeks(i);
            weeklyVolumeData.add(WeeklyVolumeVsBudgetDTO.WeeklyVolumeVsBudgetPoint.builder()
                    .weekStartDate(weekDate)
                    .volume(1400.0 + (i * 25))
                    .budget(1500.0 + (i * 20))
                    .pctBudget(93.3 + (i * 1.5))
                    .build());
        }
        builder.weeklyVolumeVsBudget(WeeklyVolumeVsBudgetDTO.builder()
                .fromDate(weekStart)
                .toDate(reportEndDate)
                .dataPoints(weeklyVolumeData)
                .build());

        // Production Plans
        List<ProductionPlanDTO> productionPlans = Arrays.asList(
                ProductionPlanDTO.builder()
                        .farmId("farm-001")
                        .farmName("Farm Alpha")
                        .estProduction(500.0)
                        .farmStatus("Active")
                        .build(),
                ProductionPlanDTO.builder()
                        .farmId("farm-002")
                        .farmName("Farm Beta")
                        .estProduction(450.0)
                        .farmStatus("Active")
                        .build(),
                ProductionPlanDTO.builder()
                        .farmId("farm-003")
                        .farmName("Farm Gamma")
                        .estProduction(550.0)
                        .farmStatus("Maintenance")
                        .build()
        );
        builder.productionPlans(productionPlans);

        // Daily Volume Data
        List<DailyVolumeDataDTO.DailyVolumePoint> dailyVolumePoints = new ArrayList<>();
        LocalDate dailyStart = reportEndDate.minusDays(6);
        for (int i = 0; i < 7; i++) {
            LocalDate date = dailyStart.plusDays(i);
            dailyVolumePoints.add(DailyVolumeDataDTO.DailyVolumePoint.builder()
                    .date(date)
                    .volume(200.0 + (i * 5))
                    .farmId("farm-001")
                    .farmName("Farm Alpha")
                    .build());
        }
        builder.dailyVolumeData(DailyVolumeDataDTO.builder()
                .fromDate(dailyStart)
                .toDate(reportEndDate)
                .dataPoints(dailyVolumePoints)
                .build());

        // Lagoon Levels Data
        List<LagoonLevelsDataDTO.LagoonLevelData> lagoonPoints = Arrays.asList(
                LagoonLevelsDataDTO.LagoonLevelData.builder()
                        .farmId("farm-001")
                        .farmName("Farm Alpha")
                        .value(75.0)
                        .timestamp(reportEndDate)
                        .build(),
                LagoonLevelsDataDTO.LagoonLevelData.builder()
                        .farmId("farm-002")
                        .farmName("Farm Beta")
                        .value(82.0)
                        .timestamp(reportEndDate)
                        .build(),
                LagoonLevelsDataDTO.LagoonLevelData.builder()
                        .farmId("farm-003")
                        .farmName("Farm Gamma")
                        .value(91.0)
                        .timestamp(reportEndDate)
                        .build()
        );
        builder.lagoonLevelsData(LagoonLevelsDataDTO.builder()
                .latestLevels(lagoonPoints)
                .yearOverYearData(Arrays.asList(
                        LagoonLevelsDataDTO.YearlyLagoonData.builder()
                                .year(currentYear)
                                .levels(lagoonPoints)
                                .build()
                ))
                .build());

        // YTD/MTD Pie Data
        List<YtdMtdPieDataDTO.YtdPieData> ytdPieData = Arrays.asList(
                YtdMtdPieDataDTO.YtdPieData.builder()
                        .farmName("Farm Alpha")
                        .percentage(33.3)
                        .build(),
                YtdMtdPieDataDTO.YtdPieData.builder()
                        .farmName("Farm Beta")
                        .percentage(30.0)
                        .build(),
                YtdMtdPieDataDTO.YtdPieData.builder()
                        .farmName("Farm Gamma")
                        .percentage(36.7)
                        .build()
        );

        List<YtdMtdPieDataDTO.MtdPieData> mtdPieData = Arrays.asList(
                YtdMtdPieDataDTO.MtdPieData.builder()
                        .farmName("Farm Alpha")
                        .total(1166.7)
                        .build(),
                YtdMtdPieDataDTO.MtdPieData.builder()
                        .farmName("Farm Beta")
                        .total(1050.0)
                        .build(),
                YtdMtdPieDataDTO.MtdPieData.builder()
                        .farmName("Farm Gamma")
                        .total(1283.3)
                        .build()
        );

        builder.ytdMtdPieData(YtdMtdPieDataDTO.builder()
                .ytdPieData(ytdPieData)
                .mtdPieData(mtdPieData)
                .build());

        // Production Budget Comparison
        List<ProductionBudgetComparisonDTO.FarmProductionBudget> farmProductions = Arrays.asList(
                ProductionBudgetComparisonDTO.FarmProductionBudget.builder()
                        .farmName("Farm Alpha")
                        .production(5000.0)
                        .pctBudget(96.2)
                        .build(),
                ProductionBudgetComparisonDTO.FarmProductionBudget.builder()
                        .farmName("Farm Beta")
                        .production(4500.0)
                        .pctBudget(95.7)
                        .build(),
                ProductionBudgetComparisonDTO.FarmProductionBudget.builder()
                        .farmName("Farm Gamma")
                        .production(5500.0)
                        .pctBudget(101.9)
                        .build()
        );
        builder.productionBudgetComparison(ProductionBudgetComparisonDTO.builder()
                .totalVolume(15000.0)
                .farmProductions(farmProductions)
                .build());

        // Multi-Year Production vs Budget
        List<MultiYearProductionVsBudgetDTO.YearProductionBudget> multiYearData = Arrays.asList(
                MultiYearProductionVsBudgetDTO.YearProductionBudget.builder()
                        .year(currentYear - 2)
                        .production(48000.0)
                        .budget(50000.0)
                        .pctBudget(96.0)
                        .build(),
                MultiYearProductionVsBudgetDTO.YearProductionBudget.builder()
                        .year(currentYear - 1)
                        .production(51000.0)
                        .budget(52000.0)
                        .pctBudget(98.1)
                        .build(),
                MultiYearProductionVsBudgetDTO.YearProductionBudget.builder()
                        .year(currentYear)
                        .production(45000.0)
                        .budget(48000.0)
                        .pctBudget(93.8)
                        .build()
        );
        builder.multiYearProductionVsBudget(MultiYearProductionVsBudgetDTO.builder()
                .years(Arrays.asList(currentYear - 2, currentYear - 1, currentYear))
                .yearData(multiYearData)
                .build());

        // Current Year Production Forecast vs Budget
        List<CurYearProductionForecastVsBudgetDTO.MonthlyForecastBudget> monthlyData = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            monthlyData.add(CurYearProductionForecastVsBudgetDTO.MonthlyForecastBudget.builder()
                    .month(month)
                    .forecast(3800.0 + (month * 50))
                    .budget(4000.0 + (month * 40))
                    .actual(month <= reportEndDate.getMonthValue() ? 3600.0 + (month * 45) : 0.0)
                    .pctForecast(95.0 + (month * 0.5))
                    .pctBudget(month <= reportEndDate.getMonthValue() ? 90.0 + (month * 0.8) : 0.0)
                    .build());
        }
        builder.curYearProductionForecastVsBudget(CurYearProductionForecastVsBudgetDTO.builder()
                .year(currentYear)
                .monthlyData(monthlyData)
                .build());

        // Year-over-Year Monthly Production
        List<YearOverYearMonthlyProductionDTO.YearlyMonthlyData> yoyData = new ArrayList<>();
        for (int yearOffset = 2; yearOffset >= 0; yearOffset--) {
            int year = currentYear - yearOffset;
            List<YearOverYearMonthlyProductionDTO.MonthlyProduction> monthlyProd = new ArrayList<>();
            for (int month = 1; month <= 12; month++) {
                monthlyProd.add(YearOverYearMonthlyProductionDTO.MonthlyProduction.builder()
                        .month(month)
                        .production(3500.0 + (yearOffset * 100) + (month * 30))
                        .build());
            }
            yoyData.add(YearOverYearMonthlyProductionDTO.YearlyMonthlyData.builder()
                    .year(year)
                    .monthlyData(monthlyProd)
                    .build());
        }
        builder.yearOverYearMonthlyProduction(YearOverYearMonthlyProductionDTO.builder()
                .years(Arrays.asList(currentYear - 2, currentYear - 1, currentYear))
                .yearData(yoyData)
                .build());

        // Population Data
        List<PopulationDataDTO.YearlyPopulationData> popMultiYearData = Arrays.asList(
                PopulationDataDTO.YearlyPopulationData.builder()
                        .year(currentYear - 2)
                        .totalPopulation(14000.0)
                        .farmData(Arrays.asList(
                                PopulationDataDTO.FarmPopulation.builder()
                                        .farmId("farm-001")
                                        .farmName("Farm Alpha")
                                        .population(4667.0)
                                        .build(),
                                PopulationDataDTO.FarmPopulation.builder()
                                        .farmId("farm-002")
                                        .farmName("Farm Beta")
                                        .population(4667.0)
                                        .build(),
                                PopulationDataDTO.FarmPopulation.builder()
                                        .farmId("farm-003")
                                        .farmName("Farm Gamma")
                                        .population(4666.0)
                                        .build()
                        ))
                        .build(),
                PopulationDataDTO.YearlyPopulationData.builder()
                        .year(currentYear - 1)
                        .totalPopulation(14500.0)
                        .farmData(Arrays.asList(
                                PopulationDataDTO.FarmPopulation.builder()
                                        .farmId("farm-001")
                                        .farmName("Farm Alpha")
                                        .population(4833.0)
                                        .build(),
                                PopulationDataDTO.FarmPopulation.builder()
                                        .farmId("farm-002")
                                        .farmName("Farm Beta")
                                        .population(4834.0)
                                        .build(),
                                PopulationDataDTO.FarmPopulation.builder()
                                        .farmId("farm-003")
                                        .farmName("Farm Gamma")
                                        .population(4833.0)
                                        .build()
                        ))
                        .build(),
                PopulationDataDTO.YearlyPopulationData.builder()
                        .year(currentYear)
                        .totalPopulation(15000.0)
                        .farmData(Arrays.asList(
                                PopulationDataDTO.FarmPopulation.builder()
                                        .farmId("farm-001")
                                        .farmName("Farm Alpha")
                                        .population(5000.0)
                                        .build(),
                                PopulationDataDTO.FarmPopulation.builder()
                                        .farmId("farm-002")
                                        .farmName("Farm Beta")
                                        .population(5000.0)
                                        .build(),
                                PopulationDataDTO.FarmPopulation.builder()
                                        .farmId("farm-003")
                                        .farmName("Farm Gamma")
                                        .population(5000.0)
                                        .build()
                        ))
                        .build()
        );

        List<PopulationDataDTO.FarmYearlyPopulation> yearOverYearData = Arrays.asList(
                PopulationDataDTO.FarmYearlyPopulation.builder()
                        .farmId("farm-001")
                        .farmName("Farm Alpha")
                        .populationPoints(Arrays.asList(
                                PopulationDataDTO.PopulationPoint.builder()
                                        .date(LocalDate.of(currentYear, 1, 1))
                                        .value(5000.0)
                                        .build(),
                                PopulationDataDTO.PopulationPoint.builder()
                                        .date(reportEndDate)
                                        .value(5000.0)
                                        .build()
                        ))
                        .build()
        );

        builder.populationData(PopulationDataDTO.builder()
                .multiYearData(popMultiYearData)
                .yearOverYearData(yearOverYearData)
                .build());

        // Current Year Population Forecast vs Budget
        List<CurYearPopulationForecastVsBudgetDTO.MonthlyPopulationForecastBudget> popMonthlyData = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            popMonthlyData.add(CurYearPopulationForecastVsBudgetDTO.MonthlyPopulationForecastBudget.builder()
                    .month(month)
                    .forecast(12000.0 + (month * 80))
                    .budget(12500.0 + (month * 70))
                    .actual(month <= reportEndDate.getMonthValue() ? 11800.0 + (month * 75) : 0.0)
                    .pctForecast(96.0 + (month * 0.3))
                    .pctBudget(month <= reportEndDate.getMonthValue() ? 94.4 + (month * 0.6) : 0.0)
                    .build());
        }
        builder.curYearPopulationForecastVsBudget(CurYearPopulationForecastVsBudgetDTO.builder()
                .year(currentYear)
                .monthlyData(popMonthlyData)
                .build());

        // Gas Production per Inventory
        List<GasProductionPerInventoryDTO.GasProductionPerInventoryPoint> gasData = new ArrayList<>();
        LocalDate gasStart = reportEndDate.minusWeeks(51);
        LocalDate gasDate = gasStart;
        while (!gasDate.isAfter(reportEndDate)) {
            gasData.add(GasProductionPerInventoryDTO.GasProductionPerInventoryPoint.builder()
                    .date(gasDate)
                    .gasProduction(200.0 + Math.random() * 50)
                    .inventoryPopulation(12000.0 + Math.random() * 2000)
                    .ratio(0.016 + Math.random() * 0.004)
                    .build());
            gasDate = gasDate.plusDays(7);
        }
        builder.gasProductionPerInventory(GasProductionPerInventoryDTO.builder()
                .latest52Week(gasData)
                .build());

        // Historic 52-Week Data
        List<Historic52WeekDTO.Categorized52WeekData> historicData = new ArrayList<>();
        String[] categories = {"Annual Performance", "Seasonal Analysis", "Trend Comparison"};
        for (String category : categories) {
            List<Historic52WeekDTO.Week52Point> points = new ArrayList<>();
            for (int yearOffset = 4; yearOffset >= 0; yearOffset--) {
                int year = currentYear - yearOffset;
                points.add(Historic52WeekDTO.Week52Point.builder()
                        .period(String.valueOf(year))
                        .gasProduction(45000.0 + (yearOffset * 2000))
                        .inventoryPopulation(14000.0 + (yearOffset * 500))
                        .ratio(3.2 + (yearOffset * 0.1))
                        .build());
            }
            historicData.add(Historic52WeekDTO.Categorized52WeekData.builder()
                    .category(category)
                    .dataPoints(points)
                    .build());
        }
        builder.historic52Week(Historic52WeekDTO.builder()
                .categorizedData(historicData)
                .build());

        return builder.build();
    }
}
