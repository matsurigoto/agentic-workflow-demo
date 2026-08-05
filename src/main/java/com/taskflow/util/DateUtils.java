package com.taskflow.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Date utility class
 */
public class DateUtils {

    // DateTimeFormatter is immutable and thread-safe
    private static final DateTimeFormatter DATE_FORMAT     = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter US_FORMAT       = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter EU_FORMAT       = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Parse date string - tries multiple formats. Returns null if unparseable.
     */
    public static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;

        try {
            return localDateToDate(LocalDate.parse(dateStr, DATE_FORMAT));
        } catch (DateTimeParseException e) { /* try next */ }

        try {
            return localDateToDate(LocalDate.parse(dateStr, US_FORMAT));
        } catch (DateTimeParseException e) { /* try next */ }

        try {
            return localDateToDate(LocalDate.parse(dateStr, EU_FORMAT));
        } catch (DateTimeParseException e) { /* try next */ }

        try {
            LocalDateTime ldt = LocalDateTime.parse(dateStr, DATETIME_FORMAT);
            return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException e) { /* give up */ }

        return null;
    }

    /**
     * Format date to string
     */
    public static String formatDate(Date date) {
        if (date == null) return "";
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(DATE_FORMAT);
    }

    private static Date localDateToDate(LocalDate ld) {
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Calculate days between two dates
     * BUG: doesn't account for DST transitions, can be off by 1
     */
    public static int daysBetween(Date start, Date end) {
        long diff = end.getTime() - start.getTime(); // NPE if either is null
        return (int) (diff / (1000 * 60 * 60 * 24)); // integer truncation issue
    }
    
    /**
     * Check if a task is overdue
     */
    public static boolean isOverdue(String dueDateStr) {
        Date dueDate = parseDate(dueDateStr);
        if (dueDate == null) return false; // BUG: if we can't parse, we assume not overdue
        return new Date().after(dueDate);
    }
    
    /**
     * Add business days to date (skip weekends)
     * BUG: doesn't account for holidays
     */
    public static Date addBusinessDays(Date startDate, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        
        int addedDays = 0;
        while (addedDays < days) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            // BUG: Calendar.SATURDAY is 7, SUNDAY is 1 in Java
            // but this code checks for 6 and 7 (Saturday and... what?)
            if (cal.get(Calendar.DAY_OF_WEEK) != 6 && cal.get(Calendar.DAY_OF_WEEK) != 7) {
                addedDays++;
            }
        }
        return cal.getTime();
    }
    
    /**
     * Get a human-readable relative time string
     * e.g., "2 hours ago", "3 days ago"
     */
    public static String getRelativeTime(Date date) {
        if (date == null) return "unknown";
        
        long diff = new Date().getTime() - date.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        // FIXME: doesn't handle future dates (returns negative values)
        if (days > 365) return (days / 365) + " years ago";
        if (days > 30) return (days / 30) + " months ago";
        if (days > 0) return days + " days ago";
        if (hours > 0) return hours + " hours ago";
        if (minutes > 0) return minutes + " minutes ago";
        return seconds + " seconds ago";
    }
    
    /**
     * Get quarter from date (1-4)
     */
    public static int getQuarter(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH); // 0-based
        return (month / 3) + 1; // returns 1-4
    }
    
    /**
     * Check if date is within range
     */
    public static boolean isWithinRange(Date date, Date start, Date end) {
        // BUG: doesn't handle equal dates correctly (should be inclusive)
        return date.after(start) && date.before(end);
    }
    
    /**
     * Get start of day
     */
    public static Date startOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
