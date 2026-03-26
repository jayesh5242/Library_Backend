package com.example.Library_backend.entity;

import com.example.Library_backend.enums.TransferStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inter_branch_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterBranchTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------- RELATIONS --------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnore
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_branch_id")
    @JsonIgnore
    private Branch fromBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_branch_id")
    @JsonIgnore
    private Branch toBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by")
    @JsonIgnore
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    @JsonIgnore
    private User approvedBy;

    // -------- FIELDS --------

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    @Column(name = "request_date", nullable = false, updatable = false)
    private LocalDateTime requestDate;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // -------- AUTO TIMESTAMP --------

    @PrePersist
    protected void onCreate() {
        this.requestDate = LocalDateTime.now();
    }
}
