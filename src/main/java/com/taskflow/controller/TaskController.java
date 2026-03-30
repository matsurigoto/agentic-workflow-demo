package com.taskflow.controller;

import com.taskflow.model.Task;
import com.taskflow.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Task REST Controller
 * 
 * FIXME: No input validation
 * FIXME: No authentication/authorization
 * FIXME: Inconsistent response format
 * FIXME: No pagination
 * FIXME: Mixing REST conventions (some return entity, some return Map)
 */
@RestController
@RequestMapping("/api")  // FIXME: some endpoints use /api/tasks, some use /api/v1/tasks
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    /**
     * Get all tasks - no pagination, returns everything
     */
    @GetMapping("/tasks")
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }
    
    /**
     * Get task by ID
     * BUG: Returns 200 with null body when task not found (should be 404)
     */
    @GetMapping("/tasks/{id}")
    public Task getTask(@PathVariable Long id) {
        return taskService.getTask(id); // Returns null if not found
    }
    
    /**
     * Create task
     * No input validation, no proper error response
     */
    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        try {
            Task created = taskService.createTask(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            // Returning error as plain text instead of proper error response
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Update task
     */
    @PutMapping("/tasks/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Task task) {
        try {
            Task updated = taskService.updateTask(id, task);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Delete task - no authorization check
     * Anyone can delete any task
     */
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            // Inconsistent: some endpoints return body, this returns empty
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            // BUG: Returns 400 for "not found" errors (should be 404)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Search tasks
     * SECURITY: SQL injection via keyword parameter
     */
    @GetMapping("/tasks/search")
    public List<Map<String, Object>> searchTasks(@RequestParam String keyword) {
        return taskService.searchTasks(keyword); // SQL injection
    }
    
    /**
     * Get tasks by status
     * FIXME: Status is magic number, should accept string
     */
    @GetMapping("/tasks/status/{status}")
    public List<Task> getTasksByStatus(@PathVariable int status) {
        return taskService.getTasksByStatus(status);
    }
    
    /**
     * Get tasks by assignee
     */
    @GetMapping("/tasks/assignee/{userId}")
    public List<Task> getTasksByAssignee(@PathVariable Long userId) {
        return taskService.getTasksByAssignee(userId);
    }
    
    /**
     * Get overdue tasks
     */
    @GetMapping("/tasks/overdue")
    public List<Task> getOverdueTasks() {
        return taskService.getOverdueTasks();
    }
    
    /**
     * Assign task to user
     * Inconsistent URL pattern - uses query param instead of path
     */
    @PostMapping("/tasks/{taskId}/assign")
    public ResponseEntity<?> assignTask(@PathVariable Long taskId, @RequestParam Long userId) {
        try {
            Task task = taskService.assignTask(taskId, userId);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Transition task status
     */
    @PostMapping("/tasks/{taskId}/transition")
    public ResponseEntity<?> transitionStatus(@PathVariable Long taskId, @RequestParam int status) {
        try {
            Task task = taskService.transitionStatus(taskId, status);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Get dashboard statistics
     * BUG: Division by zero when no completed tasks exist
     */
    @GetMapping("/stats")
    public Map<String, Object> getStatistics() {
        return taskService.getTaskStatistics();
    }
    
    /**
     * Get project statistics
     * Different URL pattern from above (inconsistent API design)
     */
    @GetMapping("/projects/{projectCode}/stats")
    public Map<String, Object> getProjectStats(@PathVariable String projectCode) {
        return taskService.getProjectStatistics(projectCode);
    }
    
    /**
     * Import tasks from CSV
     * SECURITY: No file size limit, no content validation
     */
    @PostMapping("/tasks/import")
    public ResponseEntity<?> importTasks(@RequestBody String csvData) {
        try {
            List<Task> imported = taskService.importTasks(csvData);
            Map<String, Object> result = new HashMap<>();
            result.put("imported", imported.size());
            result.put("tasks", imported);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    
    /**
     * Export tasks to CSV
     */
    @GetMapping("/tasks/export")
    public ResponseEntity<String> exportTasks(@RequestParam(required = false) String projectCode) {
        String csv = taskService.exportTasks(projectCode);
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=tasks.csv")
                .body(csv);
    }
    
    /**
     * Auto-assign task
     */
    @PostMapping("/tasks/{taskId}/auto-assign")
    public ResponseEntity<?> autoAssignTask(@PathVariable Long taskId) {
        try {
            Task task = taskService.autoAssignTask(taskId);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Legacy endpoint - kept for backward compatibility with mobile app v1.x
    @GetMapping("/v1/tasks")
    public Map<String, Object> getTasksLegacy() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", taskService.getAllTasks());
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    // Another legacy endpoint - different response format
    @GetMapping("/v1/tasks/{id}")
    public Map<String, Object> getTaskLegacy(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Task task = taskService.getTask(id);
        if (task != null) {
            response.put("success", true);
            response.put("data", task);
        } else {
            response.put("success", false);
            response.put("error", "Task not found");
        }
        return response;
    }
}
