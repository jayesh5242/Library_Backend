package com.example.Library_backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateTransferRequest {

    @JsonProperty("book_id")
    private Long bookId;

    @JsonProperty("from_branch_id")
    private Long fromBranchId;

    @JsonProperty("to_branch_id")
    private Long toBranchId;

    @JsonProperty("requested_by")
    private Long requestedBy;

    @JsonProperty("notes")
    private String notes;
}