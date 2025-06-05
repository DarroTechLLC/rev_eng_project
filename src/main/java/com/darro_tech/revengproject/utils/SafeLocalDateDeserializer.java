package com.darro_tech.revengproject.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Custom deserializer for LocalDate that handles malformed date strings.
 * Attempts to parse the date string in various formats and falls back to a default date if parsing fails.
 */
public class SafeLocalDateDeserializer extends JsonDeserializer<LocalDate> {

    private static final Logger logger = LoggerFactory.getLogger(SafeLocalDateDeserializer.class);

    // List of date formats to try
    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
        DateTimeFormatter.ISO_LOCAL_DATE,                // yyyy-MM-dd
        DateTimeFormatter.ofPattern("yyyy-M-d"),         // yyyy-M-d
        DateTimeFormatter.ofPattern("yy-MM-dd"),         // yy-MM-dd
        DateTimeFormatter.ofPattern("yy-M-d"),           // yy-M-d
        DateTimeFormatter.ofPattern("y-MM-dd"),          // y-MM-dd
        DateTimeFormatter.ofPattern("y-M-d")             // y-M-d
    );

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText();

        if (dateStr == null || dateStr.isEmpty()) {
            logger.warn("Empty date string received, using current date");
            return LocalDate.now();
        }

        // Check for specific patterns that need fixing first
        if (dateStr.matches("^\\d{1,5}-\\d{1,2}-\\d{1,2}$") || dateStr.contains("NaN")) {
            logger.info("Detected pattern that needs fixing: {}", dateStr);
            String fixedDateStr = fixDateString(dateStr);
            if (!fixedDateStr.equals(dateStr)) {
                logger.info("Fixed date string from {} to {}", dateStr, fixedDateStr);
                try {
                    return LocalDate.parse(fixedDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (DateTimeParseException e) {
                    logger.error("Failed to parse fixed date string: {}", fixedDateStr, e);
                    // Continue to try other formatters
                }
            }
        }

        // Try to parse with each formatter
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Continue to the next formatter
            }
        }

        // If we get here, none of the formatters worked
        logger.error("Failed to parse date string: {}", dateStr);

        // Try to fix common issues with the date string as a last resort
        String fixedDateStr = fixDateString(dateStr);
        if (!fixedDateStr.equals(dateStr)) {
            logger.info("Attempting to parse fixed date string as last resort: {}", fixedDateStr);
            try {
                return LocalDate.parse(fixedDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                logger.error("Failed to parse fixed date string as last resort: {}", fixedDateStr, e);
                // Fall back to current date
            }
        }

        // Fall back to current date
        logger.warn("Using current date as fallback for unparseable date: {}", dateStr);
        return LocalDate.now();
    }

    /**
     * Attempts to fix common issues with date strings.
     * 
     * @param dateStr The date string to fix
     * @return The fixed date string
     */
    private String fixDateString(String dateStr) {
        // Handle strings like "2-10-01", "20-10-01", "202-10-01", "20001-02-01"
        if (dateStr.matches("^\\d{1,5}-\\d{1,2}-\\d{1,2}$")) {
            String[] parts = dateStr.split("-");
            String year = parts[0];
            String month = parts[1];
            String day = parts[2];

            // Try to parse the year first to see if it's a valid year
            try {
                int yearInt = Integer.parseInt(year);

                // Fix year format based on number of digits
                if (year.length() == 1) {
                    year = "200" + year;  // Assume 200x for single digit
                    logger.info("Padded single-digit year {} to {}", parts[0], year);
                } else if (year.length() == 2) {
                    year = "20" + year;   // Assume 20xx for double digit
                    logger.info("Padded double-digit year {} to {}", parts[0], year);
                } else if (year.length() == 3) {
                    year = "2" + year;    // Assume 2xxx for triple digit
                    logger.info("Padded triple-digit year {} to {}", parts[0], year);
                } else if (year.length() == 5) {
                    // For 5-digit years like 20001, remove the extra digit
                    year = year.substring(0, 4);
                    logger.info("Fixed 5-digit year {} to {}", parts[0], year);
                }
            } catch (NumberFormatException e) {
                logger.error("Failed to parse year: {}", year, e);
                return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            }

            // Pad month and day to 2 digits if needed
            month = month.length() == 1 ? "0" + month : month;
            day = day.length() == 1 ? "0" + day : day;

            String fixedDateStr = year + "-" + month + "-" + day;
            logger.info("Fixed date string from {} to {}", dateStr, fixedDateStr);
            return fixedDateStr;
        }

        // Handle strings like "NaN-NaN-01"
        if (dateStr.contains("NaN")) {
            logger.info("Date string contains NaN, using current date");
            return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        return dateStr;
    }
}
