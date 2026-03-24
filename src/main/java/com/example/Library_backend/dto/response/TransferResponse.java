package com.example.Library_backend.dto.response;

import com.example.Library_backend.enums.TransferStatus;
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
public class TransferResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("book_id")
    private Long bookId;

    @JsonProperty("book_name")
    private String bookName;

    @JsonProperty("from_branch_id")
    private Long fromBranchId;

    @JsonProperty("from_branch_name")
    private String fromBranchName;

    @JsonProperty("to_branch_id")
    private Long toBranchId;

    @JsonProperty("to_branch_name")
    private String toBranchName;

    @JsonProperty("requested_by")
    private Long requestedBy;

    @JsonProperty("approved_by")
    private Long approvedBy;

    @JsonProperty("status")
    private TransferStatus status;

    @JsonProperty("request_date")
    private LocalDateTime requestDate;

    @JsonProperty("completion_date")
    private LocalDateTime completionDate;

    @JsonProperty("notes")
    private String notes;
}