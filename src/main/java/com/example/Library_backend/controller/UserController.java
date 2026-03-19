package com.example.Library_backend.controller;

import com.example.Library_backend.dto.request.RegisterRequest;
import com.example.Library_backend.dto.request.UpdateUserRequest;
import com.example.Library_backend.dto.respose.ApiResponse;
import com.example.Library_backend.dto.respose.PagedResponse;
import com.example.Library_backend.dto.response.UserResponse;
import com.example.Library_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ─── API 1: GET ALL USERS ─────────────────────────────
    // GET /api/users?page=0&size=10&sortBy=createdAt&role=STUDENT
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt")
            String sortBy,
            @RequestParam(required = false) String role) {

        PagedResponse<UserResponse> users =
                userService.getAllUsers(page, size, sortBy, role);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Users fetched successfully!", users));
    }

    // ─── API 2: GET USER BY ID ────────────────────────────
    // GET /api/users/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> getUserById(
            @PathVariable Long id) {

        UserResponse user = userService.getUserById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User fetched successfully!", user));
    }

    // ─── API 3: CREATE USER ───────────────────────────────
    // POST /api/users
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> createUser(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse user = userService.createUser(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User created successfully!", user));
    }

    // ─── API 4: UPDATE USER ───────────────────────────────
    // PUT /api/users/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request) {

        UserResponse user =
                userService.updateUser(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User updated successfully!", user));
    }

    // ─── API 5: DEACTIVATE USER ───────────────────────────
    // DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> deactivateUser(
            @PathVariable Long id) {

        String message = userService.deactivateUser(id);

        return ResponseEntity.ok(
                ApiResponse.success(message, null));
    }

    // ─── API 6: CHANGE ROLE ───────────────────────────────
    // PUT /api/users/{id}/role
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> changeUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String role = request.get("role");
        if (role == null || role.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Role is required!"));
        }

        UserResponse user =
                userService.changeUserRole(id, role);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User role updated successfully!", user));
    }

    // ─── API 7: BLOCK USER ────────────────────────────────
    // PUT /api/users/{id}/block
    @PutMapping("/{id}/block")
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> blockUser(
            @PathVariable Long id) {

        String message = userService.blockUser(id);

        return ResponseEntity.ok(
                ApiResponse.success(message, null));
    }

    // ─── API 8: UNBLOCK USER ──────────────────────────────
    // PUT /api/users/{id}/unblock
    @PutMapping("/{id}/unblock")
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> unblockUser(
            @PathVariable Long id) {

        String message = userService.unblockUser(id);

        return ResponseEntity.ok(
                ApiResponse.success(message, null));
    }

    // ─── API 9: SEARCH USERS ─────────────────────────────
    // GET /api/users/search?q=rahul&page=0&size=10
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<UserResponse> users =
                userService.searchUsers(q, page, size);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Search results fetched!", users));
    }

    // ─── API 10: USER ACTIVITY ────────────────────────────
    // GET /api/users/{id}/activity
    @GetMapping("/{id}/activity")
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> getUserActivity(
            @PathVariable Long id) {

        UserResponse activity =
                userService.getUserActivity(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User activity fetched!", activity));
    }

    // ─── API 11: BULK IMPORT ─────────────────────────────
    // POST /api/users/bulk-import
    @PostMapping("/bulk-import")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> bulkImport(
            @RequestBody List<RegisterRequest> users) {

        String message = userService.bulkImportUsers(users);

        return ResponseEntity.ok(
                ApiResponse.success(message, null));
    }
}