package com.example.Library_backend.dto.response.dashbordresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowingTrendResponse {

    private int year;

    // Month names: Jan, Feb, Mar...
    private List<String> months;

    // Count per month: [12, 8, 15, 20...]
    private List<Long> borrowingCounts;

    // Total for the year
    private Long yearTotal;
}