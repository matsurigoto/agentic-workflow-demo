package com.taskflow.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Project model utility methods.
 *
 * Documents known bugs in getMemberIds() and getProgress():
 *   - getMemberIds(): NPE when members is null, NumberFormatException on empty/whitespace parts
 *   - getProgress(): ArithmeticException (division by zero) when taskCount is 0
 */
class ProjectTest {

    // --- getProgress() ---

    @Test
    void getProgress_returnsCorrectPercentage() {
        Project project = new Project("Alpha", "ALPHA");
        project.setTaskCount(10);
        project.setCompletedTaskCount(4);
        assertEquals(40.0, project.getProgress(), 0.001);
    }

    @Test
    void getProgress_allComplete_returns100() {
        Project project = new Project("Alpha", "ALPHA");
        project.setTaskCount(5);
        project.setCompletedTaskCount(5);
        assertEquals(100.0, project.getProgress(), 0.001);
    }

    @Test
    void getProgress_noneComplete_returns0() {
        Project project = new Project("Alpha", "ALPHA");
        project.setTaskCount(8);
        project.setCompletedTaskCount(0);
        assertEquals(0.0, project.getProgress(), 0.001);
    }

    @Test
    @Disabled("BUG: getProgress() divides by zero when taskCount is 0 - see FIXME in Project.java")
    void getProgress_noTasks_shouldReturn0NotDivideByZero() {
        // A project with no tasks should report 0% progress (or 100%), not throw
        Project project = new Project("Alpha", "ALPHA");
        // taskCount defaults to 0 via constructor
        // This throws ArithmeticException (or returns NaN/Infinity for doubles):
        double progress = project.getProgress();
        assertTrue(progress == 0.0 || progress == 100.0,
                "Expected 0 or 100 for a project with no tasks, got: " + progress);
    }

    // --- getMemberIds() ---

    @Test
    void getMemberIds_parsesCommaSeparatedIds() {
        Project project = new Project("Beta", "BETA");
        project.setMembers("1,5,12,23");
        List<Long> ids = project.getMemberIds();
        assertEquals(List.of(1L, 5L, 12L, 23L), ids);
    }

    @Test
    void getMemberIds_singleMember() {
        Project project = new Project("Beta", "BETA");
        project.setMembers("42");
        List<Long> ids = project.getMemberIds();
        assertEquals(List.of(42L), ids);
    }

    @Test
    @Disabled("BUG: getMemberIds() throws NullPointerException when members field is null")
    void getMemberIds_nullMembers_shouldReturnEmptyList() {
        // A project with no members string should return an empty list, not throw NPE
        Project project = new Project("Beta", "BETA");
        // members defaults to null
        List<Long> ids = project.getMemberIds();
        assertTrue(ids.isEmpty(), "Expected empty list for null members, got: " + ids);
    }

    @Test
    @Disabled("BUG: getMemberIds() throws NumberFormatException when members is empty string")
    void getMemberIds_emptyString_shouldReturnEmptyList() {
        Project project = new Project("Beta", "BETA");
        project.setMembers("");
        // split("") produces [""] which then fails Long.parseLong("")
        List<Long> ids = project.getMemberIds();
        assertTrue(ids.isEmpty(), "Expected empty list for empty members string, got: " + ids);
    }

    @Test
    @Disabled("BUG: getMemberIds() throws NumberFormatException when member IDs contain whitespace")
    void getMemberIds_whitespaceAroundIds_shouldParseTrimmed() {
        Project project = new Project("Beta", "BETA");
        project.setMembers("1, 5, 12");
        // Long.parseLong(" 5") throws NumberFormatException (no trimming)
        List<Long> ids = project.getMemberIds();
        assertEquals(List.of(1L, 5L, 12L), ids);
    }

    // --- constructor / defaults ---

    @Test
    void constructor_setsDefaultStatus0() {
        Project project = new Project("Gamma", "GAMMA");
        assertEquals(0, project.getStatus());
    }

    @Test
    void constructor_setsTaskCountersTo0() {
        Project project = new Project("Gamma", "GAMMA");
        assertEquals(0, project.getTaskCount());
        assertEquals(0, project.getCompletedTaskCount());
    }
}
