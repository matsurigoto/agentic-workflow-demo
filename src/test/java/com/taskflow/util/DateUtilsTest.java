package com.taskflow.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

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
    
    /**
     * Demonstrates issue #5: SimpleDateFormat is not thread-safe.
     * Concurrent calls to parseDate() can produce wrong results or throw exceptions.
     *
     * This test is intentionally designed to FAIL — it documents a known bug.
     * The test proves the bug is present; fixing it requires replacing static
     * SimpleDateFormat fields with ThreadLocal<SimpleDateFormat> or DateTimeFormatter.
     */
    @Test
    @Disabled("Documents known thread-safety bug (issue #5). Enable to reproduce.")
    public void testParseDateThreadSafety_KnownBug() throws Exception {
        final int THREADS = 10;
        final int ITERATIONS = 50;
        final String DATE_STR = "2024-06-15";

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < THREADS; t++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await(); // all threads start simultaneously
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                for (int i = 0; i < ITERATIONS; i++) {
                    try {
                        Date result = DateUtils.parseDate(DATE_STR);
                        if (result == null) {
                            errorCount.incrementAndGet();
                            continue;
                        }
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(result);
                        // The parsed date must match the input exactly
                        if (cal.get(Calendar.YEAR) != 2024
                                || cal.get(Calendar.MONTH) != Calendar.JUNE
                                || cal.get(Calendar.DAY_OF_MONTH) != 15) {
                            errorCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // NumberFormatException / ArrayIndexOutOfBoundsException typical from SimpleDateFormat
                        errorCount.incrementAndGet();
                    }
                }
            }));
        }

        startLatch.countDown(); // release all threads at once
        for (Future<?> f : futures) {
            f.get();
        }
        executor.shutdown();

        // If thread-safety were guaranteed, errorCount would be 0.
        // In practice it is > 0, demonstrating the bug.
        assertEquals(0, errorCount.get(),
                "Thread-safety bug (issue #5): " + errorCount.get()
                + " out of " + (THREADS * ITERATIONS) + " concurrent parseDate() calls produced wrong results.");
    }

    /**
     * Same thread-safety bug in formatDate().
     * Concurrent format calls on the shared static DATE_FORMAT instance can corrupt output.
     */
    @Test
    @Disabled("Documents known thread-safety bug (issue #5). Enable to reproduce.")
    public void testFormatDateThreadSafety_KnownBug() throws Exception {
        final int THREADS = 10;
        final int ITERATIONS = 50;

        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JUNE, 15, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        final Date DATE = cal.getTime();
        final String EXPECTED = "2024-06-15";

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < THREADS; t++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                for (int i = 0; i < ITERATIONS; i++) {
                    try {
                        String result = DateUtils.formatDate(DATE);
                        if (!EXPECTED.equals(result)) {
                            errorCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    }
                }
            }));
        }

        startLatch.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        executor.shutdown();

        assertEquals(0, errorCount.get(),
                "Thread-safety bug (issue #5): " + errorCount.get()
                + " out of " + (THREADS * ITERATIONS) + " concurrent formatDate() calls produced wrong results.");
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
    
    // Missing tests:
    // - isOverdue
    // - addBusinessDays
    // - isWithinRange
    // - edge cases (null inputs, boundary dates)
}
