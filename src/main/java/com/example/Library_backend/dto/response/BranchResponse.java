package com.example.Library_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BranchResponse {
    private Long id;
    private String name;
    private String department;
    private String location;
    private String phone;
    private String email;
    private String operatingHours;
    private Integer maxBorrowDays;
    private Double finePerDay;
    private Boolean isActive;

    // Librarian info (simple — no password!)
    private Long librarianId;
    private String librarianName;
    private String librarianEmail;

    private Long        id;
    private String      name;
    private String      department;
    private String      location;
    private String      phone;
    private String      email;
    private Long        librarianId;
    private String      librarianName;
    private String      operatingHours;
    private Integer     maxBorrowDays;
    private BigDecimal  finePerDay;
    private Boolean     isActive;
    private LocalDateTime createdAt;

    // Stats — only populated on /stats endpoint
    private Integer totalBooks;
    private Integer availableBooks;
    private Integer activeBorrows;
    private Integer overdueCount;
}