package com.example.Library_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingListRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Subject is required")
    @Size(max = 100, message = "Subject must not exceed 100 characters")
    private String subject;

    @Size(max = 20)
    private String semester;     // e.g. "Sem 3", "2026-Spring"

    private String description;

    private Boolean isPublic;    // default true if not provided
}

 