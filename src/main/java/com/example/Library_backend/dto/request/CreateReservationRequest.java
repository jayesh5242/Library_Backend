package com.example.Library_backend.dto.request;

import lombok.Data;

@Data
public class CreateReservationRequest {

    private Long bookId;
    private Long userId;

}
