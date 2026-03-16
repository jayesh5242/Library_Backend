package com.example.Library_backend.controller;

import com.example.Library_backend.dto.request.LoginRequest;
import com.example.Library_backend.dto.request.RefreshTokenRequest;
import com.example.Library_backend.dto.request.RegisterRequest;
import com.example.Library_backend.dto.respose.ApiResponse;
import com.example.Library_backend.dto.respose.AuthResponse;
import com.example.Library_backend.dto.respose.RefreshTokenResponse;
import com.example.Library_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ─── API 1: REGISTER ─────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    // ─── API 2: LOGIN ─────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful!", authResponse));
    }

    // ─── API 3: LOGOUT ───────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(
            @RequestHeader("Authorization") String authHeader) {

        String message = authService.logout(authHeader);
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    // ─── API 4: REFRESH TOKEN ────────────────────────────────
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        RefreshTokenResponse response =
                authService.refreshToken(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Token refreshed successfully!", response));
    }
}