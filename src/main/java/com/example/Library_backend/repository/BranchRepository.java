package com.example.Library_backend.repository;


import com.example.Library_backend.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BranchRepository
        extends JpaRepository<Branch, Long> {

    // Get all active branches
    List<Branch> findByIsActiveTrue();

    // Find branch by department name
    List<Branch> findByDepartment(String department);

    // Check if branch name already exists
    boolean existsByName(String name);
}