package com.example.Library_backend.controller;

import com.example.Library_backend.dto.request.authrequest.*;
import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.authresponse.AuthResponse;
import com.example.Library_backend.dto.response.authresponse.RefreshTokenResponse;
import com.example.Library_backend.dto.response.authresponse.UserProfileResponse;
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

    // ─── API 5: FORGOT PASSWORD ──────────────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        String message = authService.forgotPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(message, null));
    }

    // ─── API 6: RESET PASSWORD ───────────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        String message = authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(message, null));
    }

    // ─── API 7: GET PROFILE ──────────────────────────────────
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getProfile() {

        UserProfileResponse profile = authService.getProfile();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile fetched successfully!", profile));
    }

    // ─── API 8: UPDATE PROFILE ───────────────────────────────
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(
            @RequestBody UpdateProfileRequest request) {

        UserProfileResponse updated =
                authService.updateProfile(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile updated successfully!", updated));
    }

    // ─── API 9: CHANGE PASSWORD ──────────────────────────────
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String message = authService.changePassword(
                request, authHeader);

        return ResponseEntity.ok(
                ApiResponse.success(message, null));
    }


    // ─── API 10: VERIFY EMAIL ────────────────────────────────
    @GetMapping("/verify-email/{token}")
    public ResponseEntity<ApiResponse> verifyEmail(
            @PathVariable String token) {

        String message = authService.verifyEmail(token);

        return ResponseEntity.ok(
                ApiResponse.success(message, null));
    }
}