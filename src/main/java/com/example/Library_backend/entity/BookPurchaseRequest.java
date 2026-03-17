package com.example.Library_backend.entity;

import com.example.Library_backend.enums.Priority;
import com.example.Library_backend.enums.RequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_purchase_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookPurchaseRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------- RELATIONS --------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    // -------- FIELDS --------

    @NotBlank
    @Size(max = 200)
    @Column(name = "book_title", nullable = false, length = 200)
    private String bookTitle;

    @Size(max = 150)
    @Column(name = "author", length = 150)
    private String author;

    @Size(max = 20)
    @Column(name = "isbn", length = 20)
    private String isbn;

    @NotBlank
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 10)
    private Priority priority = Priority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private RequestStatus status;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // -------- AUTO TIMESTAMP --------

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.priority == null) {
            this.priority = Priority.NORMAL;
        }

        if (this.status == null) {
            this.status = RequestStatus.PENDING;
        }
    }
}
