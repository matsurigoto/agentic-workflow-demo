package com.taskflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.model.User;
import com.taskflow.service.UserService;

import org.junit.jupiter.api.BeforeEach;
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
 * @WebMvcTest integration tests for UserController.
 *
 * Documents both correct behavior and known bugs (annotated with comments).
 * Uses MockBean for UserService — no DB required.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("alice", "alice@example.com", "secret");
        sampleUser.setId(1L);
        sampleUser.setFirstName("Alice");
        sampleUser.setLastName("Smith");
        sampleUser.setRole("user");
    }

    // --- GET /api/users ---

    @Test
    void getAllUsers_returnsListOfUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"));
    }

    @Test
    void getAllUsers_emptyList_returnsEmptyArray() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // --- GET /api/users/{id} ---

    @Test
    void getUser_existingId_returnsUser() throws Exception {
        when(userService.getUser(1L)).thenReturn(sampleUser);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void getUser_notFound_returns404() throws Exception {
        when(userService.getUser(99L)).thenReturn(null);

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/users/register ---

    @Test
    void registerUser_validInput_returnsCreatedUser() throws Exception {
        when(userService.createUser("alice", "alice@example.com", "secret"))
                .thenReturn(sampleUser);

        Map<String, String> body = Map.of(
                "username", "alice",
                "email", "alice@example.com",
                "password", "secret");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void registerUser_duplicateUsername_returns400() throws Exception {
        when(userService.createUser(any(), any(), any()))
                .thenThrow(new RuntimeException("Username already exists"));

        Map<String, String> body = Map.of(
                "username", "alice",
                "email", "other@example.com",
                "password", "secret");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username already exists"));
    }

    // --- POST /api/users/login ---

    @Test
    void login_validCredentials_returnsUserAndToken() throws Exception {
        when(userService.authenticate("alice", "secret")).thenReturn(sampleUser);

        Map<String, String> body = Map.of("username", "alice", "password", "secret");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("alice"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(userService.authenticate(any(), any()))
                .thenThrow(new RuntimeException("Invalid password"));

        Map<String, String> body = Map.of("username", "alice", "password", "wrong");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_deactivatedAccount_returns401() throws Exception {
        when(userService.authenticate(any(), any()))
                .thenThrow(new RuntimeException("Account is deactivated"));

        Map<String, String> body = Map.of("username", "alice", "password", "secret");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    // --- PUT /api/users/{id} ---

    @Test
    void updateUser_existingUser_returnsUpdated() throws Exception {
        User updated = new User("alice", "alice@new.com", "secret");
        updated.setId(1L);
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@new.com"));
    }

    @Test
    void updateUser_notFound_returns400() throws Exception {
        // BUG DOCUMENTED: UserController returns 400 (not 404) when update fails with RuntimeException.
        // This is consistent with how the controller is written — it catches RuntimeException broadly.
        when(userService.updateUser(eq(99L), any(User.class)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleUser)))
                .andExpect(status().isBadRequest());
    }

    // --- DELETE /api/users/{id} ---

    @Test
    void deleteUser_existingUser_returns204() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_notFound_returns400() throws Exception {
        // BUG DOCUMENTED: controller returns 400 for "not found" on delete (should be 404).
        doThrow(new RuntimeException("User not found")).when(userService).deleteUser(99L);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isBadRequest());
    }

    // --- POST /api/users/password-reset ---

    @Test
    void requestPasswordReset_validEmail_returnsToken() throws Exception {
        when(userService.requestPasswordReset("alice@example.com")).thenReturn("reset-token-123");

        Map<String, String> body = Map.of("email", "alice@example.com");

        mockMvc.perform(post("/api/users/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset email sent"))
                // BUG DOCUMENTED: token returned in response body (security issue in production)
                .andExpect(jsonPath("$.token").value("reset-token-123"));
    }

    @Test
    void requestPasswordReset_unknownEmail_returns400() throws Exception {
        when(userService.requestPasswordReset(any()))
                .thenThrow(new RuntimeException("Email not found"));

        Map<String, String> body = Map.of("email", "nobody@example.com");

        mockMvc.perform(post("/api/users/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // --- POST /api/users/password-reset/confirm ---

    @Test
    void resetPassword_validToken_returns200() throws Exception {
        doNothing().when(userService).resetPassword("reset-token-123", "newPassword");

        Map<String, String> body = Map.of("token", "reset-token-123", "newPassword", "newPassword");

        mockMvc.perform(post("/api/users/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successful"));
    }

    @Test
    void resetPassword_invalidToken_returns400() throws Exception {
        doThrow(new RuntimeException("Invalid or expired token"))
                .when(userService).resetPassword(any(), any());

        Map<String, String> body = Map.of("token", "bad-token", "newPassword", "newPass");

        mockMvc.perform(post("/api/users/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // --- GET /api/users/search ---

    @Test
    void searchUsers_returnsMatchingUsers() throws Exception {
        when(userService.searchUsers("alice")).thenReturn(List.of(sampleUser));

        mockMvc.perform(get("/api/users/search").param("q", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"));
    }

    @Test
    void searchUsers_noMatch_returnsEmptyList() throws Exception {
        when(userService.searchUsers("zzz")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users/search").param("q", "zzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // --- GET /api/users/by-role/{role} ---

    @Test
    void getUsersByRole_returnsActiveUsersForRole() throws Exception {
        when(userService.getActiveUsersByRole("admin")).thenReturn(List.of(sampleUser));

        mockMvc.perform(get("/api/users/by-role/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"));
    }
}
