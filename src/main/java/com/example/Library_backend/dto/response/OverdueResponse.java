package com.example.Library_backend.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OverdueResponse {
 
    private Long      transactionId;
    private Long      bookId;
    private String    bookTitle;
    private String    isbn;
    private Long      userId;
    private String    userName;
    private String    userEmail;
    private String    userPhone;
    private Long      branchId;
    private String    branchName;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private Integer   daysOverdue;      // today - dueDate
    private Double    fineAmount;       // daysOverdue * branch.finePerDay
    private LocalDateTime createdAt;
}