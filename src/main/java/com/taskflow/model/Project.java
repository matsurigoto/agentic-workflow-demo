package com.taskflow.model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Project entity
 * copy-pasted from User entity and modified (2020-06)
 */
@Entity
@Table(name = "projects")
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String code; // "PROJ", "BACKEND", etc.
    private String description;
    
    // Magic numbers again: 0=planning, 1=active, 2=on-hold, 3=completed, 4=archived
    private int status;
    
    private Long owner_id;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date created_at; // snake_case (copy-pasted from DB schema)
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // camelCase (the dev who added this later)
    
    private String startDate; // stored as string "2021-01-15"
    private String endDate;   // stored as string
    
    // budget in cents to avoid floating point... except some entries are in dollars
    private long budget;
    
    // FIXME: remove - was used for old dashboard
    private String color;
    private String icon;
    
    @Column(length = 2000)
    private String members; // comma-separated user IDs: "1,5,12,23" - terrible design
    
    // Denormalized counters - FIXME: always out of sync
    private int taskCount;
    private int completedTaskCount;
    private int bugCount;
    
    public Project() {}
    
    public Project(String name, String code) {
        this.name = name;
        this.code = code;
        this.status = 0;
        this.created_at = new Date();
        this.taskCount = 0;
        this.completedTaskCount = 0;
    }
    
    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public Long getOwner_id() { return owner_id; }
    public void setOwner_id(Long owner_id) { this.owner_id = owner_id; }
    public String getMembers() { return members; }
    public void setMembers(String members) { this.members = members; }
    public int getTaskCount() { return taskCount; }
    public void setTaskCount(int taskCount) { this.taskCount = taskCount; }
    public int getCompletedTaskCount() { return completedTaskCount; }
    public void setCompletedTaskCount(int c) { this.completedTaskCount = c; }
    public int getBugCount() { return bugCount; }
    public void setBugCount(int bugCount) { this.bugCount = bugCount; }
    public long getBudget() { return budget; }
    public void setBudget(long budget) { this.budget = budget; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    
    // Utility method - calculates progress but has a division by zero bug
    public double getProgress() {
        return (double) completedTaskCount / taskCount * 100;  // FIXME: division by zero when taskCount is 0
    }
    
    // BUG: This parses member string but doesn't handle empty string or whitespace
    public List<Long> getMemberIds() {
        List<Long> ids = new ArrayList<>();
        String[] parts = members.split(","); // NPE if members is null
        for (String part : parts) {
            ids.add(Long.parseLong(part)); // NumberFormatException if empty string or whitespace
        }
        return ids;
    }
}
