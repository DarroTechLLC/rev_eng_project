package com.darro_tech.revengproject.services;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.darro_tech.revengproject.models.EmailLogView;
import com.darro_tech.revengproject.models.UserSubscriptionView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for loading and managing fake/test data for development and testing
 */
@Service
public class FakeDataService {

    private static final Logger log = LoggerFactory.getLogger(FakeDataService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Load fake email logs from JSON file
     *
     * @return list of fake email log views
     */
    public List<EmailLogView> loadFakeEmailLogs() {
        log.info("📧 Loading fake email logs from JSON file");
        try {
            ClassPathResource resource = new ClassPathResource("data/fake-email-logs.json");
            InputStream inputStream = resource.getInputStream();
            JsonNode rootNode = objectMapper.readTree(inputStream);

            List<EmailLogView> emailLogs = new ArrayList<>();
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    // Use reflection to create instance (protected constructor)
                    EmailLogView emailLog = createEmailLogViewInstance();

                    // Use reflection to set fields (since EmailLogView has protected constructor)
                    setField(emailLog, "id", node.has("id") ? node.get("id").asText() : null);
                    setField(emailLog, "mailFrom", node.has("mailFrom") ? node.get("mailFrom").asText() : null);
                    setField(emailLog, "mailTo", node.has("mailTo") ? node.get("mailTo").asText() : null);
                    setField(emailLog, "subject", node.has("subject") && !node.get("subject").isNull() ? node.get("subject").asText() : null);
                    setField(emailLog, "attachedFiles", node.has("attachedFiles") && !node.get("attachedFiles").isNull() ? node.get("attachedFiles").asText() : null);
                    setField(emailLog, "mailType", node.has("mailType") && !node.get("mailType").isNull() ? node.get("mailType").asText() : null);
                    setField(emailLog, "companyId", node.has("companyId") && !node.get("companyId").isNull() ? node.get("companyId").asText() : null);
                    setField(emailLog, "userId", node.has("userId") && !node.get("userId").isNull() ? node.get("userId").asText() : null);
                    setField(emailLog, "status", node.has("status") ? node.get("status").asText() : "pending");
                    setField(emailLog, "messageId", node.has("messageId") && !node.get("messageId").isNull() ? node.get("messageId").asText() : null);
                    setField(emailLog, "errorMessage", node.has("errorMessage") && !node.get("errorMessage").isNull() ? node.get("errorMessage").asText() : null);

                    if (node.has("sentAt") && !node.get("sentAt").isNull()) {
                        setField(emailLog, "sentAt", Instant.parse(node.get("sentAt").asText()));
                    }
                    if (node.has("createdAt") && !node.get("createdAt").isNull()) {
                        setField(emailLog, "createdAt", Instant.parse(node.get("createdAt").asText()));
                    }
                    if (node.has("updatedAt") && !node.get("updatedAt").isNull()) {
                        setField(emailLog, "updatedAt", Instant.parse(node.get("updatedAt").asText()));
                    }

                    setField(emailLog, "companyName", node.has("companyName") && !node.get("companyName").isNull() ? node.get("companyName").asText() : null);
                    setField(emailLog, "companyDisplayName", node.has("companyDisplayName") && !node.get("companyDisplayName").isNull() ? node.get("companyDisplayName").asText() : null);
                    setField(emailLog, "username", node.has("username") && !node.get("username").isNull() ? node.get("username").asText() : null);
                    setField(emailLog, "firstName", node.has("firstName") && !node.get("firstName").isNull() ? node.get("firstName").asText() : null);
                    setField(emailLog, "lastName", node.has("lastName") && !node.get("lastName").isNull() ? node.get("lastName").asText() : null);

                    emailLogs.add(emailLog);
                }
            }
            log.info("✅ Loaded {} fake email logs", emailLogs.size());
            return emailLogs;
        } catch (IOException e) {
            log.error("❌ Error loading fake email logs", e);
            return new ArrayList<>();
        }
    }

    /**
     * Load fake user subscriptions from JSON file
     *
     * @return list of fake user subscription views
     */
    public List<UserSubscriptionView> loadFakeUserSubscriptions() {
        log.info("📋 Loading fake user subscriptions from JSON file");
        try {
            ClassPathResource resource = new ClassPathResource("data/fake-user-subscriptions.json");
            InputStream inputStream = resource.getInputStream();
            JsonNode rootNode = objectMapper.readTree(inputStream);

            List<UserSubscriptionView> subscriptions = new ArrayList<>();
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    // Use reflection to create instance (protected constructor)
                    UserSubscriptionView subscription = createUserSubscriptionViewInstance();

                    // Use reflection to set fields
                    setField(subscription, "userId", node.has("userId") ? node.get("userId").asText() : null);
                    setField(subscription, "companyId", node.has("companyId") ? node.get("companyId").asText() : null);
                    setField(subscription, "subscriptionKey", node.has("subscriptionKey") ? node.get("subscriptionKey").asText() : null);

                    if (node.has("createdAt") && !node.get("createdAt").isNull()) {
                        setField(subscription, "createdAt", Instant.parse(node.get("createdAt").asText()));
                    }
                    if (node.has("updatedAt") && !node.get("updatedAt").isNull()) {
                        setField(subscription, "updatedAt", Instant.parse(node.get("updatedAt").asText()));
                    }

                    setField(subscription, "username", node.has("username") && !node.get("username").isNull() ? node.get("username").asText() : null);
                    setField(subscription, "firstName", node.has("firstName") ? node.get("firstName").asText() : null);
                    setField(subscription, "lastName", node.has("lastName") ? node.get("lastName").asText() : null);
                    setField(subscription, "companyName", node.has("companyName") ? node.get("companyName").asText() : null);
                    setField(subscription, "companyDisplayName", node.has("companyDisplayName") ? node.get("companyDisplayName").asText() : null);

                    subscriptions.add(subscription);
                }
            }
            log.info("✅ Loaded {} fake user subscriptions", subscriptions.size());
            return subscriptions;
        } catch (IOException e) {
            log.error("❌ Error loading fake user subscriptions", e);
            return new ArrayList<>();
        }
    }

    /**
     * Helper method to create EmailLogView instance using reflection
     */
    private EmailLogView createEmailLogViewInstance() {
        try {
            java.lang.reflect.Constructor<EmailLogView> constructor = EmailLogView.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            log.error("❌ Error creating EmailLogView instance", e);
            throw new RuntimeException("Failed to create EmailLogView instance", e);
        }
    }

    /**
     * Helper method to create UserSubscriptionView instance using reflection
     */
    private UserSubscriptionView createUserSubscriptionViewInstance() {
        try {
            java.lang.reflect.Constructor<UserSubscriptionView> constructor = UserSubscriptionView.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            log.error("❌ Error creating UserSubscriptionView instance", e);
            throw new RuntimeException("Failed to create UserSubscriptionView instance", e);
        }
    }

    /**
     * Generate fake PDF data for testing (simple test PDF)
     *
     * @param reportType "weekly" or "daily"
     * @param companyName name of the company
     * @param reportDate date of the report
     * @return byte array containing fake PDF data
     */
    public byte[] generateFakePdf(String reportType, String companyName, String reportDate) {
        log.info("🎭 Generating fake PDF for {} report - company: {}, date: {}", reportType, companyName, reportDate);

        // Handle null values
        if (reportType == null) {
            reportType = "test";
        }
        if (companyName == null) {
            companyName = "Unknown Company";
        }
        if (reportDate == null) {
            reportDate = "Unknown Date";
        }

        java.io.ByteArrayOutputStream outputStream = null;
        com.itextpdf.text.Document document = null;

        try {
            // Create a simple PDF using iText
            outputStream = new java.io.ByteArrayOutputStream();
            document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
            com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream);

            document.open();

            // Add title
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            String titleText = String.format("%s Report - %s",
                    reportType.length() > 0
                    ? reportType.substring(0, 1).toUpperCase() + (reportType.length() > 1 ? reportType.substring(1) : "")
                    : "Test",
                    companyName);
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(titleText, titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add date
            com.itextpdf.text.Font dateFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 12);
            String dateText = String.format("Report Date: %s", reportDate);
            com.itextpdf.text.Paragraph date = new com.itextpdf.text.Paragraph(dateText, dateFont);
            date.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            date.setSpacingAfter(20);
            document.add(date);

            // Add test content
            com.itextpdf.text.Font contentFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            com.itextpdf.text.Paragraph content = new com.itextpdf.text.Paragraph(
                    "This is a test PDF generated for development and testing purposes.\n\n"
                    + "This fake data allows you to test the report viewer functionality without requiring actual report data in the database.\n\n"
                    + "To disable fake data, click the 'Test Fake Data' toggle button.", contentFont);
            content.setSpacingAfter(20);
            document.add(content);

            // Add note
            com.itextpdf.text.Font noteFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.ITALIC);
            com.itextpdf.text.Paragraph note = new com.itextpdf.text.Paragraph(
                    "Note: This is fake test data. Real reports will contain actual production data.", noteFont);
            note.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(note);

            document.close();
            document = null; // Prevent double close

            byte[] pdfBytes = outputStream.toByteArray();

            if (pdfBytes == null || pdfBytes.length == 0) {
                log.warn("⚠️ Generated PDF is empty, using fallback");
                return getMinimalPdfBytes();
            }

            log.info("✅ Generated fake PDF ({} bytes)", pdfBytes.length);
            return pdfBytes;

        } catch (Exception e) {
            log.error("❌ Error generating fake PDF: {}", e.getMessage(), e);

            // Ensure document is closed if it was opened
            if (document != null && document.isOpen()) {
                try {
                    document.close();
                } catch (Exception closeEx) {
                    log.warn("⚠️ Error closing document: {}", closeEx.getMessage());
                }
            }

            // Return minimal PDF bytes if generation fails
            return getMinimalPdfBytes();
        }
    }

    /**
     * Generate minimal valid PDF bytes (fallback)
     */
    private byte[] getMinimalPdfBytes() {
        // Minimal valid PDF (just a blank page)
        try {
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new com.itextpdf.text.Paragraph("Test PDF"));
            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("❌ Error generating minimal PDF fallback", e);
            // Return a very basic PDF structure as last resort
            return "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f \n0000000009 00000 n \n0000000058 00000 n \n0000000115 00000 n \ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n178\n%%EOF".getBytes();
        }
    }

    /**
     * Helper method to set field using reflection
     */
    private void setField(Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            log.warn("⚠️ Could not set field {}: {}", fieldName, e.getMessage());
        }
    }
}
