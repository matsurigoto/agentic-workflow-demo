package com.taskflow.controller;

import com.taskflow.model.Project;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Project Controller
 * 
 * NOTE: Uses repository directly instead of going through a service layer
 * "We didn't have time to create ProjectService" - 2020
 * 
 * FIXME: Inconsistent with TaskController and UserController patterns
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    
    // FIXME: Controller should not directly access repository
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private TaskService taskService;
    
    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getProject(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<?> getProjectByCode(@PathVariable String code) {
        Project project = projectRepository.findByCode(code);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(project);
    }
    
    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody Project project) {
        // No validation at all
        project.setStatus(0);
        Project saved = projectRepository.save(project);
        return ResponseEntity.ok(saved); // Should be 201 Created
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody Project project) {
        if (!projectRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        project.setId(id);
        // BUG: Overwrites all fields including counters, created_at, etc.
        return ResponseEntity.ok(projectRepository.save(project));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        // FIXME: Doesn't check if project has active tasks
        // FIXME: Doesn't reassign or archive tasks
        projectRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get project dashboard - mixes data from different sources
     */
    @GetMapping("/{id}/dashboard")
    public ResponseEntity<?> getProjectDashboard(@PathVariable Long id) {
        Optional<Project> projectOpt = projectRepository.findById(id);
        if (!projectOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Project project = projectOpt.get();
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("project", project);
        
        // Mix of JPA data and raw SQL stats
        dashboard.put("tasks", taskService.getTasksByProject(project.getCode()));
        dashboard.put("stats", taskService.getProjectStatistics(project.getCode()));
        dashboard.put("progress", project.getProgress()); // May throw ArithmeticException
        
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * Add member to project
     * FIXME: Uses comma-separated string instead of join table
     */
    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMember(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Optional<Project> projectOpt = projectRepository.findById(id);
        if (!projectOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Project project = projectOpt.get();
        Long userId = body.get("userId");
        
        String members = project.getMembers();
        if (members == null || members.isEmpty()) {
            project.setMembers(userId.toString());
        } else {
            // BUG: Doesn't check if user already in list
            project.setMembers(members + "," + userId);
        }
        
        projectRepository.save(project);
        return ResponseEntity.ok(project);
    }
}
