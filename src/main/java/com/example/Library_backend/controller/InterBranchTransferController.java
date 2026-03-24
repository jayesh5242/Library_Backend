package com.example.Library_backend.controller;

import com.example.Library_backend.dto.request.CreateTransferRequest;
import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.PageResponse;
import com.example.Library_backend.dto.response.TransferResponse;
import com.example.Library_backend.service.CurrentUserService;
import com.example.Library_backend.service.InterBranchTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class InterBranchTransferController {

    private final InterBranchTransferService service;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransferResponse>> create(@RequestBody CreateTransferRequest req) {
        try {
            return ResponseEntity.ok(service.create(req));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed", null));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<TransferResponse>>> my(Pageable pageable) {
        try {
            Long userId = currentUserService.getCurrentUserId();
            return ResponseEntity.ok(service.my(pageable,userId));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed", null));
        }
    }

    @GetMapping("/outgoing/{branchId}")
    public ResponseEntity<ApiResponse<PageResponse<TransferResponse>>> outgoing(@PathVariable Long branchId, Pageable pageable) {
        try {
            return ResponseEntity.ok(service.outgoing(branchId, pageable));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed", null));
        }
    }

    @GetMapping("/incoming/{branchId}")
    public ResponseEntity<ApiResponse<PageResponse<TransferResponse>>> incoming(@PathVariable Long branchId, Pageable pageable) {
        try {
            return ResponseEntity.ok(service.incoming(branchId, pageable));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed", null));
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<TransferResponse>> approve(@PathVariable Long id) {
        try {
            Long userId = currentUserService.getCurrentUserId();
            return ResponseEntity.ok(service.approve(id,userId));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed", null));
        }
    }

    @PutMapping("/{id}/dispatch")
    public ResponseEntity<ApiResponse<TransferResponse>> dispatch(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.dispatch(id));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed", null));
        }
    }

    @PutMapping("/{id}/receive")
    public ResponseEntity<ApiResponse<TransferResponse>> receive(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.receive(id));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed", null));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<TransferResponse>> reject(@PathVariable Long id,
                                                                @RequestParam String reason) {
        try {
            return ResponseEntity.ok(service.reject(id, reason));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed", null));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<TransferResponse>>> all(Pageable pageable) {
        try {
            return ResponseEntity.ok(service.all(pageable));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed", null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransferResponse>> get(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.get(id));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed", null));
        }
    }
}
