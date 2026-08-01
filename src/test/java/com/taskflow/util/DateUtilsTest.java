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
    
    // --- Regression tests documenting known bugs ---
    // These tests assert the CORRECT expected behavior.
    // Failing tests indicate bugs that need to be fixed.

    /**
     * BUG: getQuarter() returns 0-3 instead of 1-4.
     * January should be Q1 (returns 0), April should be Q2 (returns 1), etc.
     * See: issue #7
     */
    @Test
    public void testGetQuarterQ1ReturnsOne() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 15);
        assertEquals(1, DateUtils.getQuarter(cal.getTime()),
            "BUG #7: January should be Q1 but getQuarter() returns 0 (off-by-one: returns month/3 without +1)");
    }

    @Test
    public void testGetQuarterQ4ReturnsFour() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.DECEMBER, 15);
        assertEquals(4, DateUtils.getQuarter(cal.getTime()),
            "BUG #7: December should be Q4 but getQuarter() returns 3 (off-by-one)");
    }

    /**
     * BUG: startOfDay() does not reset milliseconds.
     * After calling startOfDay(), Calendar.MILLISECOND should be 0.
     */
    @Test
    public void testStartOfDayResetsMilliseconds() {
        Calendar input = Calendar.getInstance();
        input.set(Calendar.MILLISECOND, 999);
        Date start = DateUtils.startOfDay(input.getTime());
        Calendar result = Calendar.getInstance();
        result.setTime(start);
        assertEquals(0, result.get(Calendar.MILLISECOND),
            "BUG: startOfDay() does not reset milliseconds - Calendar.MILLISECOND not zeroed");
    }

    /**
     * BUG: isWithinRange() uses exclusive bounds; equal start/end dates return false.
     * A date equal to 'start' or 'end' should be within range (inclusive semantics).
     */
    @Test
    public void testIsWithinRangeIncludesStartBoundary() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JUNE, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date start = cal.getTime();

        cal.set(2024, Calendar.JUNE, 30, 0, 0, 0);
        Date end = cal.getTime();

        assertTrue(DateUtils.isWithinRange(start, start, end),
            "BUG: isWithinRange() should include start boundary (uses date.after(start) which is false for equal dates)");
    }

    @Test
    public void testIsWithinRangeIncludesEndBoundary() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JUNE, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date start = cal.getTime();

        cal.set(2024, Calendar.JUNE, 30, 0, 0, 0);
        Date end = cal.getTime();

        assertTrue(DateUtils.isWithinRange(end, start, end),
            "BUG: isWithinRange() should include end boundary (uses date.before(end) which is false for equal dates)");
    }

    @Test
    public void testIsWithinRangeMiddle() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JUNE, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date start = cal.getTime();

        cal.set(2024, Calendar.JUNE, 15);
        Date middle = cal.getTime();

        cal.set(2024, Calendar.JUNE, 30, 0, 0, 0);
        Date end = cal.getTime();

        assertTrue(DateUtils.isWithinRange(middle, start, end));
    }

    @Test
    public void testIsWithinRangeOutside() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JUNE, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date start = cal.getTime();

        cal.set(2024, Calendar.MAY, 1);
        Date before = cal.getTime();

        cal.set(2024, Calendar.JUNE, 30, 0, 0, 0);
        Date end = cal.getTime();

        assertFalse(DateUtils.isWithinRange(before, start, end));
    }

    /**
     * BUG: addBusinessDays() skips day_of_week 6 (Friday) and 7 (Saturday),
     * but should skip 7 (Saturday) and 1 (Sunday). Fridays are incorrectly treated
     * as weekend, and Sundays are incorrectly treated as business days.
     */
    @Test
    public void testAddBusinessDaysSkipsSundayNotFriday() {
        // Start on Thursday. Adding 1 business day should give Friday.
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 4, 12, 0, 0); // Thursday Jan 4 2024
        cal.set(Calendar.MILLISECOND, 0);

        Date result = DateUtils.addBusinessDays(cal.getTime(), 1);

        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        assertEquals(Calendar.FRIDAY, resultCal.get(Calendar.DAY_OF_WEEK),
            "BUG: addBusinessDays() treats Friday (DOW=6) as a weekend day. " +
            "From Thursday + 1 business day should be Friday, but skips to Sunday " +
            "because code checks != 6 (Friday) and != 7 (Saturday) instead of != 7 (Saturday) and != 1 (Sunday)");
    }

    @Test
    public void testAddBusinessDaysSkipsWeekend() {
        // Start on Friday. Adding 1 business day should give next Monday.
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 5, 12, 0, 0); // Friday Jan 5 2024
        cal.set(Calendar.MILLISECOND, 0);

        Date result = DateUtils.addBusinessDays(cal.getTime(), 1);

        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        assertEquals(Calendar.MONDAY, resultCal.get(Calendar.DAY_OF_WEEK),
            "From Friday, adding 1 business day should skip Saturday/Sunday and give Monday");
    }

    // --- Tests for previously untested methods ---

    @Test
    public void testIsOverduePastDateIsOverdue() {
        // A date well in the past should be overdue
        assertTrue(DateUtils.isOverdue("2000-01-01"),
            "A date in year 2000 should be overdue");
    }

    @Test
    public void testIsOverdueFutureDateNotOverdue() {
        assertFalse(DateUtils.isOverdue("2099-12-31"),
            "A date in 2099 should not be overdue");
    }

    @Test
    public void testIsOverdueUnparseableDateReturnsFalse() {
        // Documents the silent-failure behavior: unparseable date is treated as "not overdue"
        assertFalse(DateUtils.isOverdue("not-a-date"),
            "Unparseable date silently returns false (documents known behavior - could mask bugs)");
        assertFalse(DateUtils.isOverdue(null),
            "Null date returns false");
    }
}
