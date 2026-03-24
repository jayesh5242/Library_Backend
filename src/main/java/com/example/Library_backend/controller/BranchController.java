package com.example.Library_backend.controller;

import com.example.Library_backend.dto.request.BranchRequest;
import com.example.Library_backend.dto.response.*;
import com.example.Library_backend.service.BranchService;
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
@RequestMapping("/api/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Library branch management")
public class BranchController {

    private final BranchService branchService;


    @GetMapping
    @Operation(summary = "List all active library branches")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getAllBranches() {

        return ResponseEntity.ok(
                ApiResponse.success("Branches fetched successfully",
                        branchService.getAllActiveBranches()));

    }

    @GetMapping("/{id}")
    @Operation(summary = "Get branch details by ID")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success("Branch fetched successfully",
                        branchService.getBranchById(id)));
    }

    @GetMapping("/{id}/books")
    @Operation(summary = "Get all books available in a branch")
    public ResponseEntity<ApiResponse<PagedResponse<BookResponse>>> getBranchBooks(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                ApiResponse.success("Branch books fetched successfully",
                        branchService.getBooksInBranch(id, page, size)));

    }

    @GetMapping("/{id}/inventory")
    @Operation(
            summary = "Get full inventory of a branch with copy counts",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getBranchInventory(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success("Branch inventory fetched successfully",
                        branchService.getBranchInventory(id)));
    }

    @GetMapping("/{id}/stats")
    @Operation(
            summary = "Get branch statistics and summary",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BranchStatsResponse>> getBranchStats(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success("Branch stats fetched successfully",
                        branchService.getBranchStats(id)));
    }

    @PostMapping
    @Operation(
            summary = "Create a new library branch — Super Admin only",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @Valid @RequestBody BranchRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Branch created successfully",
                        branchService.createBranch(request)));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update branch information — Super Admin only",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Branch updated successfully",
                        branchService.updateBranch(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deactivate a branch — Super Admin only",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateBranch(
            @PathVariable Long id) {

        branchService.deactivateBranch(id);

        return ResponseEntity.ok(
                ApiResponse.success("Branch deactivated successfully", null));

    }

    @PutMapping("/{id}/librarian")
    @Operation(
            summary = "Assign a librarian to a branch — Super Admin only",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> assignLibrarian(
            @PathVariable Long id,
            @RequestParam Long librarianId) {

        return ResponseEntity.ok(
                ApiResponse.success("Librarian assigned successfully",
                        branchService.assignLibrarian(id, librarianId)));

    }

    @GetMapping("/{id}/overdue")
    @Operation(
            summary = "Get all overdue books in a branch",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<OverdueResponse>>> getOverdueBooks(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success("Overdue books fetched successfully",
                        branchService.getOverdueBooks(id)));
    }
}
