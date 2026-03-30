package com.taskflow.service;

import com.taskflow.model.Task;
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
 * Status: MOSTLY BROKEN
 * Last updated: 2021-08-15
 * 
 * Some tests are disabled because "they were failing and blocking the build"
 * Some tests have no assertions (just exist to inflate coverage numbers)
 * Some tests depend on execution order (fragile)
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
    
    // NOTE: No @BeforeEach to clean up data - tests interfere with each other
    // DataInitializer also runs, adding extra data
    
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
        // Missing: assert status, priority, createdDate, etc.
    }
    
    @Test
    public void testCreateTaskWithoutTitle() {
        Task task = new Task();
        task.description = "No title";
        
        // Should throw exception
        assertThrows(RuntimeException.class, () -> {
            taskService.createTask(task);
        });
    }
    
    @Test
    public void testGetTask() {
        // Depends on DataInitializer having run - fragile
        Task task = taskService.getTask(1L);
        // Sometimes passes, sometimes fails depending on test order
        assertNotNull(task);
    }
    
    @Test
    public void testGetNonExistentTask() {
        Task task = taskService.getTask(99999L);
        assertNull(task); // Returns null instead of throwing - debatable design
    }
    
    @Test
    @Disabled("Disabled because it fails intermittently - TASK-567")
    public void testGetOverdueTasks() {
        List<Task> overdue = taskService.getOverdueTasks();
        // This test depends on the current date and sample data
        assertNotNull(overdue);
        assertTrue(overdue.size() > 0); // Fragile: depends on DataInitializer data
    }
    
    @Test
    public void testGetAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        assertNotNull(tasks);
        // No assertion on content - "just making sure it doesn't crash"
    }
    
    @Test
    public void testGetTaskStatistics() {
        Map<String, Object> stats = taskService.getTaskStatistics();
        assertNotNull(stats);
        // FIXME: This test fails when no tasks are completed (division by zero)
    }
    
    @Test
    public void testSearchTasks() {
        // SECURITY: This test doesn't verify SQL injection protection
        // because there IS no SQL injection protection
        List<Map<String, Object>> results = taskService.searchTasks("test");
        assertNotNull(results);
    }
    
    @Test
    public void testUpdateTask() {
        // Create a task first
        Task task = new Task();
        task.title = "Original Title";
        task.priority = 1;
        task.type = "task";
        Task created = taskService.createTask(task);
        
        // Update it
        Task update = new Task();
        update.title = "Updated Title";
        update.priority = 3;
        
        Task updated = taskService.updateTask(created.getId(), update);
        assertEquals("Updated Title", updated.getTitle());
        // BUG: Doesn't check if priority was actually updated
    }
    
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
    @Disabled("Flaky test - depends on user data from DataInitializer")
    public void testAssignTask() {
        Task task = new Task();
        task.title = "Assign Me";
        task.priority = 2;
        task.type = "task";
        Task created = taskService.createTask(task);
        
        // Assumes user with ID 1 exists (from DataInitializer)
        Task assigned = taskService.assignTask(created.getId(), 1L);
        assertEquals(1L, (long) assigned.assignee_id);
    }
    
    @Test
    public void testTransitionStatus() {
        Task task = new Task();
        task.title = "Status Test";
        task.priority = 2;
        task.type = "task";
        task.status = 0; // TODO
        Task created = taskService.createTask(task);
        
        // TODO -> IN_PROGRESS should work
        Task inProgress = taskService.transitionStatus(created.getId(), 1);
        assertEquals(1, inProgress.getStatus());
    }
    
    @Test
    public void testInvalidTransition() {
        Task task = new Task();
        task.title = "Invalid Transition";
        task.priority = 2;
        task.type = "task";
        task.status = 0; // TODO
        Task created = taskService.createTask(task);
        
        // TODO -> DONE should fail (based on current transition rules)
        assertThrows(RuntimeException.class, () -> {
            taskService.transitionStatus(created.getId(), 2); // DONE
        });
    }
    
    @Test
    @Disabled("CSV import test - disabled because it's slow and flaky")
    public void testImportTasks() {
        String csv = "title,description,priority,type\n" +
                      "Import Task 1,Desc 1,2,task\n" +
                      "Import Task 2,Desc 2,3,bug\n";
        
        List<Task> imported = taskService.importTasks(csv);
        assertEquals(2, imported.size());
    }
    
    @Test
    public void testExportTasks() {
        String csv = taskService.exportTasks(null);
        assertNotNull(csv);
        assertTrue(csv.contains("ID,Title")); // Header check only
    }
    
    // This test has NO assertions - it was added to inflate coverage numbers
    @Test
    public void testGetTasksByStatus() {
        taskService.getTasksByStatus(0);
        taskService.getTasksByStatus(1);
        taskService.getTasksByStatus(2);
        // Look, 100% line coverage! But zero actual verification.
    }
    
    // This test has NO assertions either
    @Test
    public void testGetTasksByAssignee() {
        taskService.getTasksByAssignee(1L);
        taskService.getTasksByAssignee(999L);
    }
    
    // Empty test that was "going to be implemented later"
    @Test
    @Disabled("TODO: implement")
    public void testAutoAssignTask() {
        // TODO
    }
    
    @Test
    @Disabled("TODO: implement")
    public void testBulkImportWithInvalidData() {
        // TODO: test with malformed CSV
    }
    
    @Test
    @Disabled("TODO: implement")
    public void testConcurrentTaskCreation() {
        // TODO: test thread safety
    }
}
