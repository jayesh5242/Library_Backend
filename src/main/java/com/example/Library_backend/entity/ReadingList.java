package com.example.Library_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reading_lists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private User faculty;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(length = 20)
    private String semester;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "reading_list_books",
            joinColumns = @JoinColumn(name = "reading_list_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    @Builder.Default
    private List<Book> books = new ArrayList<>();
}

