package com.taskflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.model.Task;
import com.taskflow.service.TaskService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TaskController using MockMvc.
 *
 * Several tests document known bugs (marked with BUG comments) so they
 * serve as regression tests once the bugs are fixed.
 */
@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    // ── GET /api/tasks ────────────────────────────────────────────────────────

    @Test
    public void getAllTasks_returnsEmptyList() throws Exception {
        when(taskService.getAllTasks()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void getAllTasks_returnsList() throws Exception {
        Task t1 = new Task();
        t1.id = 1L;
        t1.title = "Task One";
        t1.status = 0;

        when(taskService.getAllTasks()).thenReturn(List.of(t1));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Task One"));
    }

    // ── GET /api/tasks/{id} ───────────────────────────────────────────────────

    @Test
    public void getTask_found_returns200WithTask() throws Exception {
        Task t = new Task();
        t.id = 5L;
        t.title = "Found Task";
        t.status = 1;

        when(taskService.getTask(5L)).thenReturn(t);

        mockMvc.perform(get("/api/tasks/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("Found Task"));
    }

    /**
     * BUG DOCUMENTATION: getTask() returns HTTP 200 with a null body when the
     * task does not exist. The correct behaviour would be HTTP 404.
     * This test documents the current (buggy) behaviour so the bug is
     * visible in CI; it should be updated to expect 404 once the bug is fixed.
     */
    @Test
    public void getTask_notFound_returns200WithNullBody_bug() throws Exception {
        when(taskService.getTask(99L)).thenReturn(null);

        // BUG: should be 404 but currently returns 200
        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isOk());
    }

    // ── POST /api/tasks ───────────────────────────────────────────────────────

    @Test
    public void createTask_validInput_returns201WithCreatedTask() throws Exception {
        Task input = new Task();
        input.title = "New Task";
        input.status = 0;

        Task saved = new Task();
        saved.id = 10L;
        saved.title = "New Task";
        saved.status = 0;

        when(taskService.createTask(any(Task.class))).thenReturn(saved);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("New Task"));
    }

    @Test
    public void createTask_serviceThrows_returns400() throws Exception {
        Task input = new Task();
        input.title = "Bad Task";

        when(taskService.createTask(any(Task.class)))
                .thenThrow(new RuntimeException("Validation failed"));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/tasks/{id} ───────────────────────────────────────────────────

    @Test
    public void updateTask_found_returns200WithUpdatedTask() throws Exception {
        Task input = new Task();
        input.title = "Updated Title";

        Task updated = new Task();
        updated.id = 3L;
        updated.title = "Updated Title";
        updated.status = 2;

        when(taskService.updateTask(eq(3L), any(Task.class))).thenReturn(updated);

        mockMvc.perform(put("/api/tasks/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    // ── DELETE /api/tasks/{id} ────────────────────────────────────────────────

    @Test
    public void deleteTask_existing_returns204() throws Exception {
        doNothing().when(taskService).deleteTask(7L);

        mockMvc.perform(delete("/api/tasks/7"))
                .andExpect(status().isNoContent());
    }

    /**
     * BUG DOCUMENTATION: deleteTask() returns HTTP 400 when the task is not
     * found. The correct behaviour would be HTTP 404.
     * This test documents the current (buggy) behaviour.
     */
    @Test
    public void deleteTask_notFound_returns400_bug() throws Exception {
        doThrow(new RuntimeException("Task not found")).when(taskService).deleteTask(999L);

        // BUG: should be 404 but currently returns 400
        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/tasks/search ─────────────────────────────────────────────────

    @Test
    public void searchTasks_returnsResults() throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1);
        row.put("title", "Bug fix");

        when(taskService.searchTasks("bug")).thenReturn(List.of(row));

        mockMvc.perform(get("/api/tasks/search").param("keyword", "bug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Bug fix"));
    }

    // ── GET /api/stats ────────────────────────────────────────────────────────

    @Test
    public void getStatistics_returnsMap() throws Exception {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", 5);
        stats.put("done", 2);

        when(taskService.getTaskStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.done").value(2));
    }

    // ── GET /api/v1/tasks (legacy) ────────────────────────────────────────────

    @Test
    public void getLegacyTasks_returnsWrappedResponse() throws Exception {
        Task t = new Task();
        t.id = 1L;
        t.title = "Legacy Task";

        when(taskService.getAllTasks()).thenReturn(List.of(t));

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Legacy Task"));
    }

    @Test
    public void getLegacyTask_notFound_returnsSuccessFalse() throws Exception {
        when(taskService.getTask(404L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/tasks/404"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Task not found"));
    }
}
