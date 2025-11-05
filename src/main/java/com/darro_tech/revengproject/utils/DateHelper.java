package com.darro_tech.revengproject.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateHelper {

    /**
     * Returns the start of the week (Monday) for the given date
     *
     * @param date the date to find the start of week for
     * @return the start of the week (Monday) as a LocalDate
     */
    public static LocalDate getStartOfWeek(LocalDate date) {
        return getStartOfWeek(date, 1); // 1 = Monday
    }

    /**
     * Returns the start of the week for the given date
     *
     * @param date the date to find the start of week for
     * @param weekStartsOn the day the week starts on (0=Sunday, 1=Monday, ...,
     * 6=Saturday)
     * @return the start of the week as a LocalDate
     */
    public static LocalDate getStartOfWeek(LocalDate date, int weekStartsOn) {
        if (date == null) {
            date = LocalDate.now();
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int dayValue = dayOfWeek.getValue(); // Monday = 1, Sunday = 7

        // Convert weekStartsOn to DayOfWeek value (0=Sunday becomes 7)
        int startDayValue = (weekStartsOn == 0) ? 7 : weekStartsOn;

        // Calculate days to subtract
        int daysToSubtract = (dayValue - startDayValue + 7) % 7;

        return date.minusDays(daysToSubtract);
    }

    /**
     * Format date as friendly string (e.g., "Monday, January 15, 2025")
     */
    public static String toFriendlyDateString(LocalDate date) {
        if (date == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        return date.format(formatter);
    }

    /**
     * Format date as YYYY-MM-DD string
     */
    public static String toYyyyMmDdString(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
