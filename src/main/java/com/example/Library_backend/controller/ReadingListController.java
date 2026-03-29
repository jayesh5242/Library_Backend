package com.example.Library_backend.controller;

import com.example.Library_backend.dto.request.ReadingListRequest;
import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.ReadingListResponse;
import com.example.Library_backend.service.FacultyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading-lists")
@RequiredArgsConstructor
@Tag(name = "Reading Lists", description = "Faculty reading list management")
public class ReadingListController {

    private final FacultyService facultyService;

    // ─────────────────────────────────────────────────────────
    // API 1: GET /api/reading-lists
    // Browse all public reading lists
    // Access: Public
    //
    // Example: GET /api/reading-lists
    // Response: List of all public reading lists
    // ─────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Browse all public reading lists")
    public ResponseEntity<ApiResponse<List<ReadingListResponse>>> getAllPublicReadingLists() {
        return ResponseEntity.ok(
                ApiResponse.success("Reading lists fetched successfully",
                        facultyService.getAllPublicReadingLists()));
    }

    // ─────────────────────────────────────────────────────────
    // API 2: GET /api/reading-lists/{id}
    // Get reading list with all books
    // Access: Public
    //
    // Example: GET /api/reading-lists/1
    // Response: Reading list details with full book list
    // ─────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(summary = "Get reading list with all books")
    public ResponseEntity<ApiResponse<ReadingListResponse>> getReadingListById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Reading list fetched successfully",
                        facultyService.getReadingListById(id)));
    }

    // ─────────────────────────────────────────────────────────
    // API 3: GET /api/reading-lists/my
    // Faculty's own reading lists
    // Access: Faculty only
    //
    // Example: GET /api/reading-lists/my
    // Response: All reading lists created by logged-in faculty
    // ─────────────────────────────────────────────────────────
    @GetMapping("/my")
    @Operation(summary = "Get faculty's own reading lists",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<List<ReadingListResponse>>> getMyReadingLists(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("My reading lists fetched",
                        facultyService.getMyReadingLists(
                                userDetails.getUsername())));
    }

    // ─────────────────────────────────────────────────────────
    // API 4: POST /api/reading-lists
    // Create a new reading list
    // Access: Faculty only
    //
    // Request Body:
    // { "title": "Java Fundamentals", "subject": "CS101",
    //   "semester": "Sem 3", "description": "...", "isPublic": true }
    // Response (201): Created reading list
    // ─────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new reading list",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<ReadingListResponse>> createReadingList(
            @Valid @RequestBody ReadingListRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reading list created successfully",
                        facultyService.createReadingList(
                                request, userDetails.getUsername())));
    }

    // ─────────────────────────────────────────────────────────
    // API 5: PUT /api/reading-lists/{id}
    // Update reading list details
    // Access: Faculty only (own list)
    //
    // Example: PUT /api/reading-lists/1
    // Request Body: { "title": "Updated Title", "subject": "CS102" }
    // Response (200): Updated reading list
    // ─────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @Operation(summary = "Update reading list details",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<ReadingListResponse>> updateReadingList(
            @PathVariable Long id,
            @Valid @RequestBody ReadingListRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Reading list updated successfully",
                        facultyService.updateReadingList(
                                id, request, userDetails.getUsername())));
    }

    // ─────────────────────────────────────────────────────────
    // API 6: DELETE /api/reading-lists/{id}
    // Delete a reading list
    // Access: Faculty (own) or Admin
    //
    // Example: DELETE /api/reading-lists/1
    // Response (200): { "success": true, "message": "Reading list deleted" }
    // ─────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a reading list",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('FACULTY', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReadingList(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        facultyService.deleteReadingList(id, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Reading list deleted successfully", null));
    }

    // ─────────────────────────────────────────────────────────
    // API 7: POST /api/reading-lists/{id}/books
    // Add book to reading list
    // Access: Faculty only (own list)
    //
    // Example: POST /api/reading-lists/1/books?bookId=5
    // Response (200): Updated reading list with books
    // ─────────────────────────────────────────────────────────
    @PostMapping("/{id}/books")
    @Operation(summary = "Add book to reading list",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<ReadingListResponse>> addBookToList(
            @PathVariable Long id,
            @RequestParam Long bookId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Book added to reading list",
                        facultyService.addBookToReadingList(
                                id, bookId, userDetails.getUsername())));
    }

    // ─────────────────────────────────────────────────────────
    // API 8: DELETE /api/reading-lists/{id}/books/{bookId}
    // Remove book from reading list
    // Access: Faculty only (own list)
    //
    // Example: DELETE /api/reading-lists/1/books/5
    // Response (200): Updated reading list without removed book
    // ─────────────────────────────────────────────────────────
    @DeleteMapping("/{id}/books/{bookId}")
    @Operation(summary = "Remove book from reading list",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<ReadingListResponse>> removeBookFromList(
            @PathVariable Long id,
            @PathVariable Long bookId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Book removed from reading list",
                        facultyService.removeBookFromReadingList(
                                id, bookId, userDetails.getUsername())));
    }
}

