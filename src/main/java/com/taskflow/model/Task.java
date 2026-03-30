package com.taskflow.model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Task entity
 * TODO: add audit fields
 * FIXME: created_at vs createdDate naming inconsistency
 */
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    // no validation at all
    public String title;
    public String description;
    
    // Magic numbers: 0=todo, 1=in-progress, 2=done, 3=cancelled, 4=blocked, 5=review
    public int status;
    
    // Magic numbers: 1=low, 2=medium, 3=high, 4=critical, 5=blocker
    public int priority;
    
    // FIXME: should be enum, using string for "flexibility"
    public String type; // "bug", "feature", "task", "story", "epic", "subtask"
    
    public Long assignee_id; // snake_case mixed with camelCase
    public Long reporterId;
    
    public String projectCode; // e.g. "PROJ-001"
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date createdDate;
    
    @Column(name = "updated_at")  
    @Temporal(TemporalType.TIMESTAMP)
    public Date updatedDate;
    
    // FIXME: due_date stored as string, parsing issues in production
    public String due_date; // format: "yyyy-MM-dd" or "MM/dd/yyyy" or "dd-MM-yyyy" depending on who created it
    
    public int estimated_hours; // sometimes negative values appear in DB
    public int actual_hours;
    
    @Column(length = 5000)
    public String notes; // raw HTML stored directly, XSS risk

    // TODO: implement tags properly
    public String tags; // comma-separated: "backend,urgent,Q3"
    
    // dead field - was used for old workflow engine
    public String workflow_state;
    public String legacy_id; // from JIRA migration, still referenced by some reports
    
    @Transient
    public List<String> attachmentUrls = new ArrayList<>();
    
    // No-arg constructor required by JPA
    public Task() {}
    
    // Constructor with inconsistent parameter order
    public Task(String title, int priority, String type, String description, Long assignee_id) {
        this.title = title;
        this.priority = priority;
        this.type = type;
        this.description = description;
        this.assignee_id = assignee_id;
        this.createdDate = new Date();
        this.status = 0;
    }
    
    // Another constructor - parameter explosion
    public Task(String title, String description, int status, int priority, String type, 
                Long assignee_id, Long reporterId, String projectCode, String due_date,
                int estimated_hours, String notes, String tags) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.type = type;
        this.assignee_id = assignee_id;
        this.reporterId = reporterId;
        this.projectCode = projectCode;
        this.due_date = due_date;
        this.estimated_hours = estimated_hours;
        this.notes = notes;
        this.tags = tags;
        this.createdDate = new Date();
    }
    
    // Manual getters/setters with inconsistent naming
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; } // inconsistent param name
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    // Missing getters/setters for many fields...
    
    // FIXME: toString causes NPE when fields are null
    @Override
    public String toString() {
        return "Task{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", status=" + status +
            ", assignee=" + assignee_id.toString() +  // NPE if null
            ", due=" + due_date.trim() +               // NPE if null
            '}';
    }
    
    // equals/hashCode not implemented - causes issues with collections
    // FIXME: reported in JIRA-4521, still not fixed
}
