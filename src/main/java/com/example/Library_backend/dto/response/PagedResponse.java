package com.example.Library_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedResponse<T> {

    private List<T> content;       // The actual data list
    private int pageNumber;        // Current page (0-based)
    private int pageSize;          // Items per page
    private long totalElements;    // Total records in DB
    private int totalPages;        // Total number of pages
    private boolean lastPage;      // Is this the last page?

    // Summary info
    private String message;
}