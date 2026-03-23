package com.example.Library_backend.service;

import com.example.Library_backend.dto.request.*;
import com.example.Library_backend.dto.request.authrequest.*;
import com.example.Library_backend.dto.response.authresponse.AuthResponse;

import com.example.Library_backend.dto.response.authresponse.RefreshTokenResponse;
import com.example.Library_backend.dto.response.authresponse.UserProfileResponse;
import com.example.Library_backend.entity.Branch;
import com.example.Library_backend.entity.TokenBlacklist;
import com.example.Library_backend.entity.User;
import com.example.Library_backend.enums.Role;
import com.example.Library_backend.exception.ResourceNotFoundException;
import com.example.Library_backend.repository.BranchRepository;
import com.example.Library_backend.repository.TokenBlacklistRepository;
import com.example.Library_backend.repository.UserRepository;
import com.example.Library_backend.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {


    @Value("${app.reset-token-expiry-minutes}")
    private int resetTokenExpiryMinutes;


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final CurrentUserService currentUserService;
    private final BranchRepository branchRepository;

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


        // ── Assign branch if branchId provided ────────────
                    // Only for LIBRARIAN role
        if (request.getBranchId() != null) {

            // Only librarians can be assigned to a branch
            if (user.getRole() != Role.LIBRARIAN) {
                throw new RuntimeException(
                        "Only LIBRARIAN role can be "
                                + "assigned to a branch!");
            }

            Branch branch = branchRepository
                    .findById(request.getBranchId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Branch not found with ID: "
                                            + request.getBranchId()));

            user.setBranch(branch);
        }

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

    // ─── API 5: FORGOT PASSWORD ──────────────────────────────
    public String forgotPassword(ForgotPasswordRequest request) {

        // 1. Check if email exists in database
        //    Note: We give same message whether email exists or not
        //    This is a security best practice!
        //    (prevents attackers from knowing which emails are registered)
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        // 2. If user not found → still return success message
        //    (don't reveal that email doesn't exist)
        if (user == null) {
            return "If this email is registered, "
                    + "you will receive a reset link shortly.";
        }

        // 3. Check if account is active
        if (!user.getIsActive()) {
            throw new RuntimeException(
                    "Your account has been blocked! "
                            + "Contact the librarian.");
        }

        // 4. Generate a simple 6-digit reset token
        //    Easy for user to type from email
        String resetToken = generateSixDigitToken();

        // 5. Set expiry time (60 minutes from now)
        LocalDateTime expiryTime = LocalDateTime.now()
                .plusMinutes(resetTokenExpiryMinutes);

        // 6. Save reset token and expiry in database
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiry(expiryTime);
        userRepository.save(user);

        // 7. Send reset email
        emailService.sendPasswordResetEmail(
                user.getEmail(),
                resetToken,
                user.getFullName()
        );

        return "If this email is registered, "
                + "you will receive a reset link shortly.";
    }

    // ─── Helper: Generate 6-digit token ─────────────────────
    private String generateSixDigitToken() {
        int token = (int)(Math.random() * 900000) + 100000;
        return String.valueOf(token);
    }

    // ─── API 6: RESET PASSWORD ───────────────────────────────
    public String resetPassword(ResetPasswordRequest request) {

        // 1. Check new password matches confirm password
        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {
            throw new RuntimeException(
                    "Passwords do not match!");
        }

        // 2. Find user by reset token
        User user = userRepository
                .findByPasswordResetToken(request.getResetToken())
                .orElseThrow(() -> new RuntimeException(
                        "Invalid reset token! "
                                + "Please request a new one."));

        // 3. Check if token is expired
        if (user.getPasswordResetExpiry() == null
                || LocalDateTime.now()
                .isAfter(user.getPasswordResetExpiry())) {
            // Clear expired token from database
            user.setPasswordResetToken(null);
            user.setPasswordResetExpiry(null);
            userRepository.save(user);

            throw new RuntimeException(
                    "Reset token has expired! "
                            + "Please request a new one.");
        }

        // 4. Check account is active
        if (!user.getIsActive()) {
            throw new RuntimeException(
                    "Your account has been blocked!");
        }

        // 5. Encrypt and save new password
        user.setPassword(
                passwordEncoder.encode(request.getNewPassword()));

        // 6. Clear reset token (one time use only!)
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);

        // 7. Save updated user
        userRepository.save(user);

        // 8. Send confirmation email
        emailService.sendPasswordChangedEmail(
                user.getEmail(),
                user.getFullName()
        );

        return "Password reset successful! "
                + "Please login with your new password.";
    }

    // ─── API 7: GET PROFILE ──────────────────────────────────
    public UserProfileResponse getProfile() {

        // 1. Get currently logged in user
        User user = currentUserService.getCurrentUser();

        // 2. Build profile response
        UserProfileResponse profile = new UserProfileResponse();
        profile.setId(user.getId());
        profile.setFullName(user.getFullName());
        profile.setEmail(user.getEmail());
        profile.setPhone(user.getPhone());
        profile.setRole(user.getRole().name());
        profile.setDepartment(user.getDepartment());
        profile.setEnrollmentNo(user.getEnrollmentNo());
        profile.setEmployeeId(user.getEmployeeId());
        profile.setProfileImage(user.getProfileImage());
        profile.setIsActive(user.getIsActive());
        profile.setIsEmailVerified(user.getIsEmailVerified());
        profile.setCreatedAt(user.getCreatedAt());

        // 3. Format member since date
        //    Example: "January 2024"
        if (user.getCreatedAt() != null) {
            String memberSince = user.getCreatedAt()
                    .format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            profile.setMemberSince(memberSince);
        }

        return profile;
    }

    // ─── API 8: UPDATE PROFILE ───────────────────────────────
    public UserProfileResponse updateProfile(
            UpdateProfileRequest request) {

        // 1. Get currently logged in user
        User user = currentUserService.getCurrentUser();

        // 2. Update only fields that are provided
        //    If field is null → keep existing value

        if (request.getFullName() != null
                && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getPhone() != null
                && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone().trim());
        }

        if (request.getDepartment() != null
                && !request.getDepartment().trim().isEmpty()) {
            user.setDepartment(request.getDepartment().trim());
        }

        if (request.getProfileImage() != null
                && !request.getProfileImage().trim().isEmpty()) {
            user.setProfileImage(request.getProfileImage().trim());
        }

        if (request.getEnrollmentNo() != null
                && !request.getEnrollmentNo().trim().isEmpty()) {
            user.setEnrollmentNo(request.getEnrollmentNo().trim());
        }

        if (request.getEmployeeId() != null
                && !request.getEmployeeId().trim().isEmpty()) {
            user.setEmployeeId(request.getEmployeeId().trim());
        }

        // 3. Save updated user to database
        userRepository.save(user);

        // 4. Return updated profile
        //    Reuse getProfile() logic
        return getProfile();
    }

    // ─── API 9: CHANGE PASSWORD ──────────────────────────────
    public String changePassword(
            ChangePasswordRequest request,
            String authHeader) {

        // 1. Get currently logged in user
        User user = currentUserService.getCurrentUser();

        // 2. Verify current password is correct
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {
            throw new RuntimeException(
                    "Current password is incorrect!");
        }

        // 3. Check new password matches confirm password
        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {
            throw new RuntimeException(
                    "New passwords do not match!");
        }

        // 4. Check new password is different from current
        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {
            throw new RuntimeException(
                    "New password must be different "
                            + "from current password!");
        }

        // 5. Encrypt and save new password
        user.setPassword(
                passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 6. Blacklist current token
        //    Force user to login again with new password
        if (authHeader != null
                && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            if (!tokenBlacklistRepository.existsByToken(token)) {
                TokenBlacklist blacklisted = new TokenBlacklist();
                blacklisted.setToken(token);
                blacklisted.setEmail(user.getEmail());
                blacklisted.setTokenExpiry(
                        jwtUtils.getExpiryFromToken(token));
                tokenBlacklistRepository.save(blacklisted);
            }
        }

        // 7. Send confirmation email
        emailService.sendPasswordChangedEmail(
                user.getEmail(),
                user.getFullName()
        );

        return "Password changed successfully! "
                + "Please login with your new password.";
    }
    // ─── API 10: VERIFY EMAIL ────────────────────────────────
    public String verifyEmail(String token) {

        // 1. Find user by verification token
        User user = userRepository
                .findByEmailVerifyToken(token)
                .orElseThrow(() -> new RuntimeException(
                        "Invalid verification link! "
                                + "Please register again."));

        // 2. Check if already verified
        if (user.getIsEmailVerified()) {
            return "Email is already verified! "
                    + "You can login now.";
        }

        // 3. Check if account is active
        if (!user.getIsActive()) {
            throw new RuntimeException(
                    "Your account has been blocked! "
                            + "Contact the librarian.");
        }

        // 4. Mark email as verified
        user.setIsEmailVerified(true);

        // 5. Clear verification token
        //    (one time use only!)
        user.setEmailVerifyToken(null);

        // 6. Save updated user
        userRepository.save(user);

        // 7. Send welcome email
        emailService.sendWelcomeEmail(
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );

        return "Email verified successfully! "
                + "Welcome to College Library, "
                + user.getFullName() + "!";
    }

}