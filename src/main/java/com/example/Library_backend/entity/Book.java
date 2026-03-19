package com.example.Library_backend.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "isbn", unique = true, nullable = false)
    private String isbn;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "edition")
    private String edition;

    @Column(name = "year")
    private Integer year;

    @Column(name = "category")
    private String category;

    @Column(name = "subject")
    private String subject;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "language")
    private String language = "English";

    @Column(name = "total_pages")
    private Integer totalPages;

    @Column(name = "available")
    private Boolean available = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookInventory> inventories;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookReview> reviews;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<BorrowTransaction> borrowTransactions;
}