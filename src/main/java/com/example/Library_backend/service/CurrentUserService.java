package com.example.Library_backend.service;

import com.example.Library_backend.entity.User;
import com.example.Library_backend.exception.ResourceNotFoundException;
import com.example.Library_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    // ─── Get currently logged in user ────────────────────
    public User getCurrentUser() {

        // 1. Get authentication object from Spring Security
        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        // 2. Check if user is authenticated
        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new RuntimeException(
                    "No authenticated user found!");
        }

        // 3. Get email from authentication
        //    (we stored email as the principal/username)
        String email = authentication.getName();

        // 4. Find and return user from database
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found!"));
    }

    // ─── Get current user's ID ────────────────────────────
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    // ─── Get current user's email ────────────────────────
    public String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    // ─── Get current user's role ─────────────────────────
    public String getCurrentUserRole() {
        return getCurrentUser().getRole().name();
    }

    // ─── Check if current user is Admin ──────────────────
    public boolean isAdmin() {
        return getCurrentUserRole()
                .equals("SUPER_ADMIN");
    }

    // ─── Check if current user is Librarian ──────────────
    public boolean isLibrarian() {
        String role = getCurrentUserRole();
        return role.equals("LIBRARIAN")
                || role.equals("SUPER_ADMIN");
    }
}