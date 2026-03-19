package com.example.Library_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BookReviewRequest {
 
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    private Integer rating;
 
    @Size(max = 1000, message = "Review text must not exceed 1000 characters")
    private String reviewText;  // optional — rating alone is enough
}