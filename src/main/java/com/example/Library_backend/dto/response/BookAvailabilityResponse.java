package com.example.Library_backend.dto.response;

import lombok.*;
import java.util.List;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BookAvailabilityResponse {
 
    private Long   bookId;
    private String title;
    private String isbn;
    private String author;
    private int    totalCopiesAllBranches;      // sum across all branches
    private int    availableCopiesAllBranches;  // sum across all branches
    private List<BranchStock> branches;         // per-branch breakdown
 
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BranchStock {
        private Long    branchId;
        private String  branchName;
        private String  location;
        private int     totalCopies;
        private int     availableCopies;
        private String  shelfLocation;
        private boolean isAvailable;   // true if availableCopies > 0
    }
}