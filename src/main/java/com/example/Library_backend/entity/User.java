package com.example.Library_backend.entity;

import com.example.Library_backend.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data                    // Lombok: auto creates getters, setters, toString
@NoArgsConstructor       // Lombok: auto creates empty constructor
@AllArgsConstructor      // Lombok: auto creates constructor with all fields
@Entity                  // JPA: this class is a database table
@Table(name = "users")   // JPA: table name in database
public class User {

    @Id                                    // This is the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto increment
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)           // Save enum as text in DB
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "department")
    private String department;

    @Column(name = "enrollment_no", unique = true)
    private String enrollmentNo;

    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist   // Runs automatically BEFORE saving to database
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate    // Runs automatically BEFORE updating in database
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}