package com.example.Library_backend.dto.response;

import com.example.Library_backend.enums.FineStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("transaction_id")
    private Long transactionId;

    @JsonProperty("days_overdue")
    private Integer daysOverdue;

    @JsonProperty("amount_per_day")
    private Double amountPerDay;

    @JsonProperty("total_amount")
    private Double totalAmount;

    @JsonProperty("paid_amount")
    private Double paidAmount;

    @JsonProperty("status")
    private FineStatus status;

    @JsonProperty("paid_at")
    private LocalDateTime paidAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
