package com.taskflow.repository;

import com.taskflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    User findByUsername(String username);
    
    User findByEmail(String email);
    
    // FIXME: returns deleted users too
    List<User> findByRole(String role);
    
    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findActiveUsers();
    
    // FIXME: case-sensitive search
    @Query("SELECT u FROM User u WHERE u.username LIKE %?1% OR u.email LIKE %?1%")
    List<User> searchUsers(String keyword);
    
    User findByResetToken(String token);

    /**
     * Returns the active user with the fewest non-terminal (status not in 2,3) tasks.
     * Uses a LEFT JOIN aggregate so users with zero tasks are included and ranked first.
     */
    @Query(value =
        "SELECT u.* FROM users u " +
        "LEFT JOIN tasks t ON u.id = t.assignee_id AND t.status NOT IN (2, 3) " +
        "WHERE u.active = true " +
        "GROUP BY u.id " +
        "ORDER BY COUNT(t.id) ASC " +
        "LIMIT 1",
        nativeQuery = true)
    Optional<User> findLeastBusyActiveUser();
}
