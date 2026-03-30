package com.taskflow.repository;

import com.taskflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
