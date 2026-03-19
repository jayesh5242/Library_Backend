package com.example.Library_backend.repository;

import com.example.Library_backend.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Library_backend.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // ─────────────────────────────────────────────────────────
    // API 1: GET /api/books
    // Multi-field optional filter with pagination
    // Native query — avoids lower(bytea) PostgreSQL error
    // ─────────────────────────────────────────────────────────
    @Query(value = "SELECT * FROM books b WHERE " +
            "(:title    IS NULL OR LOWER(b.title)    LIKE LOWER(CONCAT('%', :title,    '%'))) AND " +
            "(:author   IS NULL OR LOWER(b.author)   LIKE LOWER(CONCAT('%', :author,   '%'))) AND " +
            "(:category IS NULL OR LOWER(b.category) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
            "(:subject  IS NULL OR LOWER(b.subject)  LIKE LOWER(CONCAT('%', :subject,  '%')))",
            countQuery = "SELECT COUNT(*) FROM books b WHERE " +
                    "(:title    IS NULL OR LOWER(b.title)    LIKE LOWER(CONCAT('%', :title,    '%'))) AND " +
                    "(:author   IS NULL OR LOWER(b.author)   LIKE LOWER(CONCAT('%', :author,   '%'))) AND " +
                    "(:category IS NULL OR LOWER(b.category) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
                    "(:subject  IS NULL OR LOWER(b.subject)  LIKE LOWER(CONCAT('%', :subject,  '%')))",
            nativeQuery = true)
    Page<Book> filterBooks(
            @Param("title")    String title,
            @Param("author")   String author,
            @Param("category") String category,
            @Param("subject")  String subject,
            Pageable pageable);


    // ─────────────────────────────────────────────────────────
    // API 3: GET /api/books/search?q=
    // Full-text search across title, author, isbn, subject
    // ─────────────────────────────────────────────────────────
    @Query(value = "SELECT * FROM books b WHERE " +
            "LOWER(b.title)   LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(b.author)  LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(b.isbn)    LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(b.subject) LIKE LOWER(CONCAT('%', :q, '%'))",
            countQuery = "SELECT COUNT(*) FROM books b WHERE " +
                    "LOWER(b.title)   LIKE LOWER(CONCAT('%', :q, '%')) OR " +
                    "LOWER(b.author)  LIKE LOWER(CONCAT('%', :q, '%')) OR " +
                    "LOWER(b.isbn)    LIKE LOWER(CONCAT('%', :q, '%')) OR " +
                    "LOWER(b.subject) LIKE LOWER(CONCAT('%', :q, '%'))",
            nativeQuery = true)
    Page<Book> searchBooks(@Param("q") String q, Pageable pageable);


    // ─────────────────────────────────────────────────────────
    // API 4: GET /api/books/popular
    // Top 10 most borrowed books — all time
    // ─────────────────────────────────────────────────────────
    @Query(value = "SELECT b.* FROM books b " +
            "JOIN borrow_transactions bt ON b.id = bt.book_id " +
            "GROUP BY b.id " +
            "ORDER BY COUNT(bt.id) DESC " +
            "LIMIT 10",
            nativeQuery = true)
    List<Book> findTopBorrowedBooks();


    // ─────────────────────────────────────────────────────────
    // API 5: GET /api/books/trending
    // Top 10 books borrowed in last 6 months
    // ─────────────────────────────────────────────────────────
    @Query(value = "SELECT b.* FROM books b " +
            "JOIN borrow_transactions bt ON b.id = bt.book_id " +
            "WHERE bt.issue_date >= CURRENT_DATE - INTERVAL '6 months' " +
            "GROUP BY b.id " +
            "ORDER BY COUNT(bt.id) DESC " +
            "LIMIT 10",
            nativeQuery = true)
    List<Book> findTrendingBooks();


    // ─────────────────────────────────────────────────────────
    // API 6: GET /api/books/new-arrivals
    // Books added in last 30 days, newest first
    // ─────────────────────────────────────────────────────────
    @Query(value = "SELECT * FROM books b " +
            "WHERE b.created_at >= CURRENT_DATE - INTERVAL '6 months' " +
            "ORDER BY b.created_at DESC " +
            "LIMIT 10",
            nativeQuery = true)
    List<Book> findNewArrivals();


    // ─────────────────────────────────────────────────────────
    // API 7: GET /api/books/category/{category}
    // Books filtered by category — case insensitive
    // ─────────────────────────────────────────────────────────
    @Query(value = "SELECT * FROM books b " +
            "WHERE LOWER(b.category) = LOWER(:category) " +
            "ORDER BY b.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM books b " +
                    "WHERE LOWER(b.category) = LOWER(:category)",
            nativeQuery = true)
    Page<Book> findByCategory(@Param("category") String category, Pageable pageable);


    // ─────────────────────────────────────────────────────────
    // API 10: POST /api/books
    // Check if ISBN already exists before creating
    // ─────────────────────────────────────────────────────────
    boolean existsByIsbn(String isbn);
}