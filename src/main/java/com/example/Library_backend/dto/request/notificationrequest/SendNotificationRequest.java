package com.example.Library_backend.dto.request.notificationrequest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendNotificationRequest {

    // Who to send to (for /send API)
    private Long userId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    // Type: DUE_REMINDER, FINE_ALERT, BOOK_READY,
    //       GENERAL, ACCOUNT_BLOCKED etc.
    private String type = "GENERAL";

    // Also send email? true/false
    private Boolean sendEmail = false;
}