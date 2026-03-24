package com.example.Library_backend.dto.response;

import lombok.*;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BranchStatsResponse {
 
    private Long    branchId;
    private String  branchName;
    private String  department;
 
    // Inventory stats
    private Integer totalBooks;          // total copies in branch
    private Integer availableBooks;      // currently available copies
    private Integer borrowedBooks;       // currently borrowed copies
 
    // Transaction stats
    private Integer activeBorrows;       // BORROWED status count
    private Integer overdueCount;        // OVERDUE status count
    private Integer totalBorrowsAllTime; // all transactions ever
 
    // Fine stats
    private Double  totalFinesPending;   // sum of unpaid fines
    private Double  totalFinesCollected; // sum of paid fines
 
    // User stats
    private Integer totalUsers;          // users assigned to this branch
}