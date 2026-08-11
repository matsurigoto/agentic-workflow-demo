package com.taskflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.model.Project;
import com.taskflow.repository.ProjectRepository;
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
 * @WebMvcTest integration tests for ProjectController.
 *
 * <p>ProjectController accesses ProjectRepository directly (no service layer)
 * and delegates task data to TaskService. Both are mocked here so no database
 * is required.
 *
 * <p>Several tests document known bugs — marked with comments so maintainers
 * can track them without relying solely on issues.
 */
@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private TaskService taskService;

    // ──────────────────────────────────────────────────────
    // GET /api/projects
    // ──────────────────────────────────────────────────────

    @Test
    void getAllProjects_returnsEmptyList_whenNoneExist() throws Exception {
        when(projectRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getAllProjects_returnsList_whenProjectsExist() throws Exception {
        Project p1 = new Project("Alpha", "ALPHA");
        p1.setId(1L);
        Project p2 = new Project("Beta", "BETA");
        p2.setId(2L);

        when(projectRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("ALPHA"))
                .andExpect(jsonPath("$[1].code").value("BETA"));
    }

    // ──────────────────────────────────────────────────────
    // GET /api/projects/{id}
    // ──────────────────────────────────────────────────────

    @Test
    void getProject_returns200_whenFound() throws Exception {
        Project p = new Project("Alpha", "ALPHA");
        p.setId(1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ALPHA"));
    }

    @Test
    void getProject_returns404_whenNotFound() throws Exception {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/projects/99"))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────────────────
    // GET /api/projects/code/{code}
    // ──────────────────────────────────────────────────────

    @Test
    void getProjectByCode_returns200_whenFound() throws Exception {
        Project p = new Project("Alpha", "ALPHA");
        p.setId(1L);
        when(projectRepository.findByCode("ALPHA")).thenReturn(p);

        mockMvc.perform(get("/api/projects/code/ALPHA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alpha"));
    }

    @Test
    void getProjectByCode_returns404_whenNotFound() throws Exception {
        when(projectRepository.findByCode("MISSING")).thenReturn(null);

        mockMvc.perform(get("/api/projects/code/MISSING"))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────────────────
    // POST /api/projects
    // ──────────────────────────────────────────────────────

    @Test
    void createProject_returns200_andSetsStatusToPlanning() throws Exception {
        // BUG: Should return 201 Created, but returns 200 OK (documented in controller)
        Project input = new Project("New Project", "NEWPROJ");
        Project saved = new Project("New Project", "NEWPROJ");
        saved.setId(10L);
        saved.setStatus(0); // 0 = planning

        when(projectRepository.save(any(Project.class))).thenReturn(saved);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk()) // BUG: should be 201 but controller returns 200
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value(0)); // status forced to 0 (planning)
    }

    @Test
    void createProject_forcesStatusToZero_ignoringRequestStatus() throws Exception {
        // Even if caller sends status=2, controller overwrites with 0
        Project input = new Project("Project", "PROJ");
        input.setStatus(2); // on-hold

        Project saved = new Project("Project", "PROJ");
        saved.setId(11L);
        saved.setStatus(0);

        when(projectRepository.save(any(Project.class))).thenReturn(saved);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(0));
    }

    // ──────────────────────────────────────────────────────
    // PUT /api/projects/{id}
    // ──────────────────────────────────────────────────────

    @Test
    void updateProject_returns200_whenFound() throws Exception {
        when(projectRepository.existsById(1L)).thenReturn(true);

        Project updated = new Project("Updated", "ALPHA");
        updated.setId(1L);
        when(projectRepository.save(any(Project.class))).thenReturn(updated);

        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void updateProject_returns404_whenNotFound() throws Exception {
        when(projectRepository.existsById(99L)).thenReturn(false);

        Project input = new Project("X", "X");
        mockMvc.perform(put("/api/projects/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────────────────
    // DELETE /api/projects/{id}
    // ──────────────────────────────────────────────────────

    @Test
    void deleteProject_returns204_andCallsDelete() throws Exception {
        // BUG: Doesn't verify project exists before deleting;
        // deleteById on a non-existent ID throws EmptyResultDataAccessException
        // but we mock it away here so the test passes.
        doNothing().when(projectRepository).deleteById(1L);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNoContent());

        verify(projectRepository).deleteById(1L);
    }

    // ──────────────────────────────────────────────────────
    // GET /api/projects/{id}/dashboard
    // ──────────────────────────────────────────────────────

    @Test
    void getProjectDashboard_returns200_withMixedData() throws Exception {
        Project p = new Project("Alpha", "ALPHA");
        p.setId(1L);
        p.setTaskCount(5);
        p.setCompletedTaskCount(3);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(taskService.getTasksByProject("ALPHA")).thenReturn(Collections.emptyList());
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", 5);
        when(taskService.getProjectStatistics("ALPHA")).thenReturn(stats);

        mockMvc.perform(get("/api/projects/1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project.code").value("ALPHA"))
                .andExpect(jsonPath("$.stats.total").value(5));
    }

    @Test
    void getProjectDashboard_returns404_whenProjectNotFound() throws Exception {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/projects/99/dashboard"))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────────────────
    // POST /api/projects/{id}/members
    // ──────────────────────────────────────────────────────

    @Test
    void addMember_returns200_andAppendsMemberId() throws Exception {
        Project p = new Project("Alpha", "ALPHA");
        p.setId(1L);
        p.setMembers("5,10");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Long> body = Collections.singletonMap("userId", 15L);

        mockMvc.perform(post("/api/projects/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members").value("5,10,15"));
    }

    @Test
    void addMember_setsMembers_whenListWasPreviouslyEmpty() throws Exception {
        Project p = new Project("Alpha", "ALPHA");
        p.setId(1L);
        p.setMembers(null); // no members yet

        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Long> body = Collections.singletonMap("userId", 7L);

        mockMvc.perform(post("/api/projects/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members").value("7"));
    }

    @Test
    void addMember_returns404_whenProjectNotFound() throws Exception {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        Map<String, Long> body = Collections.singletonMap("userId", 7L);

        mockMvc.perform(post("/api/projects/99/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addMember_doesNotDeduplicateMembers_documentedBug() throws Exception {
        // BUG: Documented in controller — adding a userId that's already in the list
        // produces a duplicate. This test documents current (buggy) behavior.
        Project p = new Project("Alpha", "ALPHA");
        p.setId(1L);
        p.setMembers("5");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Long> body = Collections.singletonMap("userId", 5L); // already a member

        mockMvc.perform(post("/api/projects/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members").value("5,5")); // duplicated
    }
}
