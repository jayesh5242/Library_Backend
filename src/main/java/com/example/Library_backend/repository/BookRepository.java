package com.example.Library_backend.repository;

import com.example.Library_backend.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            "(:subject  IS NULL OR LOWER(b.subject)  LIKE LOWER(CONCAT('%', :subject,  '%'))) " +
            "ORDER BY b.created_at DESC",
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


    @Query("SELECT b FROM Book b WHERE b.isFeatured = true ORDER BY b.featuredOrder ASC")
    List<Book> findFeaturedBooks();

    @Query("SELECT b FROM Book b LEFT JOIN b.borrowTransactions bt GROUP BY b ORDER BY COUNT(bt) DESC, b.createdAt DESC")
    List<Book> findTopBorrowedBooks(Pageable pageable);

    @Query("SELECT b FROM Book b LEFT JOIN b.borrowTransactions bt ON bt.issueDate >= :sixMonthsAgo GROUP BY b ORDER BY COUNT(bt) DESC, b.createdAt DESC")
    List<Book> findTrendingBooks(@Param("sixMonthsAgo") java.time.LocalDate sixMonthsAgo, Pageable pageable);

    @Query("SELECT b FROM Book b ORDER BY b.createdAt DESC")
    List<Book> findNewArrivals(Pageable pageable);


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