package com.taskflow.service;

import com.taskflow.model.Task;
import com.taskflow.model.User;
import com.taskflow.model.Project;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.util.DatabaseHelper;
import com.taskflow.util.DateUtils;
import com.taskflow.util.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TaskService - THE GOD CLASS
 * 
 * This class handles:
 * - Task CRUD operations
 * - Task assignment and reassignment
 * - Task status management and workflow
 * - Task search and filtering
 * - Task statistics and reporting
 * - Email notifications (partially)
 * - Task import/export
 * - Overdue task detection
 * - Sprint management (sort of)
 * - Audit logging (broken)
 * 
 * FIXME: This class is over 700 lines and does way too much.
 * Ticket TASK-445: "Break up TaskService" - open since 2020-09
 * 
 * @author kevin (original, 2019)
 * @author jennifer (added notifications, 2020)
 * @author david (added import/export, 2020) 
 * @author intern_mike (added stats, broke everything, 2021)
 * @author contractor_lee (quick fixes, 2022)
 */
@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    // FIXME: Should use NotificationService but it's broken, so we have inline email logic
    // @Autowired
    // private NotificationService notificationService;
    
    // Hardcoded status values - duplicated from Task model
    private static final int STATUS_TODO = 0;
    private static final int STATUS_IN_PROGRESS = 1;
    private static final int STATUS_DONE = 2;
    private static final int STATUS_CANCELLED = 3;
    private static final int STATUS_BLOCKED = 4;
    private static final int STATUS_REVIEW = 5;
    
    // Priority constants - also duplicated
    private static final int PRIORITY_LOW = 1;
    private static final int PRIORITY_MEDIUM = 2;
    private static final int PRIORITY_HIGH = 3;
    private static final int PRIORITY_CRITICAL = 4;
    private static final int PRIORITY_BLOCKER = 5;
    
    /**
     * Create a new task
     * Does too many things: validation, creation, notification, counter update
     */
    public Task createTask(Task task) {
        // "Validation" - incomplete and inconsistent
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Title is required"); // Should be a proper exception type
        }
        
        // No length validation on title
        // No validation on status/priority ranges
        // No validation on assignee existence
        
        if (task.priority < 1) task.priority = 2; // default to medium, magic number
        if (task.status < 0) task.status = 0;
        
        task.createdDate = new Date();
        task.updatedDate = new Date();
        
        // Sanitize - but only title, not description (inconsistent)
        task.title = StringUtils.sanitize(task.getTitle());
        
        Task saved = taskRepository.save(task);
        
        // Update project counters (denormalized, always drifts)
        if (task.projectCode != null) {
            try {
                Project project = projectRepository.findByCode(task.projectCode);
                if (project != null) {
                    project.setTaskCount(project.getTaskCount() + 1);
                    if ("bug".equals(task.type)) {
                        project.setBugCount(project.getBugCount() + 1);
                    }
                    projectRepository.save(project);
                }
            } catch (Exception e) {
                // Swallow exception - "counter update is not critical"
                System.err.println("Failed to update project counter: " + e.getMessage());
            }
        }
        
        // Try to send notification
        try {
            sendTaskNotification(saved, "created");
        } catch (Exception e) {
            // Swallow - notifications are "best effort"
            System.err.println("Notification failed: " + e.getMessage());
        }
        
        // Audit log - broken, prints to stdout
        System.out.println("AUDIT: Task created - id=" + saved.getId() + ", title=" + saved.getTitle());
        
        return saved;
    }
    
    /**
     * Update task - LONG METHOD with many responsibilities
     */
    public Task updateTask(Long id, Task updatedTask) {
        Task existing = taskRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new RuntimeException("Task not found: " + id);
        }
        
        // Track what changed for notification
        boolean statusChanged = false;
        boolean assigneeChanged = false;
        boolean priorityChanged = false;
        int oldStatus = existing.status;
        Long oldAssignee = existing.assignee_id;
        
        // Update fields one by one - no builder, no mapper
        if (updatedTask.title != null) {
            existing.title = StringUtils.sanitize(updatedTask.title);
        }
        if (updatedTask.description != null) {
            existing.description = updatedTask.description; // FIXME: not sanitized!
        }
        if (updatedTask.status != existing.status) {
            // Status transition validation - incomplete
            if (updatedTask.status == STATUS_DONE && existing.status == STATUS_TODO) {
                // Can't go from TODO directly to DONE... or can we?
                // FIXME: PM says this should be allowed, dev says no. Left as-is.
            }
            statusChanged = true;
            existing.status = updatedTask.status;
        }
        if (updatedTask.priority != existing.priority) {
            priorityChanged = true;
            existing.priority = updatedTask.priority;
        }
        if (updatedTask.assignee_id != null && !updatedTask.assignee_id.equals(existing.assignee_id)) {
            assigneeChanged = true;
            existing.assignee_id = updatedTask.assignee_id;
        }
        if (updatedTask.due_date != null) {
            existing.due_date = updatedTask.due_date;
        }
        if (updatedTask.type != null) {
            existing.type = updatedTask.type;
        }
        if (updatedTask.tags != null) {
            existing.tags = updatedTask.tags;
        }
        if (updatedTask.notes != null) {
            existing.notes = updatedTask.notes; // Raw HTML, XSS risk
        }
        if (updatedTask.estimated_hours > 0) {
            existing.estimated_hours = updatedTask.estimated_hours;
        }
        if (updatedTask.actual_hours > 0) {
            existing.actual_hours = updatedTask.actual_hours;
        }
        
        existing.updatedDate = new Date();
        
        Task saved = taskRepository.save(existing);
        
        // Update project counters if status changed
        if (statusChanged && saved.projectCode != null) {
            try {
                Project project = projectRepository.findByCode(saved.projectCode);
                if (project != null) {
                    if (saved.status == STATUS_DONE) {
                        project.setCompletedTaskCount(project.getCompletedTaskCount() + 1);
                    }
                    // BUG: if task was un-completed (moved back from DONE), counter is never decremented
                    projectRepository.save(project);
                }
            } catch (Exception e) {
                System.err.println("Failed to update project counter: " + e.getMessage());
            }
        }
        
        // Notifications - duplicated logic from createTask
        try {
            if (statusChanged) {
                sendTaskNotification(saved, "status_changed");
            }
            if (assigneeChanged) {
                sendTaskNotification(saved, "reassigned");
                // Also notify old assignee
                if (oldAssignee != null) {
                    System.out.println("TODO: notify old assignee " + oldAssignee + " about reassignment");
                }
            }
            if (priorityChanged && saved.priority >= PRIORITY_CRITICAL) {
                sendTaskNotification(saved, "priority_escalated");
            }
        } catch (Exception e) {
            System.err.println("Notification failed: " + e.getMessage());
        }
        
        // Audit
        System.out.println("AUDIT: Task updated - id=" + saved.getId());
        
        return saved;
    }
    
    /**
     * Get task by ID
     */
    public Task getTask(Long id) {
        return taskRepository.findById(id).orElse(null); // Returns null instead of Optional
    }
    
    /**
     * Delete task - no soft delete, no authorization check
     */
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            throw new RuntimeException("Task not found: " + id);
        }
        
        // Decrement project counter
        if (task.projectCode != null) {
            try {
                Project project = projectRepository.findByCode(task.projectCode);
                if (project != null) {
                    project.setTaskCount(project.getTaskCount() - 1);
                    if (task.status == STATUS_DONE) {
                        project.setCompletedTaskCount(project.getCompletedTaskCount() - 1);
                    }
                    // BUG: what if counter goes negative?
                    projectRepository.save(project);
                }
            } catch (Exception e) {
                System.err.println("Failed to update counter: " + e.getMessage());
            }
        }
        
        taskRepository.deleteById(id);
        System.out.println("AUDIT: Task deleted - id=" + id);
    }
    
    /**
     * Get all tasks - NO PAGINATION, loads everything into memory
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll(); // Could be thousands of tasks
    }
    
    /**
     * Search tasks - uses raw JDBC instead of JPA
     * SECURITY: SQL injection vulnerability
     */
    public List<Map<String, Object>> searchTasks(String keyword) {
        try {
            return DatabaseHelper.searchTasks(keyword); // SQL injection
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Get tasks by status
     */
    public List<Task> getTasksByStatus(int status) {
        return taskRepository.findByStatus(status);
    }
    
    /**
     * Get tasks assigned to a user
     */
    public List<Task> getTasksByAssignee(Long userId) {
        return taskRepository.findByAssigneeId(userId);
    }
    
    /**
     * Get tasks for project
     */
    public List<Task> getTasksByProject(String projectCode) {
        return taskRepository.findByProjectCode(projectCode);
    }
    
    /**
     * Get overdue tasks - queries only non-completed tasks with a due date,
     * then filters by date in Java (due_date is stored as a string with mixed formats).
     * Previously loaded ALL tasks into memory before filtering.
     */
    public List<Task> getOverdueTasks() {
        List<Task> candidates = taskRepository.findNonCompletedTasksWithDueDate();
        List<Task> overdue = new ArrayList<>();

        for (Task task : candidates) {
            if (DateUtils.isOverdue(task.due_date)) {
                overdue.add(task);
            }
        }

        // Results are already ordered by priority DESC from the query
        return overdue;
    }
    
    /**
     * Assign task to user
     */
    public Task assignTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            throw new RuntimeException("Task not found");
        }
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        // No check if user is active
        // No check if user has capacity
        // No check for permission
        
        Long oldAssignee = task.assignee_id;
        task.assignee_id = userId;
        task.updatedDate = new Date();
        
        Task saved = taskRepository.save(task);
        
        // Notification - DUPLICATE of updateTask notification logic
        try {
            sendTaskNotification(saved, "assigned");
        } catch (Exception e) {
            System.err.println("Failed to send assignment notification: " + e.getMessage());
        }
        
        return saved;
    }
    
    /**
     * Transition task status with validation
     * FIXME: incomplete state machine, allows invalid transitions
     */
    public Task transitionStatus(Long taskId, int newStatus) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            throw new RuntimeException("Task not found");
        }
        
        // "Validate" transition - many gaps
        int currentStatus = task.status;
        boolean valid = false;
        
        switch (currentStatus) {
            case STATUS_TODO:
                valid = (newStatus == STATUS_IN_PROGRESS || newStatus == STATUS_CANCELLED);
                break;
            case STATUS_IN_PROGRESS:
                valid = (newStatus == STATUS_REVIEW || newStatus == STATUS_BLOCKED || newStatus == STATUS_CANCELLED);
                break;
            case STATUS_REVIEW:
                valid = (newStatus == STATUS_DONE || newStatus == STATUS_IN_PROGRESS);
                break;
            case STATUS_BLOCKED:
                valid = (newStatus == STATUS_IN_PROGRESS || newStatus == STATUS_CANCELLED);
                break;
            case STATUS_DONE:
                valid = false; // Can't transition from DONE... but sometimes PM needs to reopen
                break;
            case STATUS_CANCELLED:
                valid = false; // Can't uncancelled... but sometimes they want to
                break;
            default:
                valid = true; // Unknown status? Sure, allow anything
        }
        
        if (!valid) {
            throw new RuntimeException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
        
        task.status = newStatus;
        task.updatedDate = new Date();
        
        // If done, record completion
        if (newStatus == STATUS_DONE) {
            // FIXME: should calculate actual_hours from time tracking, not just set to estimated
            if (task.actual_hours == 0) {
                task.actual_hours = task.estimated_hours;
            }
        }
        
        return taskRepository.save(task);
    }
    
    /**
     * Get task statistics for dashboard.
     * Uses aggregate JPQL queries instead of loading all tasks into memory.
     * Previously: findAll() (O(n) memory) + 13 stream passes + a second findAll() via getOverdueTasks().
     * Now: 4 aggregate DB queries + 1 filtered query for overdue candidates.
     */
    public Map<String, Object> getTaskStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total", taskRepository.countTotal());

        // Status counts from a single aggregate query
        Map<Integer, Long> byStatus = new HashMap<>();
        for (Object[] row : taskRepository.countByStatus()) {
            byStatus.put((Integer) row[0], (Long) row[1]);
        }
        stats.put("todo",       byStatus.getOrDefault(STATUS_TODO, 0L));
        stats.put("inProgress", byStatus.getOrDefault(STATUS_IN_PROGRESS, 0L));
        stats.put("done",       byStatus.getOrDefault(STATUS_DONE, 0L));
        stats.put("cancelled",  byStatus.getOrDefault(STATUS_CANCELLED, 0L));
        stats.put("blocked",    byStatus.getOrDefault(STATUS_BLOCKED, 0L));
        stats.put("review",     byStatus.getOrDefault(STATUS_REVIEW, 0L));

        // Priority counts from a single aggregate query
        Map<Integer, Long> byPriority = new HashMap<>();
        for (Object[] row : taskRepository.countByPriority()) {
            byPriority.put((Integer) row[0], (Long) row[1]);
        }
        // PRIORITY_CRITICAL includes PRIORITY_BLOCKER (>= 4)
        long critical = byPriority.getOrDefault(PRIORITY_CRITICAL, 0L)
                      + byPriority.getOrDefault(PRIORITY_BLOCKER, 0L);
        stats.put("critical", critical);
        stats.put("high",   byPriority.getOrDefault(PRIORITY_HIGH, 0L));
        stats.put("medium", byPriority.getOrDefault(PRIORITY_MEDIUM, 0L));
        stats.put("low",    byPriority.getOrDefault(PRIORITY_LOW, 0L));

        // Type counts from a single aggregate query
        Map<String, Long> byType = new HashMap<>();
        for (Object[] row : taskRepository.countByType()) {
            if (row[0] != null) byType.put((String) row[0], (Long) row[1]);
        }
        stats.put("bugs",     byType.getOrDefault("bug", 0L));
        stats.put("features", byType.getOrDefault("feature", 0L));
        stats.put("tasks",    byType.getOrDefault("task", 0L));

        // Overdue count - reuses the optimized getOverdueTasks()
        stats.put("overdue", getOverdueTasks().size());

        // Average hours for completed tasks from a single aggregate query
        Object[] hoursRow = taskRepository.sumHoursForCompleted();
        if (hoursRow != null && hoursRow[2] != null) {
            long completedCount = (Long) hoursRow[2];
            if (completedCount > 0) {
                long totalEstimated = hoursRow[0] != null ? ((Number) hoursRow[0]).longValue() : 0L;
                long totalActual    = hoursRow[1] != null ? ((Number) hoursRow[1]).longValue() : 0L;
                stats.put("avgEstimated", (double) totalEstimated / completedCount);
                stats.put("avgActual",    (double) totalActual    / completedCount);
                stats.put("estimateAccuracy",
                        totalEstimated > 0 ? (double) totalActual / totalEstimated * 100 : 0.0);
            } else {
                stats.put("avgEstimated", 0.0);
                stats.put("avgActual",    0.0);
                stats.put("estimateAccuracy", 0.0);
            }
        } else {
            stats.put("avgEstimated", 0.0);
            stats.put("avgActual",    0.0);
            stats.put("estimateAccuracy", 0.0);
        }

        return stats;
    }
    
    /**
     * Get project statistics - uses raw SQL (inconsistent with JPA)
     */
    public Map<String, Object> getProjectStatistics(String projectCode) {
        try {
            return DatabaseHelper.getProjectStats(projectCode);
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    /**
     * Bulk import tasks from CSV-like format
     * FIXME: No transaction management, partial imports leave inconsistent state
     */
    public List<Task> importTasks(String csvData) {
        List<Task> imported = new ArrayList<>();
        String[] lines = csvData.split("\n");
        
        for (int i = 1; i < lines.length; i++) { // skip header
            try {
                String[] fields = lines[i].split(",");
                // BUG: doesn't handle commas within quoted fields
                // BUG: no validation of field count
                
                Task task = new Task();
                task.title = fields[0].trim();
                task.description = fields.length > 1 ? fields[1].trim() : "";
                task.priority = fields.length > 2 ? Integer.parseInt(fields[2].trim()) : 2;
                task.type = fields.length > 3 ? fields[3].trim() : "task";
                task.status = STATUS_TODO;
                task.createdDate = new Date();
                
                Task saved = taskRepository.save(task);
                imported.add(saved);
                
            } catch (Exception e) {
                // Skip bad lines silently
                System.err.println("Failed to import line " + i + ": " + e.getMessage());
            }
        }
        
        System.out.println("Imported " + imported.size() + " tasks out of " + (lines.length - 1));
        return imported;
    }
    
    /**
     * Export tasks to CSV format
     */
    public String exportTasks(String projectCode) {
        List<Task> tasks;
        if (projectCode != null && !projectCode.isEmpty()) {
            tasks = taskRepository.findByProjectCode(projectCode);
        } else {
            tasks = taskRepository.findAll(); // Export ALL tasks
        }
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Title,Description,Status,Priority,Type,Assignee,Due Date,Created\n");
        
        for (Task task : tasks) {
            // BUG: No CSV escaping - titles with commas or newlines break the format
            csv.append(task.id).append(",");
            csv.append(task.title).append(",");
            csv.append(task.description != null ? task.description : "").append(",");
            csv.append(task.status).append(",");
            csv.append(task.priority).append(",");
            csv.append(task.type != null ? task.type : "").append(",");
            csv.append(task.assignee_id != null ? task.assignee_id : "").append(",");
            csv.append(task.due_date != null ? task.due_date : "").append(",");
            csv.append(task.createdDate != null ? new SimpleDateFormat("yyyy-MM-dd").format(task.createdDate) : "");
            csv.append("\n");
        }
        
        return csv.toString();
    }
    
    /**
     * Send notification - stub implementation mixed into service
     * FIXME: Should be in NotificationService
     */
    private void sendTaskNotification(Task task, String eventType) {
        // This method does nothing useful but pretends to send notifications
        String recipient = "unknown";
        if (task.assignee_id != null) {
            User assignee = userRepository.findById(task.assignee_id).orElse(null);
            if (assignee != null) {
                recipient = assignee.getEmail();
            }
        }
        
        // "Send" notification - just log it
        System.out.println("NOTIFICATION: [" + eventType + "] Task '" + task.getTitle() + 
                           "' (ID: " + task.getId() + ") -> " + recipient);
        
        // TODO: Actually integrate with email service
        // TODO: Add Slack integration
        // TODO: Add in-app notifications
        // FIXME: This was supposed to be done in Q2 2021
    }
    
    /**
     * Auto-assign tasks based on workload (never actually works properly)
     */
    public Task autoAssignTask(Long taskId) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) throw new RuntimeException("Task not found");
        
        // Find user with least tasks - VERY SLOW, loads all users and all tasks
        List<User> users = userRepository.findActiveUsers();
        User leastBusy = null;
        int minTasks = Integer.MAX_VALUE;
        
        for (User user : users) {
            List<Task> userTasks = taskRepository.findActiveTasksByAssignee(user.getId());
            if (userTasks.size() < minTasks) {
                minTasks = userTasks.size();
                leastBusy = user;
            }
        }
        
        if (leastBusy != null) {
            task.assignee_id = leastBusy.getId();
            task.updatedDate = new Date();
            return taskRepository.save(task);
        }
        
        throw new RuntimeException("No available users for assignment");
    }
    
    // ============================================================
    // DEAD CODE SECTION - These methods are not called from anywhere
    // but no one dares to delete them "just in case"
    // ============================================================
    
    /**
     * @deprecated Use getTaskStatistics() instead
     * Still referenced by old admin panel that was decommissioned in 2021
     */
    @Deprecated
    public Map<String, Integer> getOldDashboardStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("open", taskRepository.findByStatus(0).size() + taskRepository.findByStatus(1).size());
        stats.put("closed", taskRepository.findByStatus(2).size() + taskRepository.findByStatus(3).size());
        return stats;
    }
    
    /**
     * @deprecated Was used for JIRA migration in 2019, kept "just in case"
     */
    @Deprecated
    public void migrateLegacyIds() {
        List<Task> allTasks = taskRepository.findAll();
        for (Task task : allTasks) {
            if (task.legacy_id == null && task.getId() != null) {
                task.legacy_id = "LEGACY-" + task.getId();
                taskRepository.save(task);
            }
        }
    }
    
    /**
     * @deprecated Old report generation - replaced by dashboard
     */
    @Deprecated
    public String generateWeeklyReport() {
        List<Task> allTasks = taskRepository.findAll();
        StringBuilder report = new StringBuilder();
        report.append("=== Weekly Task Report ===\n");
        report.append("Generated: ").append(new Date()).append("\n\n");
        
        report.append("Total tasks: ").append(allTasks.size()).append("\n");
        
        long completed = allTasks.stream().filter(t -> t.status == STATUS_DONE).count();
        report.append("Completed: ").append(completed).append("\n");
        
        long overdue = getOverdueTasks().size();
        report.append("Overdue: ").append(overdue).append("\n");
        
        return report.toString();
    }
}
