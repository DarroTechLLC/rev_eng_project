package com.darro_tech.revengproject.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darro_tech.revengproject.models.DailyReportCompany;
import com.darro_tech.revengproject.repositories.DailyReportCompanyRepository;

@Service
public class DailyReportService {

    private static final Logger logger = LoggerFactory.getLogger(DailyReportService.class);

    @Autowired
    private DailyReportCompanyRepository dailyReportRepository;

    public String getPdfData(String companyId, LocalDate date) {
        logger.info("📊 Getting PDF data for company {} on date {}", companyId, date);

        try {
            // Convert LocalDate to Instant for the start of the day
            Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant startOfNextDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            // Find reports for the given date range
            List<DailyReportCompany> reports = dailyReportRepository.findByCompanyIdAndDateBetween(
                    companyId, startOfDay, startOfNextDay);

            if (reports.isEmpty()) {
                logger.info("❌ No reports found for company {} on date {}", companyId, date);
                return "No daily report is available.";
            }

            // Get the first report's PDF data
            DailyReportCompany report = reports.get(0);
            byte[] pdfData = report.getPdf();

            if (pdfData == null || pdfData.length == 0) {
                logger.warn("❌ PDF data is null or empty for report ID: {}", report.getId());
                return "No daily report is available.";
            }

            // Convert PDF data to base64
            String base64Pdf = Base64.getEncoder().encodeToString(pdfData);
            logger.info("✅ Successfully retrieved PDF data for company {} on date {}", companyId, date);

            return base64Pdf;
        } catch (Exception e) {
            logger.error("❌ Error getting PDF data", e);
            return "No daily report is available.";
        }
    }
}
