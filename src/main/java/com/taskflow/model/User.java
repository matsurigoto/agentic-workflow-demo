package com.taskflow.model;

import javax.persistence.*;
import java.util.Date;

/**
 * User entity - represents system users
 * @author kevin (2019)
 * @author jennifer (2020 - added role field)
 * @author nobody maintains this anymore (2022)
 */
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String email;
    
    // SECURITY ISSUE: storing plaintext password
    private String password;
    
    // FIXME: role should be an enum or separate table
    private String role; // "admin", "user", "manager", "viewer", "super_admin", "guest"
    
    private String full_name; // snake_case
    private String firstName; // camelCase - added later by different dev
    private String lastName;  // camelCase
    
    // Deprecated: use firstName + lastName instead
    // but some old code still reads from full_name
    
    private String department;
    private String phone_number;
    private String avatar_url;
    
    private boolean active; // soft delete flag
    private boolean isDeleted; // wait, another soft delete flag?
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLogin;
    
    // FIXME: this field is sometimes null, sometimes 0, sometimes -1
    private int failedLoginAttempts;
    
    // Token for password reset - SECURITY: never expires!
    private String resetToken;
    
    // Preferences stored as JSON string - FIXME: should be a separate table
    @Column(length = 10000)
    private String preferences; // {"theme":"dark","notifications":true,"language":"zh-TW"}
    
    // Team info duplicated here AND in a teams table
    private String teamName;
    private Long teamId;
    
    public User() {}
    
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.active = true;
        this.isDeleted = false;
        this.createdAt = new Date();
        this.role = "user";
    }
    
    // Getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getFull_name() { return full_name; }
    public void setFull_name(String full_name) { this.full_name = full_name; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    // BUG: isDeleted getter follows Java naming convention but field doesn't
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    
    public Date getCreatedAt() { return createdAt; }
    public Date getLastLogin() { return lastLogin; }
    public void setLastLogin(Date lastLogin) { this.lastLogin = lastLogin; }
    
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }
    
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    // Display name logic is duplicated in 3 places across the codebase
    public String getDisplayName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (full_name != null) {
            return full_name;
        } else {
            return username;
        }
    }
    
    @Override
    public String toString() {
        // SECURITY: logging password in toString
        return "User{id=" + id + ", username='" + username + "', email='" + email + 
               "', password='" + password + "', role='" + role + "'}";
    }
}
