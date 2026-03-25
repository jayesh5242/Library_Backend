package com.example.Library_backend.repository;

import com.example.Library_backend.entity.ReadingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
 
@Repository
public interface ReadingListRepository extends JpaRepository<ReadingList, Long> {
 
    // All public reading lists — for browsing
    List<ReadingList> findByIsPublicTrue();
 
    // Faculty's own reading lists
    List<ReadingList> findByFacultyId(Long facultyId);
}