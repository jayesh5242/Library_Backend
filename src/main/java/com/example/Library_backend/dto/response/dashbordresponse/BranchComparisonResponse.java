package com.example.Library_backend.dto.response.dashbordresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BranchComparisonResponse {

    private Long branchId;
    private String branchName;
    private String department;
    private Long totalBorrowings;
    private Long totalBooks;
    private Long availableBooks;
    private String librarianName;

    // Percentage of total borrowings
    private Double borrowingPercentage;
}