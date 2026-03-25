package com.example.Library_backend.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReadingListResponse {
 
    private Long              id;
    private Long              facultyId;
    private String            facultyName;
    private String            title;
    private String            subject;
    private String            semester;
    private String            description;
    private Boolean           isPublic;
    private Integer           bookCount;
    private List<BookResponse> books;   // included on detail endpoint
    private LocalDateTime     createdAt;
}