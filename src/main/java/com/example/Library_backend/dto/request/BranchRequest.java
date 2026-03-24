package com.example.Library_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchRequest {
 
    @NotBlank(message = "Branch name is required")
    @Size(max = 100, message = "Branch name must not exceed 100 characters")
    private String name;
 
    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;
 
    @Size(max = 200)
    private String location;
 
    @Size(max = 15)
    private String phone;
 
    @Email(message = "Enter a valid email address")
    @Size(max = 100)
    private String email;
 
    private Long librarianId;       // optional — assign later if needed
 
    @Size(max = 100)
    private String operatingHours;  // e.g. "Mon-Fri 9AM-5PM"
 
    @Min(value = 1, message = "Max borrow days must be at least 1")
    @Max(value = 365, message = "Max borrow days must not exceed 365")
    private Integer maxBorrowDays;  // default 14 if not provided
 
    @DecimalMin(value = "0.0", message = "Fine per day must be positive")
    private BigDecimal finePerDay;  // default 2.00 if not provided
}