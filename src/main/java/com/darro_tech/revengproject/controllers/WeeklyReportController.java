package com.darro_tech.revengproject.controllers;

import java.time.LocalDate;

import com.darro_tech.revengproject.services.PdfGenerationService;
import com.itextpdf.text.DocumentException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Placeholder for WeeklyReportController This is a minimal implementation to
 * satisfy the compiler
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class WeeklyReportController {

    private final PdfGenerationService pdfGenerationService;

    @GetMapping("/align/weekly-report/pdf")
    public ResponseEntity<byte[]> downloadWeeklyReportPdf(
            @RequestParam(required = false) String companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws DocumentException {

        log.info("📥 Generating PDF for weekly report - companyId: {}, date: {}", companyId, date);

        // Use current date if not provided
        LocalDate reportDate = date != null ? date : LocalDate.now();

        try {
            // Generate PDF
            byte[] pdfContent = pdfGenerationService.generateWeeklyReportPdf(companyId, reportDate);

            // Set up response headers
            String filename = String.format("weekly-report-%s-%s.pdf",
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
}
