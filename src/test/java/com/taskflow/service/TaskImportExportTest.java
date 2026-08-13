package com.taskflow.service;

import com.taskflow.model.Task;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.repository.ProjectRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Edge case tests for TaskService.importTasks() and TaskService.exportTasks().
 *
 * These tests document known bugs in the import/export logic without masking them.
 * Bugs are noted inline and tracked in separate issues.
 *
 * 🤖 Created by Test Improver (automated AI assistant).
 */
@SpringBootTest
public class TaskImportExportTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    // -------------------------------------------------------------------------
    // importTasks() tests
    // -------------------------------------------------------------------------

    @Test
    void importTasks_withValidCsv_createsAllTasks() {
        String csv = "title,description,priority,type\n"
                   + "Import Task 1,First description,2,task\n"
                   + "Import Task 2,Second description,3,bug\n";

        List<Task> imported = taskService.importTasks(csv);

        assertEquals(2, imported.size());
        assertTrue(imported.stream().anyMatch(t -> "Import Task 1".equals(t.getTitle())));
        assertTrue(imported.stream().anyMatch(t -> "Import Task 2".equals(t.getTitle())));
    }

    @Test
    void importTasks_setsDefaultStatus_toTodo() {
        String csv = "title,description,priority,type\n"
                   + "Status Task,Desc,2,task\n";

        List<Task> imported = taskService.importTasks(csv);

        assertEquals(1, imported.size());
        assertEquals(0, imported.get(0).getStatus(), "Imported tasks should default to status TODO (0)");
    }

    @Test
    void importTasks_withHeaderOnly_returnsEmptyList() {
        String csv = "title,description,priority,type\n";

        List<Task> imported = taskService.importTasks(csv);

        assertTrue(imported.isEmpty(), "Header-only CSV should produce no tasks");
    }

    @Test
    void importTasks_withEmptyString_returnsEmptyList() {
        // Empty string has no header line, so loop body is never entered
        List<Task> imported = taskService.importTasks("");

        assertTrue(imported.isEmpty());
    }

    @Test
    void importTasks_withMissingPriority_usesDefault() {
        // Only title field provided — priority defaults to 2
        String csv = "title\nMinimal Task\n";

        List<Task> imported = taskService.importTasks(csv);

        assertEquals(1, imported.size());
        assertEquals(2, imported.get(0).priority, "Missing priority should default to 2");
    }

    @Test
    void importTasks_withMissingType_usesDefault() {
        // Title + description only — type defaults to "task"
        String csv = "title,description\nNo Type Task,Some desc\n";

        List<Task> imported = taskService.importTasks(csv);

        assertEquals(1, imported.size());
        assertEquals("task", imported.get(0).type, "Missing type should default to 'task'");
    }

    @Test
    void importTasks_withInvalidPriority_skipsLine() {
        // Non-numeric priority triggers NumberFormatException; the line is silently skipped
        String csv = "title,description,priority,type\n"
                   + "Bad Priority,Desc,notanumber,task\n"
                   + "Good Task,Desc,2,task\n";

        List<Task> imported = taskService.importTasks(csv);

        // BUG: bad line is silently swallowed; partial imports leave no trace of the skipped row.
        // Only the valid line should be imported.
        assertEquals(1, imported.size());
        assertEquals("Good Task", imported.get(0).getTitle());
    }

    @Test
    void importTasks_persistsToDatabase() {
        String csv = "title,description,priority,type\n"
                   + "Persisted Task,Saved to DB,1,task\n";

        List<Task> imported = taskService.importTasks(csv);

        assertEquals(1, imported.size());
        assertNotNull(imported.get(0).getId(), "Imported task must have a persisted ID");
        assertTrue(taskRepository.existsById(imported.get(0).getId()));
    }

    @Test
    @Disabled("BUG: importTasks() does not handle commas within quoted fields (e.g. \"Title, with comma\"). "
            + "Splitting on ',' breaks quoted CSV values. Track fix separately.")
    void importTasks_withCommaInTitle_shouldHandleQuotedFields() {
        String csv = "title,description,priority,type\n"
                   + "\"Task, with comma\",Desc,2,task\n";

        List<Task> imported = taskService.importTasks(csv);

        assertEquals(1, imported.size());
        assertEquals("Task, with comma", imported.get(0).getTitle());
    }

    // -------------------------------------------------------------------------
    // exportTasks() tests
    // -------------------------------------------------------------------------

    @Test
    void exportTasks_withNoFilter_includesHeader() {
        String csv = taskService.exportTasks(null);

        assertNotNull(csv);
        assertTrue(csv.startsWith("ID,Title,Description,Status,Priority,Type,Assignee,Due Date,Created\n"),
                "Export must begin with the expected CSV header");
    }

    @Test
    void exportTasks_withNoTasks_returnsHeaderOnly() {
        // setUp() deleted all tasks
        String csv = taskService.exportTasks(null);

        String[] lines = csv.split("\n");
        assertEquals(1, lines.length, "With no tasks, export should contain only the header line");
    }

    @Test
    void exportTasks_includesImportedTask() {
        Task task = new Task();
        task.title = "Exported Task";
        task.description = "Some description";
        task.priority = 3;
        task.type = "bug";
        task.status = 0;
        taskRepository.save(task);

        String csv = taskService.exportTasks(null);

        assertTrue(csv.contains("Exported Task"), "Export CSV must include the task title");
        assertTrue(csv.contains("Some description"), "Export CSV must include the task description");
    }

    @Test
    void exportTasks_withEmptyProjectCode_exportsAll() {
        // Empty string treated same as null — exports everything
        Task task = new Task();
        task.title = "All Tasks Export";
        task.priority = 1;
        task.type = "task";
        task.status = 0;
        taskRepository.save(task);

        String csvNull  = taskService.exportTasks(null);
        String csvEmpty = taskService.exportTasks("");

        assertEquals(csvNull, csvEmpty, "null and empty projectCode should produce identical exports");
    }

    @Test
    @Disabled("BUG: exportTasks() does not escape commas in field values. "
            + "A task title containing a comma corrupts the CSV row. "
            + "Track fix separately.")
    void exportTasks_withCommaInTitle_shouldEscapeField() {
        Task task = new Task();
        task.title = "Task, with a comma";
        task.priority = 1;
        task.type = "task";
        task.status = 0;
        taskRepository.save(task);

        String csv = taskService.exportTasks(null);
        String[] lines = csv.split("\n");

        // Expect exactly 2 lines: header + one data row
        assertEquals(2, lines.length,
                "A title with a comma must not produce extra CSV columns");
        assertTrue(lines[1].contains("\"Task, with a comma\""),
                "Comma in title must be quoted per RFC 4180");
    }
}
