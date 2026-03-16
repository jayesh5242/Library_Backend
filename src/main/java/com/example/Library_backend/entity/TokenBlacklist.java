package com.example.Library_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "token_blacklist")
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Store the blacklisted token
    @Column(name = "token", columnDefinition = "TEXT",
            nullable = false, unique = true)
    private String token;

    // Which user logged out
    @Column(name = "email")
    private String email;

    // When was it blacklisted
    @Column(name = "blacklisted_at")
    private LocalDateTime blacklistedAt;

    // When does the token naturally expire
    // (so we can clean up old records)
    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

    @PrePersist
    protected void onCreate() {
        blacklistedAt = LocalDateTime.now();
    }
}