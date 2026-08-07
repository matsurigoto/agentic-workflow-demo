package com.taskflow.service;

import com.taskflow.model.Task;
import com.taskflow.model.User;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.repository.ProjectRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TaskService
 *
 * Improved 2026-08: added @BeforeEach cleanup to prevent DataInitializer
 * pollution, added assertions to previously assertion-free tests, fixed
 * fragile ID-dependent tests.
 */
@SpringBootTest
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    /** Clean slate before every test — prevents DataInitializer data from leaking in. */
    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
    }

    // -------------------------------------------------------------------------
    // createTask
    // -------------------------------------------------------------------------

    @Test
    public void testCreateTask() {
        Task task = new Task();
        task.title = "Test Task";
        task.description = "Test Description";
        task.priority = 2;
        task.type = "task";

        Task created = taskService.createTask(task);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Test Task", created.getTitle());
        assertEquals(2, created.priority);
        assertEquals(0, created.status); // defaults to STATUS_TODO
        assertNotNull(created.createdDate);
    }

    @Test
    public void testCreateTask_defaultPriorityWhenZero() {
        // priority < 1 should default to 2 (medium)
        Task task = new Task();
        task.title = "Low Priority Input";
        task.priority = 0;
        task.type = "task";

        Task created = taskService.createTask(task);

        assertEquals(2, created.priority, "Priority < 1 should default to medium (2)");
    }

    @Test
    public void testCreateTaskWithoutTitle() {
        Task task = new Task();
        task.description = "No title";

        assertThrows(RuntimeException.class, () -> taskService.createTask(task));
    }

    @Test
    public void testCreateTaskWithBlankTitle() {
        Task task = new Task();
        task.title = "   ";
        task.type = "task";

        assertThrows(RuntimeException.class, () -> taskService.createTask(task));
    }

    // -------------------------------------------------------------------------
    // getTask
    // -------------------------------------------------------------------------

    @Test
    public void testGetTask_returnsCreatedTask() {
        Task task = new Task();
        task.title = "Find Me";
        task.priority = 1;
        task.type = "task";
        Task created = taskService.createTask(task);

        Task found = taskService.getTask(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Find Me", found.getTitle());
    }

    @Test
    public void testGetNonExistentTask() {
        Task task = taskService.getTask(99999L);
        assertNull(task);
    }

    // -------------------------------------------------------------------------
    // updateTask
    // -------------------------------------------------------------------------

    @Test
    public void testUpdateTask_titleAndPriority() {
        Task task = new Task();
        task.title = "Original Title";
        task.priority = 1;
        task.type = "task";
        Task created = taskService.createTask(task);

        Task update = new Task();
        update.title = "Updated Title";
        update.priority = 3;

        Task updated = taskService.updateTask(created.getId(), update);

        assertEquals("Updated Title", updated.getTitle());
        assertEquals(3, updated.priority);
    }

    @Test
    public void testUpdateTask_nonExistent_throws() {
        Task update = new Task();
        update.title = "Ghost Update";

        assertThrows(RuntimeException.class, () -> taskService.updateTask(99999L, update));
    }

    // -------------------------------------------------------------------------
    // deleteTask
    // -------------------------------------------------------------------------

    @Test
    public void testDeleteTask() {
        Task task = new Task();
        task.title = "To Be Deleted";
        task.priority = 1;
        task.type = "task";
        Task created = taskService.createTask(task);

        taskService.deleteTask(created.getId());

        assertNull(taskService.getTask(created.getId()));
    }

    @Test
    public void testDeleteTask_nonExistent_throws() {
        assertThrows(RuntimeException.class, () -> taskService.deleteTask(99999L));
    }

    // -------------------------------------------------------------------------
    // getAllTasks / getTasksByStatus / getTasksByAssignee
    // -------------------------------------------------------------------------

    @Test
    public void testGetAllTasks_returnsOnlyCreatedTasks() {
        // After @BeforeEach cleanup, DB is empty
        assertEquals(0, taskService.getAllTasks().size());

        Task t1 = new Task(); t1.title = "A"; t1.priority = 1; t1.type = "task";
        Task t2 = new Task(); t2.title = "B"; t2.priority = 2; t2.type = "bug";
        taskService.createTask(t1);
        taskService.createTask(t2);

        assertEquals(2, taskService.getAllTasks().size());
    }

    @Test
    public void testGetTasksByStatus_returnsMatchingTasks() {
        Task todo = new Task(); todo.title = "Todo Task"; todo.priority = 1; todo.type = "task"; todo.status = 0;
        Task inProg = new Task(); inProg.title = "InProg Task"; inProg.priority = 1; inProg.type = "task"; inProg.status = 0;
        Task created1 = taskService.createTask(todo);
        Task created2 = taskService.createTask(inProg);

        // Transition created2 to IN_PROGRESS (0 -> 1)
        taskService.transitionStatus(created2.getId(), 1);

        List<Task> todoTasks = taskService.getTasksByStatus(0);
        List<Task> inProgressTasks = taskService.getTasksByStatus(1);
        List<Task> doneTasks = taskService.getTasksByStatus(2);

        assertEquals(1, todoTasks.size());
        assertEquals("Todo Task", todoTasks.get(0).getTitle());
        assertEquals(1, inProgressTasks.size());
        assertEquals("InProg Task", inProgressTasks.get(0).getTitle());
        assertEquals(0, doneTasks.size());
    }

    @Test
    public void testGetTasksByAssignee_returnsOnlyAssigneeTasks() {
        User user = new User("testuser", "testuser@example.com", "password");
        user = userRepository.save(user);
        final Long userId = user.getId();

        Task assigned = new Task(); assigned.title = "Assigned"; assigned.priority = 2; assigned.type = "task";
        Task unassigned = new Task(); unassigned.title = "Unassigned"; unassigned.priority = 2; unassigned.type = "task";
        Task a = taskService.createTask(assigned);
        taskService.createTask(unassigned);
        taskService.assignTask(a.getId(), userId);

        List<Task> result = taskService.getTasksByAssignee(userId);
        assertEquals(1, result.size());
        assertEquals("Assigned", result.get(0).getTitle());

        List<Task> noneResult = taskService.getTasksByAssignee(99999L);
        assertEquals(0, noneResult.size());
    }

    // -------------------------------------------------------------------------
    // transitionStatus
    // -------------------------------------------------------------------------

    @Test
    public void testTransitionStatus_todoToInProgress() {
        Task task = new Task(); task.title = "Status Test"; task.priority = 2; task.type = "task"; task.status = 0;
        Task created = taskService.createTask(task);

        Task inProgress = taskService.transitionStatus(created.getId(), 1);

        assertEquals(1, inProgress.getStatus());
    }

    @Test
    public void testTransitionStatus_todoToInProgress_thenToReview_thenToDone() {
        Task task = new Task(); task.title = "Full Workflow"; task.priority = 2; task.type = "task"; task.status = 0;
        Task created = taskService.createTask(task);

        taskService.transitionStatus(created.getId(), 1); // TODO -> IN_PROGRESS
        taskService.transitionStatus(created.getId(), 5); // IN_PROGRESS -> REVIEW
        Task done = taskService.transitionStatus(created.getId(), 2); // REVIEW -> DONE

        assertEquals(2, done.getStatus());
    }

    @Test
    public void testTransitionStatus_invalidTransition_throws() {
        Task task = new Task(); task.title = "Invalid Transition"; task.priority = 2; task.type = "task"; task.status = 0;
        Task created = taskService.createTask(task);

        // TODO -> DONE is an invalid direct jump
        assertThrows(RuntimeException.class, () -> taskService.transitionStatus(created.getId(), 2));
    }

    @Test
    public void testTransitionStatus_cancelledTaskCannotTransition() {
        Task task = new Task(); task.title = "Cancel Me"; task.priority = 1; task.type = "task"; task.status = 0;
        Task created = taskService.createTask(task);
        taskService.transitionStatus(created.getId(), 3); // TODO -> CANCELLED

        // Cancelled tasks cannot be further transitioned
        assertThrows(RuntimeException.class,
                () -> taskService.transitionStatus(created.getId(), 0));
    }

    // -------------------------------------------------------------------------
    // exportTasks
    // -------------------------------------------------------------------------

    @Test
    public void testExportTasks_includesHeaderAndData() {
        Task task = new Task(); task.title = "Export Me"; task.priority = 2; task.type = "task";
        taskService.createTask(task);

        String csv = taskService.exportTasks(null);

        assertNotNull(csv);
        assertTrue(csv.contains("ID,Title"), "CSV should start with header");
        assertTrue(csv.contains("Export Me"), "CSV should contain the task title");
    }

    @Test
    public void testExportTasks_emptyRepository_returnsHeaderOnly() {
        String csv = taskService.exportTasks(null);

        assertNotNull(csv);
        assertTrue(csv.contains("ID,Title"));
        // Only the header line plus the trailing newline
        assertEquals(1, csv.lines().filter(line -> !line.isBlank()).count());
    }

    // -------------------------------------------------------------------------
    // importTasks
    // -------------------------------------------------------------------------

    @Test
    public void testImportTasks_validCsv_importsTasks() {
        String csv = "title,description,priority,type\n"
                   + "Import Task 1,Desc 1,2,task\n"
                   + "Import Task 2,Desc 2,3,bug\n";

        List<Task> imported = taskService.importTasks(csv);

        assertEquals(2, imported.size());
        assertEquals(0, imported.get(0).status, "Imported tasks should start as TODO (0)");
    }

    @Test
    public void testImportTasks_malformedLine_skipsAndContinues() {
        // Line 2 has no fields (just commas), line 3 is valid
        String csv = "title,description,priority,type\n"
                   + ",,not-a-number,task\n"      // parseInt will fail — skipped
                   + "Valid Task,Desc,1,task\n";

        List<Task> imported = taskService.importTasks(csv);

        assertEquals(1, imported.size());
        assertEquals("Valid Task", imported.get(0).getTitle());
    }

    // -------------------------------------------------------------------------
    // getOverdueTasks
    // -------------------------------------------------------------------------

    @Test
    public void testGetOverdueTasks_noTasks_returnsEmpty() {
        List<Task> overdue = taskService.getOverdueTasks();
        assertNotNull(overdue);
        assertEquals(0, overdue.size());
    }

    @Test
    public void testGetOverdueTasks_pastDueDate_included() {
        Task overTask = new Task();
        overTask.title = "Past Due";
        overTask.priority = 2;
        overTask.type = "task";
        overTask.due_date = "2020-01-01"; // well in the past
        taskService.createTask(overTask);

        List<Task> overdue = taskService.getOverdueTasks();

        assertEquals(1, overdue.size());
        assertEquals("Past Due", overdue.get(0).getTitle());
    }

    @Test
    public void testGetOverdueTasks_completedTask_notIncluded() {
        Task task = new Task();
        task.title = "Done Task";
        task.priority = 2;
        task.type = "task";
        task.due_date = "2020-01-01";
        Task created = taskService.createTask(task);
        taskService.transitionStatus(created.getId(), 1); // TODO -> IN_PROGRESS
        taskService.transitionStatus(created.getId(), 5); // -> REVIEW
        taskService.transitionStatus(created.getId(), 2); // -> DONE

        List<Task> overdue = taskService.getOverdueTasks();

        assertEquals(0, overdue.size(), "Completed tasks should not appear as overdue");
    }

    // -------------------------------------------------------------------------
    // getTaskStatistics — known bug: ArithmeticException when completedCount==0
    // -------------------------------------------------------------------------

    @Test
    @Disabled("Known bug: getTaskStatistics throws ArithmeticException (/ by zero) when no tasks are completed. See FIXME in TaskService.getTaskStatistics().")
    public void testGetTaskStatistics_emptyDb_knownDivisionByZeroBug() {
        // This documents the division-by-zero bug at line ~481 of TaskService.
        // Remove @Disabled once the bug is fixed.
        Map<String, Object> stats = taskService.getTaskStatistics();
        assertNotNull(stats);
    }

    @Test
    public void testGetTaskStatistics_withCompletedTask_returnsCorrectCounts() {
        Task todo = new Task(); todo.title = "Todo"; todo.priority = 1; todo.type = "task";
        Task bug = new Task(); bug.title = "Bug"; bug.priority = 3; bug.type = "bug";
        Task created1 = taskService.createTask(todo);
        Task created2 = taskService.createTask(bug);

        // Complete created2 via valid transitions
        taskService.transitionStatus(created2.getId(), 1);
        taskService.transitionStatus(created2.getId(), 5);
        taskService.transitionStatus(created2.getId(), 2);

        Map<String, Object> stats = taskService.getTaskStatistics();

        assertEquals(2, (int)(long)(Long) stats.get("total"));
        assertEquals(1L, stats.get("todo"));
        assertEquals(1L, stats.get("done"));
        assertEquals(1L, stats.get("bugs"));
        assertEquals(1L, stats.get("tasks"));
    }
}
