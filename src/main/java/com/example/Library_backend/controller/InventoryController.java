package com.example.Library_backend.controller;

import com.example.Library_backend.dto.request.InventoryRequest;
import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.InventoryResponse;
import com.example.Library_backend.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Book inventory management per branch")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;

    // ─────────────────────────────────────────────────────────
    // API 1: GET /api/inventory
    // All inventory across all branches
    // Access: Librarian, Admin
    //
    // Example: GET /api/inventory
    // Response: List of all inventory records across all branches
    // ─────────────────────────────────────────────────────────
    @GetMapping
    @Operation(summary = "Get all inventory across all branches")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAllInventory() {

        return ResponseEntity.ok(
                ApiResponse.success("Inventory fetched successfully",
                        inventoryService.getAllInventory()));
    }

    // ─────────────────────────────────────────────────────────
    // API 2: GET /api/inventory/branch/{id}
    // Inventory for a specific branch
    // Access: Librarian, Admin
    //
    // Example: GET /api/inventory/branch/1
    // Response: All inventory records for branch with id 1
    // ─────────────────────────────────────────────────────────
    @GetMapping("/branch/{id}")
    @Operation(summary = "Get inventory for a specific branch")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoryByBranch(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success("Branch inventory fetched successfully",
                        inventoryService.getInventoryByBranch(id)));
    }

    // ─────────────────────────────────────────────────────────
    // API 3: POST /api/inventory
    // Add book copies to a branch
    // Access: Librarian, Admin
    //
    // Request Body:
    // {
    //   "bookId": 1,
    //   "branchId": 1,
    //   "totalCopies": 5,
    //   "availableCopies": 5,
    //   "shelfLocation": "Row A-3",
    //   "condition": "GOOD"
    // }
    // Response (201): Created inventory record
    // ─────────────────────────────────────────────────────────
    @PostMapping
    @Operation(summary = "Add book copies to a branch")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> addInventory(
            @Valid @RequestBody InventoryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory added successfully",
                        inventoryService.addInventory(request)));
    }

    // ─────────────────────────────────────────────────────────
    // API 4: PUT /api/inventory/{id}
    // Update copy count or shelf location
    // Access: Librarian, Admin
    //
    // Example: PUT /api/inventory/1
    // Request Body:
    // {
    //   "totalCopies": 8,
    //   "availableCopies": 6,
    //   "shelfLocation": "Row B-1"
    // }
    // Response (200): Updated inventory record
    // ─────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @Operation(summary = "Update copy count or shelf location")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateInventory(
            @PathVariable Long id,
            @RequestBody InventoryRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Inventory updated successfully",
                        inventoryService.updateInventory(id, request)));
    }

    // ─────────────────────────────────────────────────────────
    // API 5: DELETE /api/inventory/{id}
    // Remove book from branch inventory
    // Access: Admin only
    //
    // Example: DELETE /api/inventory/1
    // Response (200): { "success": true, "message": "Inventory removed", "data": null }
    // ─────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove book from branch inventory — Admin only")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeInventory(
            @PathVariable Long id) {

        inventoryService.removeInventory(id);

        return ResponseEntity.ok(
                ApiResponse.success("Inventory removed successfully", null));
    }

    // ─────────────────────────────────────────────────────────
    // API 6: PUT /api/inventory/{id}/condition
    // Update book physical condition
    // Access: Librarian only
    //
    // Example: PUT /api/inventory/1/condition?condition=FAIR
    // Valid values: GOOD, FAIR, POOR, DAMAGED
    // Response (200): Updated inventory record
    // ─────────────────────────────────────────────────────────
    @PutMapping("/{id}/condition")
    @Operation(summary = "Update book physical condition")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateCondition(
            @PathVariable Long id,
            @RequestParam String condition) {

        return ResponseEntity.ok(
                ApiResponse.success("Condition updated successfully",
                        inventoryService.updateCondition(id, condition)));
    }

    // ─────────────────────────────────────────────────────────
    // API 7: GET /api/inventory/low-stock
    // Books with only 1 copy left
    // Access: Librarian, Admin
    //
    // Example: GET /api/inventory/low-stock
    // Response: List of inventory records with availableCopies <= 1
    // ─────────────────────────────────────────────────────────
    @GetMapping("/low-stock")
    @Operation(summary = "Get books with only 1 copy left")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStockBooks() {

        return ResponseEntity.ok(
                ApiResponse.success("Low stock books fetched successfully",
                        inventoryService.getLowStockBooks()));
    }

    // ─────────────────────────────────────────────────────────
    // API 8: GET /api/inventory/out-of-stock
    // Books with 0 available copies
    // Access: Librarian, Admin
    //
    // Example: GET /api/inventory/out-of-stock
    // Response: List of inventory records with availableCopies = 0
    // ─────────────────────────────────────────────────────────
    @GetMapping("/out-of-stock")
    @Operation(summary = "Get books with 0 available copies")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getOutOfStockBooks() {

        return ResponseEntity.ok(
                ApiResponse.success("Out of stock books fetched successfully",
                        inventoryService.getOutOfStockBooks()));
    }
}