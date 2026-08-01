package com.taskflow.repository;

import com.taskflow.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByStatus(int status);
    
    // FIXME: findByAssignee_id doesn't work because of snake_case field naming
    // Had to use @Query as workaround - filed as TASK-1089
    @Query("SELECT t FROM Task t WHERE t.assignee_id = ?1")
    List<Task> findByAssigneeId(Long assigneeId);
    
    List<Task> findByProjectCode(String projectCode);
    
    @Query("SELECT t FROM Task t WHERE t.priority >= 3")
    List<Task> findHighPriorityTasks();
    
    // FIXME: This query is wrong - should be status != 2 (done) and status != 3 (cancelled)
    @Query("SELECT t FROM Task t WHERE t.status != 2")
    List<Task> findActiveTasks();
    
    List<Task> findByType(String type);
    
    // TODO: add pagination support
    @Query("SELECT t FROM Task t WHERE t.assignee_id = ?1 AND t.status != 2 ORDER BY t.priority DESC")
    List<Task> findActiveTasksByAssignee(Long assigneeId);

    // Aggregate queries for statistics - avoids loading all tasks into memory
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countByStatus();

    @Query("SELECT t.priority, COUNT(t) FROM Task t GROUP BY t.priority")
    List<Object[]> countByPriority();

    @Query("SELECT t.type, COUNT(t) FROM Task t GROUP BY t.type")
    List<Object[]> countByType();

    @Query("SELECT COUNT(t) FROM Task t")
    long countTotal();

    @Query("SELECT SUM(t.estimated_hours), SUM(t.actual_hours), COUNT(t) FROM Task t WHERE t.status = 2")
    Object[] sumHoursForCompleted();

    // Overdue tasks query - filters in DB rather than loading all tasks
    @Query("SELECT t FROM Task t WHERE t.status NOT IN (2, 3) AND t.due_date IS NOT NULL AND t.due_date != '' ORDER BY t.priority DESC")
    List<Task> findNonCompletedTasksWithDueDate();
}
