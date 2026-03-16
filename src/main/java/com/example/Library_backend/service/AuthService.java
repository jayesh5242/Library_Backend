package com.example.Library_backend.service;

import com.example.Library_backend.dto.request.LoginRequest;
import com.example.Library_backend.dto.request.RefreshTokenRequest;
import com.example.Library_backend.dto.request.RegisterRequest;
import com.example.Library_backend.dto.respose.AuthResponse;
import com.example.Library_backend.dto.respose.RefreshTokenResponse;
import com.example.Library_backend.entity.TokenBlacklist;
import com.example.Library_backend.entity.User;
import com.example.Library_backend.enums.Role;
import com.example.Library_backend.exception.ResourceNotFoundException;
import com.example.Library_backend.repository.TokenBlacklistRepository;
import com.example.Library_backend.repository.UserRepository;
import com.example.Library_backend.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager; // ADD THIS
    private final TokenBlacklistRepository tokenBlacklistRepository;
    // ─── API 1: REGISTER ─────────────────────────────────────
    public String register(RegisterRequest request) {

        // 1. Check if email already used
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }

        // 2. Create new user object
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        // 3. Encrypt password before saving
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setPhone(request.getPhone());
        user.setDepartment(request.getDepartment());
        user.setEnrollmentNo(request.getEnrollmentNo());
        user.setEmployeeId(request.getEmployeeId());
        user.setIsActive(true);
        user.setIsEmailVerified(false);

        // 4. Set role (default STUDENT)
        try {
            user.setRole(request.getRole() != null
                    ? Role.valueOf(request.getRole().toUpperCase())
                    : Role.STUDENT);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Invalid role! Use: STUDENT, FACULTY, LIBRARIAN, SUPER_ADMIN");
        }

        // 5. Generate email verification token
        String verifyToken = UUID.randomUUID().toString();
        user.setEmailVerifyToken(verifyToken);

        // 6. Save user to database
        userRepository.save(user);

        // 7. Send verification email
        emailService.sendVerificationEmail(user.getEmail(), verifyToken);

        return "Registration successful! Please check your email to verify your account.";
    }

    // ─── API 2: LOGIN ─────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {

        // 1. Authenticate email + password
        //    This automatically throws exception if wrong credentials
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password!");
        }

        // 2. Get user from database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found!"));

        // 3. Check if account is active (not blocked)
        if (!user.getIsActive()) {
            throw new RuntimeException(
                    "Your account has been blocked! Contact librarian.");
        }

        // 4. Generate access token (24 hours)
        String accessToken = jwtUtils.generateToken(user.getEmail());

        // 5. Generate refresh token (7 days)
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());

        // 6. Return tokens + user info
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole().name());
        response.setUserId(user.getId());
        response.setDepartment(user.getDepartment());
        response.setIsEmailVerified(user.getIsEmailVerified());

        return response;
    }

    // ─── API 3: LOGOUT ───────────────────────────────────────
    public String logout(String authHeader) {

        // 1. Check if token exists in header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("No token provided!");
        }

        // 2. Extract token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        // 3. Validate token first
        if (!jwtUtils.validateToken(token)) {
            throw new RuntimeException("Invalid token!");
        }

        // 4. Check if already blacklisted
        if (tokenBlacklistRepository.existsByToken(token)) {
            throw new RuntimeException("Already logged out!");
        }

        // 5. Get email from token
        String email = jwtUtils.getEmailFromToken(token);

        // 6. Get token expiry
        LocalDateTime expiry = jwtUtils.getExpiryFromToken(token);

        // 7. Save token to blacklist
        TokenBlacklist blacklistedToken = new TokenBlacklist();
        blacklistedToken.setToken(token);
        blacklistedToken.setEmail(email);
        blacklistedToken.setTokenExpiry(expiry);
        tokenBlacklistRepository.save(blacklistedToken);

        return "Logged out successfully!";
    }

    // ─── API 4: REFRESH TOKEN ────────────────────────────────
    public RefreshTokenResponse refreshToken(
            RefreshTokenRequest request) {

        String refreshToken = request.getRefreshToken();

        // 1. Check if refresh token is blacklisted
        if (tokenBlacklistRepository.existsByToken(refreshToken)) {
            throw new RuntimeException(
                    "Refresh token is invalid! Please login again.");
        }

        // 2. Validate refresh token
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new RuntimeException(
                    "Refresh token is expired! Please login again.");
        }

        // 3. Get email from refresh token
        String email = jwtUtils.getEmailFromToken(refreshToken);

        // 4. Check if user still exists and is active
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found!"));

        if (!user.getIsActive()) {
            throw new RuntimeException(
                    "Your account has been blocked!");
        }

        // 5. Blacklist the OLD refresh token
        //    (so it cannot be used again)
        TokenBlacklist oldToken = new TokenBlacklist();
        oldToken.setToken(refreshToken);
        oldToken.setEmail(email);
        oldToken.setTokenExpiry(
                jwtUtils.getExpiryFromToken(refreshToken));
        tokenBlacklistRepository.save(oldToken);

        // 6. Generate NEW access token
        String newAccessToken = jwtUtils.generateToken(email);

        // 7. Generate NEW refresh token
        String newRefreshToken = jwtUtils.generateRefreshToken(email);

        return new RefreshTokenResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                "Token refreshed successfully!"
        );
    }
}