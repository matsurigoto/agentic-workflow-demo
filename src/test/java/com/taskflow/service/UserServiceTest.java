package com.taskflow.service;

import com.taskflow.model.User;
import com.taskflow.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UserService.
 *
 * Covers: user creation, authentication, display name resolution,
 * role-based active filtering, and bulk deactivation.
 */
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    // -------------------------------------------------------------------------
    // createUser
    // -------------------------------------------------------------------------

    @Test
    void createUser_savesUserWithDefaultRole() {
        User user = userService.createUser("alice", "alice@example.com", "secret");

        assertNotNull(user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("user", user.getRole());
        assertTrue(user.isActive());
        assertFalse(user.isDeleted());
    }

    @Test
    void createUser_throwsWhenUsernameAlreadyExists() {
        userService.createUser("bob", "bob@example.com", "pass");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.createUser("bob", "other@example.com", "pass"));
        assertTrue(ex.getMessage().contains("Username already exists"));
    }

    @Test
    void createUser_throwsWhenEmailAlreadyExists() {
        userService.createUser("carol", "carol@example.com", "pass");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.createUser("carol2", "carol@example.com", "pass"));
        assertTrue(ex.getMessage().contains("Email already exists"));
    }

    // -------------------------------------------------------------------------
    // authenticate
    // -------------------------------------------------------------------------

    @Test
    void authenticate_returnsUserOnCorrectPassword() {
        userService.createUser("dave", "dave@example.com", "correct");

        User authenticated = userService.authenticate("dave", "correct");

        assertEquals("dave", authenticated.getUsername());
        assertNotNull(authenticated.getLastLogin());
        assertEquals(0, authenticated.getFailedLoginAttempts());
    }

    @Test
    void authenticate_throwsWhenUserNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.authenticate("nobody", "pass"));
        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void authenticate_incrementsFailedAttemptsOnWrongPassword() {
        userService.createUser("eve", "eve@example.com", "correct");

        assertThrows(RuntimeException.class,
                () -> userService.authenticate("eve", "wrong"));

        User eve = userRepository.findByUsername("eve");
        assertEquals(1, eve.getFailedLoginAttempts());
    }

    @Test
    void authenticate_throwsWhenAccountDeactivated() {
        userService.createUser("frank", "frank@example.com", "pass");
        User frank = userRepository.findByUsername("frank");
        frank.setActive(false);
        userRepository.save(frank);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.authenticate("frank", "pass"));
        assertTrue(ex.getMessage().contains("deactivated"));
    }

    // -------------------------------------------------------------------------
    // getUserDisplayName
    // -------------------------------------------------------------------------

    @Test
    void getUserDisplayName_usesFirstAndLastName() {
        User user = userService.createUser("grace", "grace@example.com", "pass");
        user.setFirstName("Grace");
        user.setLastName("Hopper");
        userRepository.save(user);

        assertEquals("Grace Hopper", userService.getUserDisplayName(user.getId()));
    }

    @Test
    void getUserDisplayName_fallsBackToFullName() {
        User user = userService.createUser("hank", "hank@example.com", "pass");
        user.setFull_name("Hank Aaron");
        userRepository.save(user);

        assertEquals("Hank Aaron", userService.getUserDisplayName(user.getId()));
    }

    @Test
    void getUserDisplayName_fallsBackToUsername() {
        User user = userService.createUser("iris", "iris@example.com", "pass");

        assertEquals("iris", userService.getUserDisplayName(user.getId()));
    }

    @Test
    void getUserDisplayName_returnsUnknownUserForMissingId() {
        assertEquals("Unknown User", userService.getUserDisplayName(99999L));
    }

    // -------------------------------------------------------------------------
    // getActiveUsersByRole
    // -------------------------------------------------------------------------

    @Test
    void getActiveUsersByRole_excludesInactiveUsers() {
        User active = userService.createUser("jack", "jack@example.com", "pass");
        active.setRole("manager");
        userRepository.save(active);

        User inactive = userService.createUser("kate", "kate@example.com", "pass");
        inactive.setRole("manager");
        inactive.setActive(false);
        userRepository.save(inactive);

        List<User> managers = userService.getActiveUsersByRole("manager");

        assertEquals(1, managers.size());
        assertEquals("jack", managers.get(0).getUsername());
    }

    @Test
    void getActiveUsersByRole_excludesDeletedUsers() {
        User deleted = userService.createUser("leo", "leo@example.com", "pass");
        deleted.setRole("admin");
        deleted.setDeleted(true);
        userRepository.save(deleted);

        List<User> admins = userService.getActiveUsersByRole("admin");

        assertTrue(admins.isEmpty());
    }

    // -------------------------------------------------------------------------
    // deactivateUsers
    // -------------------------------------------------------------------------

    @Test
    void deactivateUsers_returnsCountOfDeactivatedUsers() {
        User u1 = userService.createUser("mia", "mia@example.com", "pass");
        User u2 = userService.createUser("ned", "ned@example.com", "pass");

        int count = userService.deactivateUsers(Arrays.asList(u1.getId(), u2.getId()));

        assertEquals(2, count);
        assertFalse(userRepository.findById(u1.getId()).get().isActive());
        assertFalse(userRepository.findById(u2.getId()).get().isActive());
    }

    @Test
    void deactivateUsers_ignoresMissingIds() {
        User u1 = userService.createUser("olivia", "olivia@example.com", "pass");

        int count = userService.deactivateUsers(Arrays.asList(u1.getId(), 99999L));

        assertEquals(1, count);
    }
}
