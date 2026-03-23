package com.example.Library_backend.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IssueBookRequest {
    private Long bookId;
    private Long userId;
}