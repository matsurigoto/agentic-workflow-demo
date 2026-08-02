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
    
    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
    }
    
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
        assertEquals(0, created.status); // defaults to TODO
        assertNotNull(created.createdDate);
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
        task.priority = 1;
        task.type = "task";

        assertThrows(RuntimeException.class, () -> taskService.createTask(task));
    }
    
    @Test
    public void testGetTask() {
        Task task = new Task();
        task.title = "Findable Task";
        task.priority = 1;
        task.type = "task";
        Task created = taskService.createTask(task);

        Task found = taskService.getTask(created.getId());
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Findable Task", found.getTitle());
    }
    
    @Test
    public void testGetNonExistentTask() {
        Task task = taskService.getTask(99999L);
        assertNull(task);
    }
    
    @Test
    public void testGetAllTasks() {
        Task t1 = new Task();
        t1.title = "Task A";
        t1.priority = 1;
        t1.type = "task";
        taskService.createTask(t1);

        Task t2 = new Task();
        t2.title = "Task B";
        t2.priority = 2;
        t2.type = "bug";
        taskService.createTask(t2);

        List<Task> tasks = taskService.getAllTasks();
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
    }
    
    @Test
    public void testGetAllTasksEmpty() {
        List<Task> tasks = taskService.getAllTasks();
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }
    
    @Test
    public void testGetTaskStatistics() {
        Task task = new Task();
        task.title = "Stats Task";
        task.priority = 2;
        task.type = "task";
        taskService.createTask(task);

        Map<String, Object> stats = taskService.getTaskStatistics();
        assertNotNull(stats);
        assertTrue(stats.containsKey("total"));
    }
    
    @Test
    public void testSearchTasks() {
        Task task = new Task();
        task.title = "Searchable Task";
        task.priority = 1;
        task.type = "task";
        taskService.createTask(task);

        List<Map<String, Object>> results = taskService.searchTasks("Searchable");
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    public void testSearchTasksNoMatch() {
        Task task = new Task();
        task.title = "Unique Title XYZ";
        task.priority = 1;
        task.type = "task";
        taskService.createTask(task);

        List<Map<String, Object>> results = taskService.searchTasks("NoMatchABC");
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
    
    @Test
    public void testUpdateTask() {
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
    @Disabled("Depends on user data - enable once User fixtures are set up")
    public void testAssignTask() {
        Task task = new Task();
        task.title = "Assign Me";
        task.priority = 2;
        task.type = "task";
        Task created = taskService.createTask(task);
        
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
        
        // TODO -> DONE is not a valid transition
        assertThrows(RuntimeException.class, () -> taskService.transitionStatus(created.getId(), 2));
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
        Task task = new Task();
        task.title = "Export Me";
        task.priority = 1;
        task.type = "task";
        taskService.createTask(task);

        String csv = taskService.exportTasks(null);
        assertNotNull(csv);
        assertTrue(csv.contains("ID,Title"));
        assertTrue(csv.contains("Export Me"));
    }
    
    @Test
    public void testGetTasksByStatus() {
        Task todo = new Task();
        todo.title = "TODO Task";
        todo.priority = 1;
        todo.type = "task";
        todo.status = 0;
        taskService.createTask(todo);

        Task inProgress = new Task();
        inProgress.title = "In Progress Task";
        inProgress.priority = 2;
        inProgress.type = "task";
        inProgress.status = 0;
        Task created = taskService.createTask(inProgress);
        taskService.transitionStatus(created.getId(), 1);

        List<Task> todoTasks = taskService.getTasksByStatus(0);
        assertNotNull(todoTasks);
        assertEquals(1, todoTasks.size());
        assertEquals("TODO Task", todoTasks.get(0).getTitle());

        List<Task> inProgressTasks = taskService.getTasksByStatus(1);
        assertNotNull(inProgressTasks);
        assertEquals(1, inProgressTasks.size());

        List<Task> doneTasks = taskService.getTasksByStatus(2);
        assertNotNull(doneTasks);
        assertTrue(doneTasks.isEmpty());
    }
    
    @Test
    public void testGetTasksByAssignee() {
        // No assignee - unassigned tasks should not appear for assignee lookups
        Task task = new Task();
        task.title = "Unassigned Task";
        task.priority = 1;
        task.type = "task";
        taskService.createTask(task);

        List<Task> assignedTo1 = taskService.getTasksByAssignee(1L);
        assertNotNull(assignedTo1);
        assertTrue(assignedTo1.isEmpty());

        List<Task> assignedTo999 = taskService.getTasksByAssignee(999L);
        assertNotNull(assignedTo999);
        assertTrue(assignedTo999.isEmpty());
    }
    
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
