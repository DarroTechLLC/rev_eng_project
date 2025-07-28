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

import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.models.DailyReportCompany;
import com.darro_tech.revengproject.repositories.DailyReportCompanyRepository;

@RestController
@RequestMapping("/api/reports")
public class ReportApiController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ReportApiController.class);

    @Autowired
    private DailyReportCompanyRepository dailyReportRepository;

    @GetMapping("/daily-pdf/{companyName}")
    public ResponseEntity<?> getDailyPdf(
            @PathVariable String companyName,
            @RequestParam("company_id") String companyId,
            @RequestParam("date") String date) {

        logger.info("📊 Processing daily PDF request - companyName: {}, companyId: {}, date: {}",
                companyName, companyId, date);

        try {
            // Parse the date string to Instant
            LocalDate localDate = LocalDate.parse(date);
            Instant startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant startOfNextDay = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            // Find reports for the given date range
            List<DailyReportCompany> reports = dailyReportRepository.findByCompanyIdAndDateBetween(
                    companyId, startOfDay, startOfNextDay);

            if (reports.isEmpty()) {
                logger.info("❌ No reports found for company {} on date {}", companyId, date);
                return ResponseEntity.notFound().build();
            }

            // Get the first report's PDF data
            DailyReportCompany report = reports.get(0);
            byte[] pdfData = report.getPdf();

            if (pdfData == null || pdfData.length == 0) {
                logger.warn("❌ PDF data is null or empty for report ID: {}", report.getId());
                return ResponseEntity.notFound().build();
            }

            // Set headers for PDF response
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "daily-report.pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            logger.info("✅ Successfully retrieved PDF data for company {} on date {}", companyId, date);
            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("❌ Error getting PDF data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
