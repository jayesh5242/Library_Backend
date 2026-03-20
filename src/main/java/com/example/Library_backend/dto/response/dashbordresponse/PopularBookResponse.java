package com.example.Library_backend.dto.response.dashbordresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PopularBookResponse {

    private Long bookId;
    private String title;
    private String author;
    private String category;
    private Long borrowCount;
    private int rank;
}