package com.example.Library_backend.dto.respose;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {

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

    // Extra helpful info
    private String memberSince; // formatted date like "January 2024"
}