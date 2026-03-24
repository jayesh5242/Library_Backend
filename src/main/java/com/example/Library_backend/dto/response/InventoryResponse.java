package com.example.Library_backend.dto.response;

import lombok.*;
import java.time.LocalDateTime;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryResponse {
 
    private Long          id;             // inventory record ID
    private Long          bookId;
    private String        bookTitle;
    private String        bookAuthor;
    private String        isbn;
    private String        category;
    private Long          branchId;
    private String        branchName;
    private Integer       totalCopies;
    private Integer       availableCopies;
    private Integer       borrowedCopies;  // totalCopies - availableCopies
    private String        shelfLocation;
    private String        condition;       // GOOD, FAIR, POOR, DAMAGED
    private LocalDateTime addedAt;
}
 