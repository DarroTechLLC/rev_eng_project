package com.darro_tech.revengproject.controllers.api;

import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.models.DailyReportCompany;
import com.darro_tech.revengproject.models.WeeklyReportCompany;
import com.darro_tech.revengproject.repositories.DailyReportCompanyRepository;
import com.darro_tech.revengproject.repositories.WeeklyReportCompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for report-related API endpoints.
 * Handles requests for PDF reports and their timestamps.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportApiController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ReportApiController.class);

    @Autowired
    private WeeklyReportCompanyRepository weeklyReportRepository;

    @Autowired
    private DailyReportCompanyRepository dailyReportRepository;

    /**
     * Get the timestamp for a weekly PDF report.
     * 
     * @param companyId The ID of the company
     * @param date The date for the report
     * @return A response containing the timestamp
     */
    @GetMapping("/weekly-pdf/timestamp")
    public ResponseEntity<Map<String, Object>> getWeeklyPdfTimestamp(
            @RequestParam("company_id") String companyId, 
            @RequestParam("date") String date) {

        logger.info("📊 Processing weekly PDF timestamp request - companyId: {}, date: {}", companyId, date);

        try {
            // Parse the date string to Instant
            LocalDate localDate = LocalDate.parse(date);
            Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

            // Find the weekly report for the company and date
            List<WeeklyReportCompany> reports = weeklyReportRepository.findByCompanyIdAndDate(companyId, instant);

            // If no reports found with the exact date, try a date range approach
            if (reports.isEmpty()) {
                logger.info("🔍 No reports found with exact date match, trying date range for companyId: {}, date: {}", companyId, date);

                // Create a date range for the entire day (from start of day to start of next day)
                Instant startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant startOfNextDay = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

                reports = weeklyReportRepository.findByCompanyIdAndDateBetween(companyId, startOfDay, startOfNextDay);

                if (!reports.isEmpty()) {
                    logger.info("✅ Found {} reports using date range for companyId: {}, date: {}", reports.size(), companyId, date);
                }
            }

            Map<String, Object> response = new HashMap<>();
            if (!reports.isEmpty()) {
                // Return the timestamp of the first report
                response.put("timestamp", reports.get(0).getTimestamp().toString());
                logger.info("⏱️ Returning timestamp: {} for date: {}", reports.get(0).getTimestamp(), date);
            } else {
                // If no report is found, return the current timestamp
                response.put("timestamp", Instant.now().toString());
                logger.warn("⚠️ No weekly report found for companyId: {}, date: {}", companyId, date);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing request: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "An error occurred while processing the request");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get a weekly PDF report.
     * 
     * @param companyName The name of the company
     * @param companyId The ID of the company
     * @param date The date for the report
     * @return The PDF report as a resource
     */
    @GetMapping({"/weekly-pdf/{companyName}", "/weekly-pdf/{companyName}/"})
    public ResponseEntity<?> getWeeklyPdf(
            @PathVariable String companyName,
            @RequestParam("company_id") String companyId,
            @RequestParam("date") String date) {

        logger.info("📊 Processing weekly PDF request - companyName: {}, companyId: {}, date: {}", 
                   companyName, companyId, date);

        try {
            // Parse the date string to Instant
            LocalDate localDate = LocalDate.parse(date);
            Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

            // Find the weekly report for the company and date
            List<WeeklyReportCompany> reports = weeklyReportRepository.findByCompanyIdAndDate(companyId, instant);

            // If no reports found with the exact date, try a date range approach
            if (reports.isEmpty()) {
                logger.info("🔍 No reports found with exact date match, trying date range for companyId: {}, date: {}", companyId, date);

                // Create a date range for the entire day (from start of day to start of next day)
                Instant startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant startOfNextDay = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

                reports = weeklyReportRepository.findByCompanyIdAndDateBetween(companyId, startOfDay, startOfNextDay);

                if (!reports.isEmpty()) {
                    logger.info("✅ Found {} reports using date range for companyId: {}, date: {}", reports.size(), companyId, date);
                }
            }

            if (!reports.isEmpty()) {
                // Return the PDF from the database
                byte[] pdfBytes = reports.get(0).getPdf();
                logger.info("📄 Returning PDF report with timestamp: {} for date: {}", reports.get(0).getTimestamp(), date);

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header("Content-Disposition", "inline; filename=\"" + companyName + "-weekly-report.pdf\"")
                        .body(pdfBytes);
            } else {
                // If no report is found, return a placeholder PDF
                logger.warn("⚠️ No weekly report found for companyId: {}, date: {}", companyId, date);
                Resource resource = new ClassPathResource("static/img/undraw_profile.svg");
                byte[] svgBytes = resource.getInputStream().readAllBytes();

                return ResponseEntity.ok()
                        .contentType(MediaType.valueOf("image/svg+xml"))
                        .body(svgBytes);
            }
        } catch (Exception e) {
            logger.error("❌ Error processing request: {}", e.getMessage(), e);
            // Return a JSON error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "An error occurred while processing the request");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    /**
     * Get the timestamp for a daily PDF report.
     * 
     * @param companyId The ID of the company
     * @param date The date for the report
     * @return A response containing the timestamp
     */
    @GetMapping("/daily-pdf/timestamp")
    public ResponseEntity<Map<String, Object>> getDailyPdfTimestamp(
            @RequestParam("company_id") String companyId, 
            @RequestParam("date") String date) {

        logger.info("📊 Processing daily PDF timestamp request - companyId: {}, date: {}", companyId, date);

        try {
            // Parse the date string to Instant
            LocalDate localDate = LocalDate.parse(date);
            Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

            // Find the daily report for the company and date
            List<DailyReportCompany> reports = dailyReportRepository.findByCompanyIdAndDate(companyId, instant);

            // If no reports found with the exact date, try a date range approach
            if (reports.isEmpty()) {
                logger.info("🔍 No reports found with exact date match, trying date range for companyId: {}, date: {}", companyId, date);

                // Create a date range for the entire day (from start of day to start of next day)
                Instant startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant startOfNextDay = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

                reports = dailyReportRepository.findByCompanyIdAndDateBetween(companyId, startOfDay, startOfNextDay);

                if (!reports.isEmpty()) {
                    logger.info("✅ Found {} reports using date range for companyId: {}, date: {}", reports.size(), companyId, date);
                }
            }

            Map<String, Object> response = new HashMap<>();
            if (!reports.isEmpty()) {
                // Return the timestamp of the first report
                response.put("timestamp", reports.get(0).getTimestamp().toString());
                logger.info("⏱️ Returning timestamp: {} for date: {}", reports.get(0).getTimestamp(), date);
            } else {
                // If no report is found, return the current timestamp
                response.put("timestamp", Instant.now().toString());
                logger.warn("⚠️ No daily report found for companyId: {}, date: {}", companyId, date);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing request: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "An error occurred while processing the request");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get a daily PDF report.
     * 
     * @param companyName The name of the company
     * @param companyId The ID of the company
     * @param date The date for the report
     * @return The PDF report as a resource
     */
    @GetMapping({"/daily-pdf/{companyName}", "/daily-pdf/{companyName}/"})
    public ResponseEntity<?> getDailyPdf(
            @PathVariable String companyName,
            @RequestParam("company_id") String companyId,
            @RequestParam("date") String date) {

        logger.info("📊 Processing daily PDF request - companyName: {}, companyId: {}, date: {}", 
                   companyName, companyId, date);

        try {
            // Parse the date string to Instant
            LocalDate localDate = LocalDate.parse(date);
            Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

            // Find the daily report for the company and date
            List<DailyReportCompany> reports = dailyReportRepository.findByCompanyIdAndDate(companyId, instant);

            // If no reports found with the exact date, try a date range approach
            if (reports.isEmpty()) {
                logger.info("🔍 No reports found with exact date match, trying date range for companyId: {}, date: {}", companyId, date);

                // Create a date range for the entire day (from start of day to start of next day)
                Instant startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant startOfNextDay = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

                reports = dailyReportRepository.findByCompanyIdAndDateBetween(companyId, startOfDay, startOfNextDay);

                if (!reports.isEmpty()) {
                    logger.info("✅ Found {} reports using date range for companyId: {}, date: {}", reports.size(), companyId, date);
                }
            }

            if (!reports.isEmpty()) {
                // Return the PDF from the database
                byte[] pdfBytes = reports.get(0).getPdf();
                logger.info("📄 Returning PDF report with timestamp: {} for date: {}", reports.get(0).getTimestamp(), date);

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header("Content-Disposition", "inline; filename=\"" + companyName + "-daily-report.pdf\"")
                        .body(pdfBytes);
            } else {
                // If no report is found, return a placeholder PDF
                logger.warn("⚠️ No daily report found for companyId: {}, date: {}", companyId, date);
                Resource resource = new ClassPathResource("static/img/undraw_profile.svg");
                byte[] svgBytes = resource.getInputStream().readAllBytes();

                return ResponseEntity.ok()
                        .contentType(MediaType.valueOf("image/svg+xml"))
                        .body(svgBytes);
            }
        } catch (Exception e) {
            logger.error("❌ Error processing request: {}", e.getMessage(), e);
            // Return a JSON error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "An error occurred while processing the request");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }
}
