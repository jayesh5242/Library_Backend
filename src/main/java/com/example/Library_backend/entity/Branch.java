package com.example.Library_backend.entity;



import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "branches")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(length = 200)
    private String location;

    @Column(length = 15)
    private String phone;

    @Column(length = 100)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "librarian_id")
    private User librarian;

    @Column(name = "operating_hours", length = 100)
    private String operatingHours;

    @Column(name = "max_borrow_days")
    @Builder.Default
    private Integer maxBorrowDays = 14;

    @Column(name = "fine_per_day", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal finePerDay = BigDecimal.valueOf(2.00);

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    private List<BookInventory> inventory;
}