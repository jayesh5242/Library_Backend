package com.example.Library_backend.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequestResponse {

    private Long          id;
    private Long          requestedById;
    private String        requestedByName;
    private Long          branchId;
    private String        branchName;
    private String        bookTitle;
    private String        author;
    private String        isbn;
    private String        reason;
    private String        priority;   // enum name as String
    private String        status;     // enum name as String
    private Long          approvedById;
    private String        approvedByName;
    private String        adminNotes;
    private LocalDateTime createdAt;
}
