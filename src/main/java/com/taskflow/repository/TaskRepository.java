package com.taskflow.repository;

import com.taskflow.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /** Count all tasks — used by generateWeeklyReport() to avoid findAll(). */
    @Query("SELECT COUNT(t) FROM Task t")
    long countAll();

    /** Count tasks with a specific status — used by generateWeeklyReport(). */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = ?1")
    long countByStatus(int status);

    /**
     * Count overdue tasks: non-done, non-cancelled tasks whose due_date is a
     * non-empty string that compares lexicographically less than today's date
     * in ISO-8601 format ("yyyy-MM-dd").  This mirrors the DB-side filter
     * already used in the optimised getOverdueTasks() query.
     */
    @Query("SELECT COUNT(t) FROM Task t " +
           "WHERE t.status != 2 AND t.status != 3 " +
           "AND t.due_date IS NOT NULL AND t.due_date != '' " +
           "AND t.due_date < ?1")
    long countOverdue(String todayIso);
    
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
}
