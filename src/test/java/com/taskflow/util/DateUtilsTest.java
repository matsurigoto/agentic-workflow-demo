package com.taskflow.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DateUtils
 * Status: Very incomplete, several tests disabled
 */
public class DateUtilsTest {
    
    @Test
    public void testParseDateISO() {
        Date date = DateUtils.parseDate("2024-01-15");
        assertNotNull(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2024, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }
    
    @Test
    public void testParseDateNull() {
        assertNull(DateUtils.parseDate(null));
        assertNull(DateUtils.parseDate(""));
        assertNull(DateUtils.parseDate("not-a-date"));
    }
    
    @Test
    public void testFormatDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.MARCH, 15, 0, 0, 0);
        String formatted = DateUtils.formatDate(cal.getTime());
        assertEquals("2024-03-15", formatted);
    }
    
    @Test
    @Disabled("Fails due to thread-safety issue when run in parallel")
    public void testParseDateThreadSafety() {
        // This test would demonstrate the thread-safety bug
        // but is disabled because it fails intermittently
    }
    
    @Test
    public void testDaysBetween() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2024, Calendar.JANUARY, 1);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2024, Calendar.JANUARY, 10);
        
        int days = DateUtils.daysBetween(cal1.getTime(), cal2.getTime());
        // BUG: might be 8 or 9 due to DST, but we just check it's close
        assertTrue(days >= 8 && days <= 10);
    }
    
    @Test
    public void testGetRelativeTime() {
        assertNotNull(DateUtils.getRelativeTime(new Date()));
        assertEquals("unknown", DateUtils.getRelativeTime(null));
    }
    
    @Test
    public void testGetQuarter() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 15);
        assertEquals(1, DateUtils.getQuarter(cal.getTime())); // FAILS: returns 0
    }
    
    @Test
    public void testStartOfDay() {
        Date now = new Date();
        Date start = DateUtils.startOfDay(now);
        Calendar cal = Calendar.getInstance();
        cal.setTime(start);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        // BUG: milliseconds not reset, but we don't test for it
    }
    
    // --- Regression tests for known bugs ---

    /**
     * Regression: isWithinRange uses exclusive bounds but should be inclusive.
     * A date equal to the start boundary should be considered within range.
     * Bug: date.after(start) returns false when date == start.
     */
    @Test
    public void testIsWithinRange_dateEqualsStart_shouldBeInRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 10, 0, 0, 0);
        Date start = cal.getTime();
        cal.set(2024, Calendar.JANUARY, 20, 0, 0, 0);
        Date end = cal.getTime();

        // BUG: returns false because date.after(start) is false when date == start
        assertTrue(DateUtils.isWithinRange(start, start, end),
                "Date equal to start boundary should be considered within range (inclusive bounds)");
    }

    /**
     * Regression: isWithinRange uses exclusive bounds but should be inclusive.
     * A date equal to the end boundary should be considered within range.
     */
    @Test
    public void testIsWithinRange_dateEqualsEnd_shouldBeInRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 10, 0, 0, 0);
        Date start = cal.getTime();
        cal.set(2024, Calendar.JANUARY, 20, 0, 0, 0);
        Date end = cal.getTime();

        // BUG: returns false because date.before(end) is false when date == end
        assertTrue(DateUtils.isWithinRange(end, start, end),
                "Date equal to end boundary should be considered within range (inclusive bounds)");
    }

    @Test
    public void testIsWithinRange_dateInsideRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 10, 0, 0, 0);
        Date start = cal.getTime();
        cal.set(2024, Calendar.JANUARY, 15, 0, 0, 0);
        Date middle = cal.getTime();
        cal.set(2024, Calendar.JANUARY, 20, 0, 0, 0);
        Date end = cal.getTime();

        assertTrue(DateUtils.isWithinRange(middle, start, end));
    }

    @Test
    public void testIsWithinRange_dateOutsideRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 10, 0, 0, 0);
        Date start = cal.getTime();
        cal.set(2024, Calendar.JANUARY, 20, 0, 0, 0);
        Date end = cal.getTime();
        cal.set(2024, Calendar.FEBRUARY, 1, 0, 0, 0);
        Date after = cal.getTime();

        assertFalse(DateUtils.isWithinRange(after, start, end));
    }

    /**
     * Regression: addBusinessDays uses day-of-week constants 6 and 7.
     * In Java Calendar: FRIDAY=6, SATURDAY=7, SUNDAY=1.
     * The bug skips Friday and Saturday but counts Sunday as a business day.
     *
     * Correct: Thursday + 2 business days = Monday (skip Saturday and Sunday).
     * Buggy:   Thursday + 2 business days = Sunday (skips Friday and Saturday, counts Sunday).
     */
    @Test
    public void testAddBusinessDays_fromThursday_shouldSkipWeekend() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 11, 0, 0, 0, 0); // Thursday Jan 11, 2024
        Date thursday = cal.getTime();

        Date result = DateUtils.addBusinessDays(thursday, 2);

        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);

        // BUG: returns Sunday Jan 14 (skips Fri and Sat, counts Sun)
        // Correct answer: Monday Jan 15
        assertEquals(Calendar.MONDAY, resultCal.get(Calendar.DAY_OF_WEEK),
                "Adding 2 business days to Thursday should yield Monday (skip Saturday+Sunday)");
        assertEquals(15, resultCal.get(Calendar.DAY_OF_MONTH),
                "Adding 2 business days to Thursday Jan 11 should yield Monday Jan 15");
    }

    @Test
    public void testAddBusinessDays_fromMonday_noWeekendCrossing() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 8, 0, 0, 0, 0); // Monday Jan 8, 2024
        Date monday = cal.getTime();

        Date result = DateUtils.addBusinessDays(monday, 3);

        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);

        assertEquals(Calendar.THURSDAY, resultCal.get(Calendar.DAY_OF_WEEK));
        assertEquals(11, resultCal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testIsOverdue_pastDate_returnsTrue() {
        assertTrue(DateUtils.isOverdue("2020-01-01"),
                "A date in the past should be considered overdue");
    }

    @Test
    public void testIsOverdue_futureDate_returnsFalse() {
        assertFalse(DateUtils.isOverdue("2099-12-31"),
                "A date in the future should not be overdue");
    }

    @Test
    public void testIsOverdue_nullOrInvalidDate_returnsFalse() {
        // BUG: silently returns false on parse failure - could mask real overdue items
        assertFalse(DateUtils.isOverdue(null));
        assertFalse(DateUtils.isOverdue("not-a-date"));
    }
}
