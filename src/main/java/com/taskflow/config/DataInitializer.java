package com.taskflow.config;

import com.taskflow.model.Task;
import com.taskflow.model.User;
import com.taskflow.model.Project;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.repository.ProjectRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Initialize sample data for development
 * FIXME: This runs in production too because there's no profile check
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("Initializing sample data...");
        
        // Create users with plaintext passwords
        User admin = new User("admin", "admin@taskflow.local", "admin123");
        admin.setRole("admin");
        admin.setFirstName("System");
        admin.setLastName("Admin");
        admin.setDepartment("Engineering");
        userRepository.save(admin);
        
        User kevin = new User("kevin", "kevin@taskflow.local", "password");
        kevin.setRole("manager");
        kevin.setFirstName("Kevin");
        kevin.setLastName("Chen");
        kevin.setDepartment("Engineering");
        userRepository.save(kevin);
        
        User jennifer = new User("jennifer", "jennifer@taskflow.local", "jennifer2020");
        jennifer.setRole("user");
        jennifer.setFirstName("Jennifer");
        jennifer.setLastName("Wu");
        jennifer.setDepartment("Engineering");
        userRepository.save(jennifer);
        
        User david = new User("david", "david@taskflow.local", "david123!");
        david.setRole("user");
        david.setFirstName("David");
        david.setLastName("Lin");
        david.setDepartment("QA");
        userRepository.save(david);
        
        User intern = new User("mike_intern", "mike@taskflow.local", "intern2021");
        intern.setRole("user");
        intern.setFirstName("Mike");
        intern.setLastName("Wang");
        intern.setDepartment("Engineering");
        intern.setActive(false); // intern left
        userRepository.save(intern);
        
        // Create projects
        Project backend = new Project("TaskFlow Backend API", "BACKEND");
        backend.setDescription("Core API service for TaskFlow platform");
        backend.setOwner_id(kevin.getId());
        backend.setMembers(kevin.getId() + "," + jennifer.getId() + "," + david.getId());
        backend.setStatus(1); // active
        backend.setStartDate("2019-06-01");
        backend.setBudget(50000000); // cents
        projectRepository.save(backend);
        
        Project frontend = new Project("TaskFlow Web UI", "FRONTEND");
        frontend.setDescription("React-based web interface");
        frontend.setOwner_id(jennifer.getId());
        frontend.setStatus(1);
        frontend.setStartDate("2020-01-15");
        projectRepository.save(frontend);
        
        Project mobile = new Project("TaskFlow Mobile App", "MOBILE");
        mobile.setDescription("iOS and Android mobile app - ON HOLD");
        mobile.setStatus(2); // on hold
        mobile.setStartDate("2021-03-01");
        projectRepository.save(mobile);
        
        // Create tasks with various issues
        createTask("Fix login timeout issue", "Users report being logged out after 5 minutes instead of 24 hours. JWT expiration seems wrong.", 
                   1, 4, "bug", jennifer.getId(), kevin.getId(), "BACKEND", "2024-06-15");
        
        createTask("Implement password hashing", "Currently storing passwords in plaintext. Need to use bcrypt or similar.", 
                   0, 5, "bug", null, david.getId(), "BACKEND", "2023-01-01"); // overdue!
        
        createTask("Add pagination to task list API", "GET /api/tasks returns all tasks. Need pagination for performance.",
                   0, 3, "feature", null, kevin.getId(), "BACKEND", "2024-03-01"); // overdue
        
        createTask("Database connection pool optimization", "Getting intermittent connection timeout errors under load",
                   4, 4, "bug", jennifer.getId(), david.getId(), "BACKEND", "2024-09-30");
        
        createTask("Migrate from Log4j 1.x to Logback", "Log4j 1.x is EOL and has known vulnerabilities",
                   0, 3, "task", null, null, "BACKEND", "2022-06-30"); // very overdue
        
        createTask("Add unit tests for TaskService", "TaskService has 0% test coverage",
                   0, 2, "task", intern.getId(), kevin.getId(), "BACKEND", null); // assigned to inactive user
        
        createTask("Fix SQL injection in search endpoint", "The /api/tasks/search endpoint is vulnerable to SQL injection via the keyword parameter",
                   0, 5, "bug", null, david.getId(), "BACKEND", "2023-06-01"); // critical overdue
        
        createTask("Implement proper error handling", "Replace RuntimeException with proper exception hierarchy and error responses",
                   0, 2, "task", null, null, "BACKEND", null);
        
        createTask("Setup CI/CD pipeline", "Currently no automated testing or deployment",
                   0, 3, "task", null, kevin.getId(), "BACKEND", "2024-01-01");
        
        createTask("Refactor TaskService God class", "TaskService is over 700 lines and handles too many responsibilities",
                   0, 2, "task", null, null, "BACKEND", null);
        
        createTask("Add input validation to REST endpoints", "No validation on any API endpoint. Null pointer exceptions in production.",
                   0, 3, "bug", null, david.getId(), "BACKEND", "2024-07-01");
        
        createTask("Fix CORS configuration", "CORS allows all origins. Need to restrict to known domains.",
                   0, 3, "bug", null, null, "BACKEND", null);
        
        createTask("Implement soft delete for users", "Hard deleting users causes orphaned task references",
                   0, 2, "feature", null, jennifer.getId(), "BACKEND", null);
        
        createTask("Update Spring Boot to 3.x", "Running Spring Boot 2.7 which will be EOL. Need to upgrade to 3.x",
                   0, 2, "task", null, null, "BACKEND", "2024-12-31");
        
        createTask("Add API documentation (Swagger/OpenAPI)", "No API documentation exists. New developers struggle to understand endpoints.",
                   0, 1, "task", null, null, "BACKEND", null);
        
        createTask("Fix thread-safety issues in DateUtils", "SimpleDateFormat is not thread-safe. Causes intermittent parsing errors under load.",
                   1, 3, "bug", jennifer.getId(), david.getId(), "BACKEND", "2024-08-01");
        
        createTask("Remove hardcoded credentials from source code", "Application properties and source code contain hardcoded passwords",
                   0, 5, "bug", null, david.getId(), "BACKEND", "2023-03-01"); // critical overdue
        
        createTask("Implement rate limiting", "No rate limiting on API endpoints. Vulnerable to abuse.",
                   0, 3, "feature", null, null, "BACKEND", null);
        
        createTask("Fix project progress calculation", "Division by zero when project has no tasks. Crashes dashboard.",
                   0, 4, "bug", null, david.getId(), "BACKEND", "2024-05-01");
        
        createTask("Add health check endpoint", "Need /actuator/health for monitoring and load balancer",
                   0, 2, "task", null, null, "BACKEND", null);
        
        // Update project counters (they'll be wrong, which is realistic)
        backend.setTaskCount(15); // actual is 20 - demonstrating counter drift
        backend.setCompletedTaskCount(0);
        backend.setBugCount(5); // actual is more
        projectRepository.save(backend);
        
        System.out.println("Sample data initialized!");
    }
    
    private void createTask(String title, String description, int status, int priority, String type,
                           Long assigneeId, Long reporterId, String projectCode, String dueDate) {
        Task task = new Task();
        task.title = title;
        task.description = description;
        task.status = status;
        task.priority = priority;
        task.type = type;
        task.assignee_id = assigneeId;
        task.reporterId = reporterId;
        task.projectCode = projectCode;
        task.due_date = dueDate;
        task.createdDate = new Date();
        task.updatedDate = new Date();
        task.estimated_hours = (int) (Math.random() * 40) + 1;
        taskRepository.save(task);
    }
}
