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

    @Query("SELECT u FROM User u WHERE u.role = ?1 AND u.active = true AND u.deleted = false")
    List<User> findActiveUsersByRole(String role);

    @Query("UPDATE User u SET u.active = false WHERE u.id IN ?1")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    int deactivateByIds(List<Long> ids);

    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findActiveUsers();
    
    // FIXME: case-sensitive search
    @Query("SELECT u FROM User u WHERE u.username LIKE %?1% OR u.email LIKE %?1%")
    List<User> searchUsers(String keyword);
    
    User findByResetToken(String token);
}
