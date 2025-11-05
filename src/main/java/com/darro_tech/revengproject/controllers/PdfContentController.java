package com.darro_tech.revengproject.controllers;

import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.darro_tech.revengproject.dto.WeeklyReportDataDTO;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.WeeklyReportDataService;
import com.darro_tech.revengproject.utils.DateHelper;

/**
 * Controller for PDF content routes matching Next.js format
 */
@Controller
public class PdfContentController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(PdfContentController.class);

    @Autowired
    private WeeklyReportDataService weeklyReportDataService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private com.darro_tech.revengproject.services.DailyReportDataService dailyReportDataService;

    /**
     * Weekly report content route matching Next.js: GET
     * /pdf/{companyKey}/weekly-report/content/
     */
    @GetMapping("/pdf/{companyKey}/weekly-report/content/")
    public String weeklyReportContent(
            @PathVariable String companyKey,
            @RequestParam(required = false) String api_key,
            @RequestParam(required = false) String sel_date,
            @RequestParam(required = false, defaultValue = "print") String media,
            @RequestParam(required = false, defaultValue = "false") boolean useFakeData,
            Model model) {

        logger.info("📄 Rendering weekly report content for companyKey: {}, date: {}, media: {}",
                companyKey, sel_date, media);

        try {
            // Find company by key (companyKey is lowercase with hyphens)
            Optional<com.darro_tech.revengproject.models.Company> companyOpt = companyService.getAllCompanies().stream()
                    .filter(c -> formatCompanyKey(c.getName()).equals(companyKey))
                    .findFirst();

            if (companyOpt.isEmpty()) {
                logger.warn("⚠️ Company not found for key: {}", companyKey);
                model.addAttribute("errorMessage", "Company not found");
                model.addAttribute("errorDetails", "No company found matching key: " + companyKey);
                return "pdf/weekly-report-content";
            }

            com.darro_tech.revengproject.models.Company company = companyOpt.get();
            String companyId = company.getId();

            // Parse date or use current date
            LocalDate reportDate;
            if (sel_date != null && !sel_date.isEmpty()) {
                try {
                    reportDate = LocalDate.parse(sel_date);
                } catch (Exception e) {
                    logger.warn("⚠️ Invalid date format: {}, using current date", sel_date);
                    reportDate = LocalDate.now();
                }
            } else {
                reportDate = LocalDate.now();
            }

            // Get start of week (Monday) for the selected date
            LocalDate reportEndDate = DateHelper.getStartOfWeek(reportDate, 1).plusDays(6);

            // Fetch comprehensive weekly report data
            WeeklyReportDataDTO reportData = weeklyReportDataService.getWeeklyReportData(
                    companyId, reportDate, useFakeData);

            // Add data to model
            model.addAttribute("reportData", reportData);
            model.addAttribute("companyName", company.getName());
            model.addAttribute("companyKey", companyKey);
            model.addAttribute("reportDate", reportDate);
            model.addAttribute("reportEndDate", reportEndDate);
            model.addAttribute("media", media);
            model.addAttribute("useFakeData", useFakeData);

            logger.info("✅ Successfully loaded weekly report data for company: {}", company.getName());

        } catch (Exception e) {
            logger.error("❌ Error loading weekly report data", e);
            model.addAttribute("errorMessage", "ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            model.addAttribute("errorDetails", "This indicates a code issue. Check logs.");
        }

        return "pdf/weekly-report-content";
    }

    /**
     * Daily report content route matching Next.js: GET
     * /pdf/{companyKey}/daily-report/content/
     */
    @GetMapping("/pdf/{companyKey}/daily-report/content/")
    public String dailyReportContent(
            @PathVariable String companyKey,
            @RequestParam(required = false) String api_key,
            @RequestParam(required = false) String sel_date,
            @RequestParam(required = false, defaultValue = "print") String media,
            @RequestParam(required = false, defaultValue = "false") boolean useFakeData,
            Model model) {

        logger.info("📄 Rendering daily report content for companyKey: {}, date: {}, media: {}",
                companyKey, sel_date, media);

        try {
            // Find company by key
            Optional<com.darro_tech.revengproject.models.Company> companyOpt = companyService.getAllCompanies().stream()
                    .filter(c -> formatCompanyKey(c.getName()).equals(companyKey))
                    .findFirst();

            if (companyOpt.isEmpty()) {
                logger.warn("⚠️ Company not found for key: {}", companyKey);
                model.addAttribute("errorMessage", "Company not found");
                model.addAttribute("errorDetails", "No company found matching key: " + companyKey);
                return "pdf/daily-report-content";
            }

            com.darro_tech.revengproject.models.Company company = companyOpt.get();

            // Parse date or use current date
            LocalDate reportDate;
            if (sel_date != null && !sel_date.isEmpty()) {
                try {
                    reportDate = LocalDate.parse(sel_date);
                } catch (Exception e) {
                    logger.warn("⚠️ Invalid date format: {}, using current date", sel_date);
                    reportDate = LocalDate.now();
                }
            } else {
                reportDate = LocalDate.now();
            }

            // Fetch daily report data
            com.darro_tech.revengproject.dto.DailyReportDTO reportData = 
                    dailyReportDataService.getDailyReportData(company.getId(), reportDate);

            // Add data to model
            model.addAttribute("reportData", reportData);
            model.addAttribute("companyName", company.getName());
            model.addAttribute("companyKey", companyKey);
            model.addAttribute("reportDate", reportDate);
            model.addAttribute("media", media);
            model.addAttribute("useFakeData", useFakeData);

            logger.info("✅ Successfully loaded daily report data for company: {}", company.getName());

        } catch (Exception e) {
            logger.error("❌ Error loading daily report data", e);
            model.addAttribute("errorMessage", "ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            model.addAttribute("errorDetails", "This indicates a code issue. Check logs.");
        }

        return "pdf/daily-report-content";
    }

    /**
     * Format company name to company key (lowercase, replace spaces with
     * hyphens)
     */
    private String formatCompanyKey(String companyName) {
        if (companyName == null) {
            return "";
        }
        return companyName.toLowerCase().replaceAll("\\s+", "-");
    }
}


