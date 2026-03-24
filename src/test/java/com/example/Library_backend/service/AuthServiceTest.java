package com.example.Library_backend.service;

import com.example.Library_backend.dto.request.authrequest.LoginRequest;
import com.example.Library_backend.dto.request.authrequest.RegisterRequest;
import com.example.Library_backend.dto.response.authresponse.AuthResponse;
import com.example.Library_backend.entity.User;
import com.example.Library_backend.enums.Role;
import com.example.Library_backend.repository.TokenBlacklistRepository;
import com.example.Library_backend.repository.UserRepository;

import com.example.Library_backend.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// @ExtendWith = use Mockito with JUnit 5
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // @Mock = create a fake version of this class
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private EmailService emailService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private TokenBlacklistRepository tokenBlacklistRepository;
    @Mock private CurrentUserService currentUserService;

    // @InjectMocks = create real AuthService
    // but inject all the @Mock objects above into it
    @InjectMocks private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;

    // @BeforeEach = runs before every test method
    @BeforeEach
    void setUp() {
        // Create a test user object
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setEmail("test@college.edu");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setRole(Role.STUDENT);
        testUser.setIsActive(true);
        testUser.setIsEmailVerified(false);

        // Create a register request
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("New User");
        registerRequest.setEmail("newuser@college.edu");
        registerRequest.setPassword("password123");
        registerRequest.setRole("STUDENT");
    }

    // ─── TEST 1: Successful Registration ──────────────
    @Test
    void register_WithValidData_ShouldSucceed() {
        // ARRANGE: Setup mock behavior
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false); // email not taken
        when(passwordEncoder.encode(anyString()))
                .thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);
        doNothing().when(emailService)
                .sendVerificationEmail(anyString(), anyString());

        // ACT: Call the method being tested
        String result = authService.register(registerRequest);

        // ASSERT: Check the result
        assertNotNull(result);
        assertTrue(result.contains("successful"));

        // Verify interactions
        verify(userRepository, times(1))
                .existsByEmail("newuser@college.edu");
        verify(passwordEncoder, times(1))
                .encode("password123");
        verify(userRepository, times(1))
                .save(any(User.class));
    }

    // ─── TEST 2: Registration with Duplicate Email ────
    @Test
    void register_WithDuplicateEmail_ShouldThrowException() {
        // ARRANGE: Email already exists
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(true);

        // ACT + ASSERT: Should throw RuntimeException
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.register(registerRequest)
        );

        assertEquals("Email already registered!", exception.getMessage());

        // Verify save was NEVER called
        verify(userRepository, never()).save(any());
    }

    // ─── TEST 3: Successful Login ──────────────────────
    @Test
    void login_WithValidCredentials_ShouldReturnTokens() {
        // ARRANGE
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@college.edu");
        loginRequest.setPassword("password123");

        when(userRepository.findByEmail("test@college.edu"))
                .thenReturn(Optional.of(testUser));
        when(jwtUtils.generateToken(anyString()))
                .thenReturn("mockAccessToken");
        when(jwtUtils.generateRefreshToken(anyString()))
                .thenReturn("mockRefreshToken");

        // ACT
        AuthResponse response = authService.login(loginRequest);

        // ASSERT
        assertNotNull(response);
        assertEquals("mockAccessToken", response.getAccessToken());
        assertEquals("mockRefreshToken", response.getRefreshToken());
        assertEquals("test@college.edu", response.getEmail());
        assertEquals("STUDENT", response.getRole());
    }

    // ─── TEST 4: Login with Blocked Account ───────────
    @Test
    void login_WithBlockedAccount_ShouldThrowException() {
        // ARRANGE: User is blocked
        testUser.setIsActive(false);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@college.edu");
        loginRequest.setPassword("password123");

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(testUser));

        // ACT + ASSERT
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login(loginRequest)
        );
        assertTrue(ex.getMessage().contains("blocked"));
    }

    // ─── TEST 5: Invalid Role in Registration ─────────
    @Test
    void register_WithInvalidRole_ShouldThrowException() {
        registerRequest.setRole("HACKER"); // invalid role

        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("hashed");

        assertThrows(
                RuntimeException.class,
                () -> authService.register(registerRequest)
        );
    }
}