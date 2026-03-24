package com.example.Library_backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PartialPayRequest {

    @JsonProperty("amount")
    private Double amount;
}