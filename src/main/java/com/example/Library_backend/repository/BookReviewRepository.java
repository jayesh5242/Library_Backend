package com.example.Library_backend.repository;

import com.example.Library_backend.entity.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
import java.util.Optional;
 
@Repository
public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
 
    // Get all approved reviews for a book
    List<BookReview> findByBookIdAndIsApprovedTrue(Long bookId);
 
    // Check if user already reviewed this book
    Optional<BookReview> findByBookIdAndUserId(Long bookId, Long userId);
 
    // Check duplicate review
    boolean existsByBookIdAndUserId(Long bookId, Long userId);
}