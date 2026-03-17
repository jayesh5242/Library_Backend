package com.example.Library_backend.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    // All fields are optional
    // Only provided fields will be updated

    private String fullName;
    private String phone;
    private String department;
    private String profileImage;

    // Students can update enrollment number
    private String enrollmentNo;

    // Faculty can update employee ID
    private String employeeId;
}