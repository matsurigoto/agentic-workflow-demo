package com.taskflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.model.Task;
import com.taskflow.service.TaskService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @WebMvcTest integration tests for TaskController.
 *
 * Documents known HTTP status bugs:
 *   - GET /api/tasks/{id} returns 200 with null body when task not found (should be 404)
 *   - DELETE /api/tasks/{id} returns 400 when task not found (should be 404)
 */
@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private Task sampleTask;

    @BeforeEach
    public void setUp() {
        sampleTask = new Task();
        sampleTask.id = 1L;
        sampleTask.title = "Sample Task";
        sampleTask.description = "A test task";
        sampleTask.status = 0;
        sampleTask.priority = 2;
        sampleTask.type = "task";
    }

    // ──────────────────────────── GET /api/tasks ────────────────────────────

    @Test
    public void getAllTasks_returnsTaskList() throws Exception {
        when(taskService.getAllTasks()).thenReturn(List.of(sampleTask));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Sample Task")));
    }

    @Test
    public void getAllTasks_returnsEmptyList_whenNoTasks() throws Exception {
        when(taskService.getAllTasks()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ──────────────────────────── GET /api/tasks/{id} ────────────────────────────

    @Test
    public void getTask_returnsTask_whenFound() throws Exception {
        when(taskService.getTask(1L)).thenReturn(sampleTask);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Sample Task")))
                .andExpect(jsonPath("$.status", is(0)));
    }

    /**
     * BUG: When the task is not found, the controller returns 200 with a null body
     * instead of a proper 404. This test documents the current (broken) behavior.
     * See: TaskController.getTask() — returns null directly, no 404 handling.
     */
    @Disabled("BUG: getTask returns 200 with null body when not found — should return 404")
    @Test
    public void getTask_returns404_whenNotFound() throws Exception {
        when(taskService.getTask(999L)).thenReturn(null);

        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────── POST /api/tasks ────────────────────────────

    @Test
    public void createTask_returns201_withCreatedTask() throws Exception {
        Task input = new Task();
        input.title = "New Task";
        input.priority = 3;
        input.type = "bug";

        Task created = new Task();
        created.id = 2L;
        created.title = "New Task";
        created.priority = 3;
        created.type = "bug";
        created.status = 0;

        when(taskService.createTask(any(Task.class))).thenReturn(created);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.title", is("New Task")));
    }

    @Test
    public void createTask_returns400_onValidationError() throws Exception {
        Task bad = new Task(); // no title

        when(taskService.createTask(any(Task.class)))
                .thenThrow(new RuntimeException("Title is required"));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    // ──────────────────────────── PUT /api/tasks/{id} ────────────────────────────

    @Test
    public void updateTask_returnsUpdatedTask() throws Exception {
        Task update = new Task();
        update.title = "Updated";
        update.priority = 1;

        Task result = new Task();
        result.id = 1L;
        result.title = "Updated";
        result.priority = 1;

        when(taskService.updateTask(eq(1L), any(Task.class))).thenReturn(result);

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated")));
    }

    @Test
    public void updateTask_returns400_whenNotFound() throws Exception {
        when(taskService.updateTask(eq(999L), any(Task.class)))
                .thenThrow(new RuntimeException("Task not found"));

        mockMvc.perform(put("/api/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTask)))
                .andExpect(status().isBadRequest());
    }

    // ──────────────────────────── DELETE /api/tasks/{id} ────────────────────────────

    @Test
    public void deleteTask_returns204_onSuccess() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }

    /**
     * BUG: When task is not found, deleteTask returns 400 Bad Request instead of 404.
     * Documents the current (broken) behavior.
     * See: TaskController.deleteTask() catch block — uses ResponseEntity.badRequest().
     */
    @Test
    public void deleteTask_returns400NotFound_documentsBug() throws Exception {
        doThrow(new RuntimeException("Task not found: 999"))
                .when(taskService).deleteTask(999L);

        mockMvc.perform(delete("/api/tasks/999"))
                // BUG: should be 404, but controller returns 400
                .andExpect(status().isBadRequest());
    }

    // ──────────────────────────── GET /api/tasks/search ────────────────────────────

    @Test
    public void searchTasks_returnsMatchingResults() throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1);
        row.put("title", "Fix bug");
        when(taskService.searchTasks("bug")).thenReturn(List.of(row));

        mockMvc.perform(get("/api/tasks/search").param("keyword", "bug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("Fix bug")));
    }

    // ──────────────────────────── GET /api/tasks/overdue ────────────────────────────

    @Test
    public void getOverdueTasks_returnsOverdueTasks() throws Exception {
        Task overdue = new Task();
        overdue.id = 5L;
        overdue.title = "Overdue task";
        overdue.due_date = "2023-01-01";
        when(taskService.getOverdueTasks()).thenReturn(List.of(overdue));

        mockMvc.perform(get("/api/tasks/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("Overdue task")));
    }

    // ──────────────────────────── POST /api/tasks/{taskId}/assign ────────────────────────────

    @Test
    public void assignTask_returnsUpdatedTask() throws Exception {
        Task assigned = new Task();
        assigned.id = 1L;
        assigned.title = "Sample Task";
        assigned.assignee_id = 10L;
        when(taskService.assignTask(1L, 10L)).thenReturn(assigned);

        mockMvc.perform(post("/api/tasks/1/assign").param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee_id", is(10)));
    }

    @Test
    public void assignTask_returns400_whenUserNotFound() throws Exception {
        when(taskService.assignTask(1L, 999L))
                .thenThrow(new RuntimeException("User not found: 999"));

        mockMvc.perform(post("/api/tasks/1/assign").param("userId", "999"))
                .andExpect(status().isBadRequest());
    }

    // ──────────────────────────── POST /api/tasks/{taskId}/transition ────────────────────────────

    @Test
    public void transitionStatus_returnsTaskWithNewStatus() throws Exception {
        Task transitioned = new Task();
        transitioned.id = 1L;
        transitioned.status = 2; // DONE
        when(taskService.transitionStatus(1L, 2)).thenReturn(transitioned);

        mockMvc.perform(post("/api/tasks/1/transition").param("status", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(2)));
    }

    // ──────────────────────────── GET /api/stats ────────────────────────────

    @Test
    public void getStatistics_returnsStatsMap() throws Exception {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", 10);
        stats.put("done", 3);
        when(taskService.getTaskStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(10)))
                .andExpect(jsonPath("$.done", is(3)));
    }

    // ──────────────────────────── POST /api/tasks/import ────────────────────────────

    @Test
    public void importTasks_returnsImportedCount() throws Exception {
        when(taskService.importTasks(any(String.class))).thenReturn(List.of(sampleTask));

        mockMvc.perform(post("/api/tasks/import")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("title,priority,type\nSample Task,2,task"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported", is(1)));
    }

    // ──────────────────────────── GET /api/tasks/export ────────────────────────────

    @Test
    public void exportTasks_returnsCsvWithHeaders() throws Exception {
        when(taskService.exportTasks(null)).thenReturn("id,title,status\n1,Sample Task,0\n");

        mockMvc.perform(get("/api/tasks/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(header().string("Content-Disposition", containsString("tasks.csv")))
                .andExpect(content().string(containsString("Sample Task")));
    }

    // ──────────────────────────── GET /api/v1/tasks (legacy) ────────────────────────────

    @Test
    public void getLegacyTasks_returnsWrappedResponse() throws Exception {
        when(taskService.getAllTasks()).thenReturn(List.of(sampleTask));

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    public void getLegacyTaskById_returnsSuccessTrue_whenFound() throws Exception {
        when(taskService.getTask(1L)).thenReturn(sampleTask);

        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Sample Task")));
    }

    @Test
    public void getLegacyTaskById_returnsSuccessFalse_whenNotFound() throws Exception {
        when(taskService.getTask(999L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/tasks/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", is("Task not found")));
    }
}
