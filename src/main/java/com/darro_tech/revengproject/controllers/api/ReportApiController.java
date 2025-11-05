package com.darro_tech.revengproject.controllers.api;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.models.DailyReportCompany;
import com.darro_tech.revengproject.repositories.DailyReportCompanyRepository;
import com.darro_tech.revengproject.services.DailyReportService;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/reports")
public class ReportApiController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ReportApiController.class);
    
    @PostConstruct
    public void init() {
        logger.info("✅ ReportApiController initialized - endpoints registered:");
        logger.info("   GET /api/reports/daily-pdf/timestamp");
        logger.info("   GET /api/reports/daily-pdf/{pdfFileName}");
        logger.info("   GET /api/reports/daily-pdf/health");
    }

    @Autowired
    private DailyReportCompanyRepository dailyReportRepository;

    @Autowired
    private DailyReportService dailyReportService;

    @Autowired
    private com.darro_tech.revengproject.services.FakeDataService fakeDataService;

    @Autowired
    private com.darro_tech.revengproject.services.PdfGenerationService pdfGenerationService;

    /**
     * Health check endpoint for daily report API
     */
    @GetMapping({"/daily-pdf/health", "/daily-pdf/health/"})
    public ResponseEntity<?> healthCheck() {
        logger.info("✅ Daily report API health check");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(java.util.Map.of("status", "ok", "message", "Daily report API is running"));
    }

    /**
     * Get daily report PDF timestamp (matching Next.js API format)
     * Note: This endpoint must be registered before the /daily-pdf/{pdfFileName} endpoint
     * to ensure proper path matching
     */
    @GetMapping({"/daily-pdf/timestamp", "/daily-pdf/timestamp/"})
    public ResponseEntity<?> getDailyPdfTimestamp(
            @RequestParam("company_id") String companyId,
            @RequestParam("date") String date,
            @RequestParam(required = false, defaultValue = "false") String useFakeData) {

        logger.info("🕒 Getting timestamp for daily report - companyId: {}, date: {}, useFakeData: {}", companyId, date, useFakeData);

        try {
            boolean isFakeData = "true".equalsIgnoreCase(useFakeData);

            if (isFakeData) {
                // Return fake timestamp for fake data
                LocalDate localDate = LocalDate.parse(date);
                java.time.ZonedDateTime zonedDateTime = localDate.atStartOfDay(java.time.ZoneId.systemDefault());
                long fakeTimestamp = zonedDateTime.toInstant().toEpochMilli();

                logger.info("🎭 Returning fake timestamp for daily report");
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .body(java.util.Map.of("timestamp", fakeTimestamp));
            }

            LocalDate localDate = LocalDate.parse(date);
            java.util.Date timestamp = dailyReportService.getLatestTimestamp(companyId, localDate);

            if (timestamp == null) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .body("{\"error\": \"No report available for the selected date.\"}");
            }

            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(java.util.Map.of("timestamp", timestamp.getTime()));
        } catch (Exception e) {
            logger.error("❌ Error getting timestamp", e);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"An error occurred while retrieving the timestamp.\"}");
        }
    }

    @GetMapping(value = {
            "/daily-pdf/{pdfFileName}",
            "/daily-pdf/{pdfFileName}/"
    }, produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getDailyPdf(
            @PathVariable String pdfFileName,
            @RequestParam("company_id") String companyId,
            @RequestParam("date") String date,
            @RequestParam(required = false, defaultValue = "false") String download,
            HttpServletRequest request) {

        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        logger.info("🔍 Received request - URI: {}, Query: {}, pdfFileName: '{}', companyId: {}, date: {}", 
                requestUri, queryString, pdfFileName, companyId, date);

        // Remove trailing slash from pdfFileName if present
        if (pdfFileName != null && pdfFileName.endsWith("/")) {
            pdfFileName = pdfFileName.substring(0, pdfFileName.length() - 1);
            logger.info("🔧 Removed trailing slash, pdfFileName now: '{}'", pdfFileName);
        }

        // Reject reserved paths that should be handled by specific endpoints
        if ("timestamp".equals(pdfFileName) || "health".equals(pdfFileName)) {
            logger.warn("❌ Reserved path '{}' attempted via path variable endpoint", pdfFileName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"Invalid path. Use the specific endpoint for this resource.\"}");
        }

        logger.info("📊 Processing daily PDF request - pdfFileName: {}, companyId: {}, date: {}",
                pdfFileName, companyId, date);

        try {
            // Parse the date string to Instant
            LocalDate localDate;
            try {
                localDate = LocalDate.parse(date);
            } catch (Exception e) {
                logger.warn("❌ Invalid date format: {}", date);
                return ResponseEntity.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": \"Invalid date format. Please use YYYY-MM-DD format.\"}");
            }

            // Check if date is in the future
            if (localDate.isAfter(LocalDate.now())) {
                logger.warn("❌ Future date requested: {}", date);
                return ResponseEntity.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": \"Cannot request reports for future dates.\"}");
            }

            Instant startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant startOfNextDay = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            // Find reports for the given date range
            List<DailyReportCompany> reports = dailyReportRepository.findByCompanyIdAndDateBetween(
                    companyId, startOfDay, startOfNextDay);

            byte[] pdfData = null;

            if (!reports.isEmpty()) {
                // Get the first report's PDF data
                DailyReportCompany report = reports.get(0);
                pdfData = report.getPdf();
            }

            // If no PDF data exists in database, generate it on the fly
            if (pdfData == null || pdfData.length == 0) {
                logger.info("📄 No PDF data found in database for company {} on date {}, generating on the fly...", companyId, date);
                try {
                    pdfData = pdfGenerationService.generateDailyReportPdf(companyId, localDate);
                    logger.info("✅ Successfully generated PDF on the fly for company {} on date {}", companyId, date);
                } catch (Exception e) {
                    logger.error("❌ Error generating PDF on the fly", e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"error\": \"Failed to generate report. Please try again later.\"}");
                }
            } else {
                logger.info("✅ Using existing PDF data from database for company {} on date {}", companyId, date);
            }

            // Set headers for PDF response
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // Use "attachment" for download, omit Content-Disposition for inline display
            // Some browsers (like Chrome) download PDFs in iframes even with "inline"
            // So we omit the header entirely for inline display
            boolean isDownload = "true".equalsIgnoreCase(download);
            if (isDownload) {
                headers.setContentDispositionFormData("attachment", pdfFileName + ".pdf");
                logger.info("📥 Setting Content-Disposition: attachment for download");
            } else {
                // For inline display, don't set Content-Disposition header at all
                // Browsers will display the PDF inline when it's in an iframe
                // Only set Content-Type: application/pdf
                logger.info("📄 No Content-Disposition header for inline iframe display");
            }
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            // Add X-Content-Type-Options to prevent MIME sniffing
            headers.set("X-Content-Type-Options", "nosniff");

            logger.info("✅ Successfully retrieved PDF data for company {} on date {}", companyId, date);
            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("❌ Error getting PDF data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"An error occurred while retrieving the report. Please try again later.\"}");
        }
    }

    /**
     * Get fake PDF for testing (weekly report)
     */
    @GetMapping("/fake-pdf/weekly")
    public ResponseEntity<byte[]> getFakeWeeklyPdf(
            @RequestParam("company_id") String companyId,
            @RequestParam("date") String date,
            @RequestParam("company_name") String companyName,
            @RequestParam(required = false, defaultValue = "false") Boolean download) {

        try {
            // Decode URL-encoded company name
            companyName = java.net.URLDecoder.decode(companyName, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("⚠️ Could not decode company name, using as-is: {}", companyName);
        }

        logger.info("🎭 Getting fake weekly PDF - companyId: {}, date: {}, companyName: {}", companyId, date, companyName);

        try {
            byte[] pdfData = fakeDataService.generateFakePdf("weekly", companyName, date);

            if (pdfData == null || pdfData.length == 0) {
                logger.error("❌ Generated PDF data is null or empty");
                String errorJson = "{\"error\": \"Failed to generate PDF data.\"}";
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentLength(pdfData.length);
            headers.set("Accept-Ranges", "bytes");
            headers.set("Connection", "close");

            if (download) {
                String fileName = String.format("%s-weekly-report-%s.pdf",
                        companyName.toLowerCase().replaceAll("\\s+", "-"), date);
                headers.setContentDispositionFormData("attachment", fileName);
            } else {
                headers.setContentDispositionFormData("inline", "weekly-report.pdf");
            }

            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            logger.info("✅ Successfully generated fake weekly PDF ({} bytes)", pdfData.length);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfData.length)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfData);
        } catch (Exception e) {
            logger.error("❌ Error generating fake PDF", e);
            String errorMessage = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "Unknown error";
            String errorJson = "{\"error\": \"An error occurred while generating the fake PDF: " + errorMessage + "\"}";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    /**
     * Get fake PDF for testing (daily report)
     */
    @GetMapping("/fake-pdf/daily")
    public ResponseEntity<byte[]> getFakeDailyPdf(
            @RequestParam("company_id") String companyId,
            @RequestParam("date") String date,
            @RequestParam("company_name") String companyName) {

        try {
            // Decode URL-encoded company name
            companyName = java.net.URLDecoder.decode(companyName, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("⚠️ Could not decode company name, using as-is: {}", companyName);
        }

        logger.info("🎭 Getting fake daily PDF - companyId: {}, date: {}, companyName: {}", companyId, date, companyName);

        try {
            byte[] pdfData = fakeDataService.generateFakePdf("daily", companyName, date);

            if (pdfData == null || pdfData.length == 0) {
                logger.error("❌ Generated PDF data is null or empty");
                String errorJson = "{\"error\": \"Failed to generate PDF data.\"}";
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentLength(pdfData.length);
            headers.set("Accept-Ranges", "bytes");
            headers.set("Connection", "close");
            headers.setContentDispositionFormData("inline", "daily-report.pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            logger.info("✅ Successfully generated fake daily PDF ({} bytes)", pdfData.length);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfData.length)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfData);
        } catch (Exception e) {
            logger.error("❌ Error generating fake PDF", e);
            String errorMessage = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "Unknown error";
            String errorJsonStr = "{\"error\": \"An error occurred while generating the fake PDF: " + errorMessage + "\"}";
            byte[] errorJsonBytes = errorJsonStr.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJsonBytes);
        }
    }
}
