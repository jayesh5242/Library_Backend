package com.example.Library_backend.dto.response;

import lombok.*;
import java.time.LocalDateTime;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewResponse {
 
    private Long          id;
    private Long          bookId;
    private Long          userId;
    private String        userName;      // reviewer's full name
    private Integer       rating;        // 1 to 5
    private String        reviewText;
    private Boolean       isApproved;
    private LocalDateTime createdAt;
}