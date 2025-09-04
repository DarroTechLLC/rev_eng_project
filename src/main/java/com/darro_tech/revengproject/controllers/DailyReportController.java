package com.darro_tech.revengproject.controllers;

import java.time.LocalDate;
import java.util.Map;

import com.darro_tech.revengproject.services.DailyReportService;
import com.darro_tech.revengproject.services.PdfGenerationService;
import com.itextpdf.text.DocumentException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for daily report endpoints
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class DailyReportController {

    private final PdfGenerationService pdfGenerationService;
    private final DailyReportService dailyReportService;

    /**
     * Download daily report PDF
     * 
     * @param companyId The ID of the company
     * @param date The date for the report
     * @return PDF file as response
     * @throws DocumentException if PDF generation fails
     */
    @GetMapping("/align/daily-report/pdf")
    public ResponseEntity<byte[]> downloadDailyReportPdf(
            @RequestParam(required = false) String companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws DocumentException {

        log.info("📥 Generating PDF for daily report - companyId: {}, date: {}", companyId, date);

        // Use current date if not provided
        LocalDate reportDate = date != null ? date : LocalDate.now();

        try {
            // Generate PDF
            byte[] pdfContent = pdfGenerationService.generateDailyReportPdf(companyId, reportDate);

            // Set up response headers
            String filename = String.format("daily-report-%s-%s.pdf",
                    companyId != null ? companyId : "all",
                    reportDate.toString());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfContent);

        } catch (Exception e) {
            log.error("❌ Error generating PDF: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get daily report PDF timestamp
     * 
     * @param company_id The ID of the company
     * @param date The date for the report
     * @return Timestamp of the latest daily report PDF
     */
    @GetMapping("/api/daily-reports/pdf/timestamp")
    @ResponseBody
    public Map<String, Object> getDailyPdfTimestamp(
            @RequestParam(required = false) String company_id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("🕒 Getting timestamp for daily report - companyId: {}, date: {}", company_id, date);

        // Use current date if not provided
        LocalDate reportDate = date != null ? date : LocalDate.now();

        return Map.of("timestamp", dailyReportService.getLatestTimestamp(company_id, reportDate));
    }

    /**
     * Get daily report PDF
     * 
     * @param companyName The name of the company
     * @param company_id The ID of the company
     * @param date The date for the report
     * @return PDF file as response
     */
    @GetMapping("/api/daily-reports/pdf/{companyName}")
    public ResponseEntity<byte[]> getDailyPdf(
            @PathVariable String companyName,
            @RequestParam(required = false) String company_id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("📊 Getting PDF for daily report - companyName: {}, companyId: {}, date: {}", companyName, company_id, date);

        // Use current date if not provided
        LocalDate reportDate = date != null ? date : LocalDate.now();

        // Get PDF data
        byte[] pdfData = dailyReportService.getPdfData(company_id, reportDate);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }
}
