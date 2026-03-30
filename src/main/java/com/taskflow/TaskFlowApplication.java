package com.taskflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TaskFlow - Task Management System
 * Created by the team, 2019
 * 
 * Main application entry point
 */
@SpringBootApplication
public class TaskFlowApplication {

    // TODO: add banner
    // TODO: add health check endpoint
    
    public static void main(String[] args) {
        System.out.println("Starting TaskFlow...");
        SpringApplication.run(TaskFlowApplication.class, args);
        System.out.println("TaskFlow started successfully!");
    }
}
