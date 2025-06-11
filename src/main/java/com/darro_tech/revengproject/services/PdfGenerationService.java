package com.darro_tech.revengproject.services;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.darro_tech.revengproject.dto.WeeklyReportDTO;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfGenerationService {

    private final WeeklyReportService weeklyReportService;

    @PostConstruct
    public void init() {
        log.info("📑 Initializing PDF Generation Service");
    }

    /**
     * Generates a PDF version of the weekly report
     *
     * @param companyId The ID of the company
     * @param reportDate The date for which to generate the report
     * @return byte array containing the PDF data
     * @throws DocumentException if PDF generation fails
     */
    public byte[] generateWeeklyReportPdf(String companyId, LocalDate reportDate) throws DocumentException {
        log.info("📑 Generating PDF weekly report for company: {} as of date: {}", companyId, reportDate);

        try {
            // Get report data
            WeeklyReportDTO reportData = weeklyReportService.getWeeklyReport(companyId, reportDate);

            // Create PDF document
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);

            // Open document
            document.open();

            // Add content
            addHeader(document, reportData);
            addContent(document, reportData);

            // Close document
            document.close();

            log.info("✅ Successfully generated PDF report");
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("❌ Error generating PDF: {}", e.getMessage(), e);
            throw new DocumentException("Failed to generate PDF report: " + e.getMessage());
        }
    }

    private void addHeader(Document document, WeeklyReportDTO reportData) throws DocumentException {
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

        Paragraph title = new Paragraph("Weekly Production Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph(" ")); // Add some spacing

        Paragraph dateInfo = new Paragraph(
                "Report Date: " + reportData.getReportDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                normalFont
        );
        document.add(dateInfo);

        if (reportData.getCompanyName() != null) {
            Paragraph companyInfo = new Paragraph("Company: " + reportData.getCompanyName(), normalFont);
            document.add(companyInfo);
        }

        document.add(new Paragraph(" ")); // Add some spacing
    }

    private void addContent(Document document, WeeklyReportDTO reportData) throws DocumentException {
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

        // Add production summary
        Paragraph summaryTitle = new Paragraph("Production Summary", sectionFont);
        document.add(summaryTitle);
        document.add(new Paragraph(" "));

        // Add WTD, MTD, YTD totals
        addProductionTotals(document, reportData);

        // Add farm-specific data if available
        if (reportData.getFarmProductions() != null && !reportData.getFarmProductions().isEmpty()) {
            document.add(new Paragraph(" "));
            Paragraph farmTitle = new Paragraph("Farm Production Details", sectionFont);
            document.add(farmTitle);
            document.add(new Paragraph(" "));

            for (var farmProduction : reportData.getFarmProductions()) {
                Paragraph farmInfo = new Paragraph(
                        String.format("%s: %.2f",
                                farmProduction.getFarmName(),
                                farmProduction.getProduction()),
                        normalFont
                );
                document.add(farmInfo);
            }
        }
    }

    private void addProductionTotals(Document document, WeeklyReportDTO reportData) throws DocumentException {
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

        // Add WTD Total
        document.add(new Paragraph(
                String.format("Week to Date Total: %.2f", reportData.getWtdTotal()),
                normalFont
        ));

        // Add MTD Total
        document.add(new Paragraph(
                String.format("Month to Date Total: %.2f", reportData.getMtdTotal()),
                normalFont
        ));

        // Add YTD Total
        document.add(new Paragraph(
                String.format("Year to Date Total: %.2f", reportData.getYtdTotal()),
                normalFont
        ));
    }
}
