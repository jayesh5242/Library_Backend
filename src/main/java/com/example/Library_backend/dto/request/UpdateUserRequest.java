package com.example.Library_backend.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {

    // All fields optional
    // Only provided fields will be updated
    private String fullName;
    private String phone;
    private String department;
    private String enrollmentNo;
    private String employeeId;
    private String profileImage;

    // Admin can also set these
    private Boolean isActive;
    private Boolean isEmailVerified;
}