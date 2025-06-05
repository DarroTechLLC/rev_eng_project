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
            Optional<WeeklyReportCompany> report = weeklyReportRepository.findByCompanyIdAndDate(companyId, instant);

            Map<String, Object> response = new HashMap<>();
            if (report.isPresent()) {
                // Return the timestamp of the report
                response.put("timestamp", report.get().getTimestamp().toString());
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
    public ResponseEntity<Resource> getWeeklyPdf(
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
            Optional<WeeklyReportCompany> report = weeklyReportRepository.findByCompanyIdAndDate(companyId, instant);

            if (report.isPresent()) {
                // Return the PDF from the database
                byte[] pdfBytes = report.get().getPdf();
                ByteArrayResource resource = new ByteArrayResource(pdfBytes);

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(resource);
            } else {
                // If no report is found, return a placeholder PDF
                logger.warn("⚠️ No weekly report found for companyId: {}, date: {}", companyId, date);
                Resource resource = new ClassPathResource("static/img/undraw_profile.svg");

                return ResponseEntity.ok()
                        .contentType(MediaType.valueOf("image/svg+xml"))
                        .body(resource);
            }
        } catch (Exception e) {
            logger.error("❌ Error processing request: {}", e.getMessage(), e);
            // Return a ByteArrayResource with a JSON error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ByteArrayResource(
                            ("{\"error\":true,\"message\":\"An error occurred while processing the request\"}").getBytes()));
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
            Optional<DailyReportCompany> report = dailyReportRepository.findByCompanyIdAndDate(companyId, instant);

            Map<String, Object> response = new HashMap<>();
            if (report.isPresent()) {
                // Return the timestamp of the report
                response.put("timestamp", report.get().getTimestamp().toString());
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
    public ResponseEntity<Resource> getDailyPdf(
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
            Optional<DailyReportCompany> report = dailyReportRepository.findByCompanyIdAndDate(companyId, instant);

            if (report.isPresent()) {
                // Return the PDF from the database
                byte[] pdfBytes = report.get().getPdf();
                ByteArrayResource resource = new ByteArrayResource(pdfBytes);

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(resource);
            } else {
                // If no report is found, return a placeholder PDF
                logger.warn("⚠️ No daily report found for companyId: {}, date: {}", companyId, date);
                Resource resource = new ClassPathResource("static/img/undraw_profile.svg");

                return ResponseEntity.ok()
                        .contentType(MediaType.valueOf("image/svg+xml"))
                        .body(resource);
            }
        } catch (Exception e) {
            logger.error("❌ Error processing request: {}", e.getMessage(), e);
            // Return a ByteArrayResource with a JSON error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ByteArrayResource(
                            ("{\"error\":true,\"message\":\"An error occurred while processing the request\"}").getBytes()));
        }
    }
}
