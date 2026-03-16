package com.example.Library_backend.controller;

import com.example.Library_backend.dto.request.LoginRequest;
import com.example.Library_backend.dto.request.RegisterRequest;
import com.example.Library_backend.dto.respose.ApiResponse;
import com.example.Library_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController               // This class handles REST API requests
@RequestMapping("/api/auth")  // All URLs in this class start with /api/auth
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @Valid @RequestBody LoginRequest request) {
        var authResponse = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful!", authResponse));
    }
}