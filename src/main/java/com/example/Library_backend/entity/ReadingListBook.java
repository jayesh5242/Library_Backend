package com.example.Library_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reading_list_books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ReadingListBookId.class)
public class ReadingListBook {

    // -------- COMPOSITE KEY --------

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reading_list_id", nullable = false)
    private ReadingList readingList;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // -------- EXTRA FIELDS --------

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
