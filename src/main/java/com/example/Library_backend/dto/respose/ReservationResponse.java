package com.example.Library_backend.dto.respose;

import com.example.Library_backend.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private Long id;

    private Long bookId;
    private Long userId;

    private String bookName;
    private String userName;

    private ReservationStatus status;

    private LocalDateTime reservedAt;
    private LocalDate expiryDate;

}
