package com.example.Library_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookReviewResponse {

    private Long id;

    private Integer rating;

    private String reviewText;

    private Boolean isApproved;

    private LocalDateTime createdAt;

    // Optional (good for UI / viva 🔥)
    private Long userId;
    private String userName;
}