package com.example.Library_backend.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "book_inventory",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"book_id", "branch_id"}
        ))
public class BookInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "total_copies")
    private Integer totalCopies = 1;

    @Column(name = "available_copies")
    private Integer availableCopies = 1;

    @Column(name = "shelf_location")
    private String shelfLocation;

    @Column(length = 20)
    @Builder.Default
    private String condition = "GOOD";

    @Column(name = "added_at", updatable = false)
    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now();

}