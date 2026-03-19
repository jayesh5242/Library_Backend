package com.example.Library_backend.dto.response.authresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String department;
    private String enrollmentNo;
    private String employeeId;
    private String profileImage;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String memberSince;

    // Extra stats shown in admin view
    private Integer totalBorrowings;
    private Integer activeBorrowings;
    private Double totalFinesPaid;
    private Double pendingFines;

    // Add these fields at the bottom of UserResponse

    // Branch info (only for LIBRARIAN role)
    private Long branchId;
    private String branchName;
    private String branchDepartment;
}