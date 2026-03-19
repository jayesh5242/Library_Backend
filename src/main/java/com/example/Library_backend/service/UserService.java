package com.example.Library_backend.service;

import com.example.Library_backend.dto.request.RegisterRequest;
import com.example.Library_backend.dto.request.UpdateUserRequest;
import com.example.Library_backend.dto.response.PagedResponse;
import com.example.Library_backend.dto.response.UserResponse;
import com.example.Library_backend.entity.User;
import com.example.Library_backend.enums.Role;
import com.example.Library_backend.exception.ResourceNotFoundException;
import com.example.Library_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CurrentUserService currentUserService;

    // ─── HELPER: Convert User entity to UserResponse DTO ─
    public UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole().name());
        response.setDepartment(user.getDepartment());
        response.setEnrollmentNo(user.getEnrollmentNo());
        response.setEmployeeId(user.getEmployeeId());
        response.setProfileImage(user.getProfileImage());
        response.setIsActive(user.getIsActive());
        response.setIsEmailVerified(user.getIsEmailVerified());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        // Format member since
        if (user.getCreatedAt() != null) {
            response.setMemberSince(
                    user.getCreatedAt().format(
                            DateTimeFormatter.ofPattern("MMMM yyyy")));
        }

        return response;
    }

    // ─── API 1: GET ALL USERS ─────────────────────────────
    public PagedResponse<UserResponse> getAllUsers(
            int page, int size, String sortBy, String role) {

        // Create pageable object
        Pageable pageable = PageRequest.of(
                page, size, Sort.by(sortBy).descending());

        Page<User> usersPage;

        // Filter by role if provided
        if (role != null && !role.isEmpty()) {
            try {
                Role roleEnum = Role.valueOf(role.toUpperCase());
                usersPage = userRepository
                        .findByRole(roleEnum, pageable);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(
                        "Invalid role: " + role);
            }
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        // Convert to response DTOs
        List<UserResponse> users = usersPage.getContent()
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                users,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isLast(),
                "Users fetched successfully!"
        );
    }

    // ─── API 2: GET USER BY ID ────────────────────────────
    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id));

        return toUserResponse(user);
    }

    // ─── API 3: CREATE USER (Admin creates user) ──────────
    public UserResponse createUser(RegisterRequest request) {

        // Check email unique
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                    "Email already registered!");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setDepartment(request.getDepartment());
        user.setEnrollmentNo(request.getEnrollmentNo());
        user.setEmployeeId(request.getEmployeeId());
        user.setIsActive(true);

        // Admin-created accounts are auto-verified
        user.setIsEmailVerified(true);

        // Set role
        try {
            user.setRole(request.getRole() != null
                    ? Role.valueOf(request.getRole().toUpperCase())
                    : Role.STUDENT);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role!");
        }

        userRepository.save(user);

        // Send welcome email
        emailService.sendWelcomeEmail(
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );

        return toUserResponse(user);
    }

    // ─── API 4: UPDATE USER ───────────────────────────────
    public UserResponse updateUser(
            Long id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id));

        // Update only provided fields
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

        if (request.getEnrollmentNo() != null
                && !request.getEnrollmentNo().trim().isEmpty()) {
            user.setEnrollmentNo(
                    request.getEnrollmentNo().trim());
        }

        if (request.getEmployeeId() != null
                && !request.getEmployeeId().trim().isEmpty()) {
            user.setEmployeeId(request.getEmployeeId().trim());
        }

        if (request.getProfileImage() != null
                && !request.getProfileImage().trim().isEmpty()) {
            user.setProfileImage(
                    request.getProfileImage().trim());
        }

        if (request.getIsEmailVerified() != null) {
            user.setIsEmailVerified(
                    request.getIsEmailVerified());
        }

        userRepository.save(user);
        return toUserResponse(user);
    }

    // ─── API 5: DELETE / DEACTIVATE USER ─────────────────
    public String deactivateUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id));

        // Prevent admin from deleting themselves
        User currentUser = currentUserService.getCurrentUser();
        if (user.getId().equals(currentUser.getId())) {
            throw new RuntimeException(
                    "You cannot deactivate your own account!");
        }

        // Soft delete — just mark as inactive
        user.setIsActive(false);
        userRepository.save(user);

        return "User '" + user.getFullName()
                + "' has been deactivated successfully!";
    }

    // ─── API 6: CHANGE USER ROLE ──────────────────────────
    public UserResponse changeUserRole(Long id, String role) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id));

        // Prevent changing own role
        User currentUser = currentUserService.getCurrentUser();
        if (user.getId().equals(currentUser.getId())) {
            throw new RuntimeException(
                    "You cannot change your own role!");
        }

        // Validate and set role
        try {
            Role newRole = Role.valueOf(role.toUpperCase());
            user.setRole(newRole);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Invalid role! Use: STUDENT, FACULTY, "
                            + "LIBRARIAN, SUPER_ADMIN");
        }

        userRepository.save(user);
        return toUserResponse(user);
    }

    // ─── API 7: BLOCK USER ────────────────────────────────
    public String blockUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id));

        // Prevent blocking yourself
        User currentUser = currentUserService.getCurrentUser();
        if (user.getId().equals(currentUser.getId())) {
            throw new RuntimeException(
                    "You cannot block your own account!");
        }

        // Prevent blocking another admin
        if (user.getRole() == Role.SUPER_ADMIN) {
            throw new RuntimeException(
                    "Cannot block a Super Admin account!");
        }

        if (!user.getIsActive()) {
            throw new RuntimeException(
                    "User is already blocked!");
        }

        user.setIsActive(false);
        userRepository.save(user);

        // Notify user by email
        emailService.sendEmail(
                user.getEmail(),
                "Account Blocked - College Library",
                "Hello " + user.getFullName() + ",\n\n"
                        + "Your library account has been blocked.\n"
                        + "Please contact the librarian for assistance.\n\n"
                        + "College Library Team"
        );

        return "User '" + user.getFullName()
                + "' has been blocked successfully!";
    }

    // ─── API 8: UNBLOCK USER ──────────────────────────────
    public String unblockUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id));

        if (user.getIsActive()) {
            throw new RuntimeException(
                    "User account is already active!");
        }

        user.setIsActive(true);
        userRepository.save(user);

        // Notify user by email
        emailService.sendEmail(
                user.getEmail(),
                "Account Unblocked - College Library",
                "Hello " + user.getFullName() + ",\n\n"
                        + "Your library account has been unblocked.\n"
                        + "You can now login and use library services.\n\n"
                        + "College Library Team"
        );

        return "User '" + user.getFullName()
                + "' has been unblocked successfully!";
    }

    // ─── API 9: SEARCH USERS ─────────────────────────────
    public PagedResponse<UserResponse> searchUsers(
            String query, int page, int size) {

        if (query == null || query.trim().isEmpty()) {
            throw new RuntimeException(
                    "Search query cannot be empty!");
        }

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("fullName").ascending());

        Page<User> usersPage = userRepository
                .searchUsers(query.trim(), pageable);

        List<UserResponse> users = usersPage.getContent()
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                users,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isLast(),
                "Search results for: " + query
        );
    }

    // ─── API 10: GET USER ACTIVITY ────────────────────────
    public UserResponse getUserActivity(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id));

        // Return user details
        // (In Level 4+ we will add borrow history here)
        return toUserResponse(user);
    }

    // ─── API 11: BULK IMPORT USERS ───────────────────────
    public String bulkImportUsers(
            List<RegisterRequest> users) {

        if (users == null || users.isEmpty()) {
            throw new RuntimeException(
                    "No users provided for import!");
        }

        if (users.size() > 500) {
            throw new RuntimeException(
                    "Cannot import more than 500 users at once!");
        }

        int successCount = 0;
        int skipCount = 0;
        StringBuilder skippedEmails = new StringBuilder();

        for (RegisterRequest request : users) {

            // Skip if email already exists
            if (userRepository.existsByEmail(
                    request.getEmail())) {
                skipCount++;
                skippedEmails.append(request.getEmail())
                        .append(", ");
                continue;
            }

            try {
                User user = new User();
                user.setFullName(request.getFullName());
                user.setEmail(request.getEmail());
                user.setPassword(passwordEncoder.encode(
                        request.getPassword() != null
                                ? request.getPassword()
                                : "library@123"  // default password
                ));
                user.setPhone(request.getPhone());
                user.setDepartment(request.getDepartment());
                user.setEnrollmentNo(request.getEnrollmentNo());
                user.setEmployeeId(request.getEmployeeId());
                user.setIsActive(true);
                user.setIsEmailVerified(true);
                user.setRole(request.getRole() != null
                        ? Role.valueOf(request.getRole().toUpperCase())
                        : Role.STUDENT);

                userRepository.save(user);
                successCount++;

            } catch (Exception e) {
                skipCount++;
            }
        }

        return String.format(
                "Bulk import complete! "
                        + "Successfully imported: %d users. "
                        + "Skipped: %d users (already exist).",
                successCount, skipCount
        );
    }
}