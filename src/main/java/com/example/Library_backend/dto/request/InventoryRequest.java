package com.example.Library_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryRequest {

    @NotNull(message = "Book ID is required")
    private Long bookId;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotNull(message = "Total copies is required")
    @Min(value = 1, message = "Total copies must be at least 1")
    private Integer totalCopies;

    @Min(value = 0, message = "Available copies cannot be negative")
    private Integer availableCopies;  // optional — defaults to totalCopies

    @Size(max = 50)
    private String shelfLocation;     // e.g. "Row A-3"

    @Size(max = 20)
    private String condition;         // GOOD, FAIR, POOR, DAMAGED
}