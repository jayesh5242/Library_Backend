package com.example.Library_backend.service;


import com.example.Library_backend.dto.request.LoginRequest;
import com.example.Library_backend.dto.request.RegisterRequest;
import com.example.Library_backend.dto.respose.AuthResponse;
import com.example.Library_backend.entity.User;
import com.example.Library_backend.enums.Role;
import com.example.Library_backend.repository.UserRepository;
import com.example.Library_backend.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    // REGISTER
    public String register(RegisterRequest request) {

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }

        // Create new user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // encrypt!
        user.setPhone(request.getPhone());
        user.setDepartment(request.getDepartment());
        user.setEnrollmentNo(request.getEnrollmentNo());
        user.setEmployeeId(request.getEmployeeId());
        user.setIsActive(true);

        // Set role (default to STUDENT if not provided)
        if (request.getRole() != null) {
            user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        } else {
            user.setRole(Role.STUDENT);
        }

        userRepository.save(user);
        return "Registration successful!";
    }

    // LOGIN
    public AuthResponse login(LoginRequest request) {

        // Authenticate - Spring Security checks email + password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()
                )
        );

        // Get user from database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if account is active
        if (!user.getIsActive()) {
            throw new RuntimeException("Your account has been blocked!");
        }

        // Generate JWT token
        String token = jwtUtils.generateToken(user.getEmail());

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.getId()
        );
    }
}