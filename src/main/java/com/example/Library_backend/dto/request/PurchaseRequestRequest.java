package com.example.Library_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseRequestRequest {
 
    @NotBlank(message = "Book title is required")
    @Size(max = 200)
    private String bookTitle;
 
    @Size(max = 150)
    private String author;
 
    @Size(max = 20)
    private String isbn;
 
    @NotBlank(message = "Reason is required")
    private String reason;
 
    private String priority;    // LOW, NORMAL, HIGH, URGENT
 
    @NotNull(message = "Branch ID is required")
    private Long branchId;
}