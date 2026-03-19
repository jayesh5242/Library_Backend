package com.example.Library_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BookRequest {
 
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
 
    @NotBlank(message = "Author is required")
    @Size(max = 150, message = "Author must not exceed 150 characters")
    private String author;
 
    @NotBlank(message = "ISBN is required")
    @Size(max = 20, message = "ISBN must not exceed 20 characters")
    private String isbn;
 
    @Size(max = 150)
    private String publisher;
 
    @Size(max = 20)
    private String edition;
 
    @Min(value = 1000, message = "Year must be valid")
    @Max(value = 2100, message = "Year must be valid")
    private Integer year;
 
    @NotBlank(message = "Category is required")
    @Size(max = 50)
    private String category;
 
    @Size(max = 100)
    private String subject;
 
    private String description;
 
    @Size(max = 500)
    private String coverImageUrl;
 
    @Size(max = 30)
    private String language;
 
    @Min(value = 1, message = "Total pages must be at least 1")
    private Integer totalPages;
}