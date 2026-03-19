package com.example.Library_backend.dto.response;

import lombok.*;
import java.time.LocalDateTime;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BookResponse {
    private Long          id;
    private String        title;
    private String        author;
    private String        isbn;
    private String        publisher;
    private String        edition;
    private Integer       year;
    private String        category;
    private String        subject;
    private String        description;
    private String        coverImageUrl;
    private String        language;
    private Integer       totalPages;
    private Integer       totalCopies;       // aggregated from inventory
    private Integer       availableCopies;   // aggregated from inventory
    private Double        averageRating;     // aggregated from reviews
    private Integer       reviewCount;
    private LocalDateTime createdAt;
}