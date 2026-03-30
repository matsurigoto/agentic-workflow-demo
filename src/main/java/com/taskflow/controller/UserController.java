package com.taskflow.controller;

import com.taskflow.model.User;
import com.taskflow.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * User REST Controller
 * 
 * SECURITY: No authentication middleware
 * SECURITY: No authorization checks
 * SECURITY: Password exposed in responses
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public List<User> getAllUsers() {
        // SECURITY: Returns password field in response!
        return userService.getAllUsers();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        // SECURITY: Password included in response
        return ResponseEntity.ok(user);
    }
    
    /**
     * Register new user
     * SECURITY: No CAPTCHA, no rate limiting
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String email = body.get("email");
            String password = body.get("password");
            
            // No input validation at all
            User user = userService.createUser(username, email, password);
            
            // SECURITY: Returning password in response
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Login endpoint
     * SECURITY: Returns full user object including password
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");
            
            User user = userService.authenticate(username, password);
            
            // Should return a JWT token, instead returns user with password
            Map<String, Object> response = new HashMap<>();
            response.put("user", user); // includes password!
            response.put("token", "fake-jwt-token-" + System.currentTimeMillis()); // Not a real JWT
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updated = userService.updateUser(id, user);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Password reset request
     * SECURITY: Returns the reset token in the response body
     */
    @PostMapping("/password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> body) {
        try {
            String token = userService.requestPasswordReset(body.get("email"));
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset email sent");
            response.put("token", token); // SECURITY: Never return reset token in API response!
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Reset password
     */
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            userService.resetPassword(body.get("token"), body.get("newPassword"));
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Search users
     */
    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String q) {
        return userService.searchUsers(q);
    }
    
    // FIXME: This endpoint was added for an integration that was cancelled
    // No one knows if it's still used
    @GetMapping("/by-role/{role}")
    public List<User> getUsersByRole(@PathVariable String role) {
        return userService.getActiveUsersByRole(role);
    }
}
