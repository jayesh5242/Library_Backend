package com.example.Library_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReadingListRequest {
 
    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;
 
    @NotBlank(message = "Subject is required")
    @Size(max = 100)
    private String subject;
 
    @Size(max = 20)
    private String semester;
 
    private String description;
 
    private Boolean isPublic;   // default true
}
 