package com.example.Library_backend.repository;

import com.example.Library_backend.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.util.List;
 
@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
 
    // Only return active branches for public listing
    List<Branch> findByIsActiveTrue();
 
    // Check duplicate branch name
    boolean existsByName(String name);

    // Check duplicate name — ignoring current branch
    @Query("SELECT COUNT(b) > 0 FROM Branch b " +
            "WHERE b.name = :name AND b.id != :id")
    boolean existsByNameAndIdNot(
            @Param("name") String name,
            @Param("id")   Long id);
}
 