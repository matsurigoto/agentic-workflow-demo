package com.taskflow.repository;

import com.taskflow.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    Project findByCode(String code);
    
    List<Project> findByStatus(int status);
    
    @Query("SELECT p FROM Project p WHERE p.owner_id = ?1")
    List<Project> findByOwner(Long ownerId);
}
