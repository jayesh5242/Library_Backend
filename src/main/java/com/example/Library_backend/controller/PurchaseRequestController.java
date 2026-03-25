package com.example.Library_backend.controller;

import com.example.Library_backend.dto.request.PurchaseRequestRequest;
import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.PurchaseRequestResponse;
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
@RequestMapping("/api/purchase-requests")
@RequiredArgsConstructor
@Tag(name = "Purchase Requests", description = "Book purchase request management")
@SecurityRequirement(name = "bearerAuth")
public class PurchaseRequestController {
 
    private final FacultyService facultyService;
 
    // ─────────────────────────────────────────────────────────
    // API 9: GET /api/purchase-requests/my
    // Faculty's own purchase requests
    // Access: Faculty only
    //
    // Example: GET /api/purchase-requests/my
    // Response: All purchase requests by logged-in faculty
    // ─────────────────────────────────────────────────────────
    @GetMapping("/my")
    @Operation(summary = "Get faculty's own purchase requests")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<List<PurchaseRequestResponse>>> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
 
        return ResponseEntity.ok(
                ApiResponse.success("Purchase requests fetched",
                        facultyService.getMyPurchaseRequests(
                                userDetails.getUsername())));
    }
 
    // ─────────────────────────────────────────────────────────
    // API 10: POST /api/purchase-requests
    // Submit a book purchase request
    // Access: Faculty only
    //
    // Request Body:
    // { "bookTitle": "Design Patterns", "author": "GoF",
    //   "isbn": "978-0201633610", "reason": "Required for CS301",
    //   "priority": "HIGH", "branchId": 1 }
    // Response (201): Created purchase request
    // ─────────────────────────────────────────────────────────
    @PostMapping
    @Operation(summary = "Submit a book purchase request")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> submitRequest(
            @Valid @RequestBody PurchaseRequestRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
 
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase request submitted successfully",
                        facultyService.submitPurchaseRequest(
                                request, userDetails.getUsername())));
    }
 
    // ─────────────────────────────────────────────────────────
    // API 11: GET /api/purchase-requests/all
    // All purchase requests — Librarian/Admin only
    //
    // Example: GET /api/purchase-requests/all
    // Response: All purchase requests with status
    // ─────────────────────────────────────────────────────────
    @GetMapping("/all")
    @Operation(summary = "Get all purchase requests")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<PurchaseRequestResponse>>> getAllRequests() {
 
        return ResponseEntity.ok(
                ApiResponse.success("All purchase requests fetched",
                        facultyService.getAllPurchaseRequests()));
    }
 
    // ─────────────────────────────────────────────────────────
    // API 12: PUT /api/purchase-requests/{id}/approve
    // Approve a purchase request
    // Access: Admin only
    //
    // Example: PUT /api/purchase-requests/1/approve
    // Response (200): Updated request with status APPROVED
    // ─────────────────────────────────────────────────────────
    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a purchase request — Admin only")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> approveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
 
        return ResponseEntity.ok(
                ApiResponse.success("Purchase request approved",
                        facultyService.approvePurchaseRequest(
                                id, userDetails.getUsername())));
    }
 
    // ─────────────────────────────────────────────────────────
    // API 13: PUT /api/purchase-requests/{id}/reject
    // Reject a purchase request with reason
    // Access: Admin only
    //
    // Example: PUT /api/purchase-requests/1/reject?reason=Budget+constraints
    // Response (200): Updated request with status REJECTED
    // ─────────────────────────────────────────────────────────
    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a purchase request with reason — Admin only")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> rejectRequest(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails) {
 
        return ResponseEntity.ok(
                ApiResponse.success("Purchase request rejected",
                        facultyService.rejectPurchaseRequest(
                                id, reason, userDetails.getUsername())));
    }
}