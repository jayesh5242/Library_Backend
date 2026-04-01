package com.example.Library_backend.controller;

import com.example.Library_backend.dto.request.PartialPayRequest;
import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.FineResponse;
import com.example.Library_backend.dto.response.PageResponse;
import com.example.Library_backend.dto.response.authresponse.PagedResponse;
import com.example.Library_backend.service.CurrentUserService;
import com.example.Library_backend.service.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineService service;
    private final CurrentUserService currentUserService;
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PagedResponse<FineResponse>>> my(Pageable pageable) {
        try {
            Long userId = currentUserService.getCurrentUserId();
            return ResponseEntity.ok(service.my(pageable, userId));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<PagedResponse<FineResponse>>builder()
                    .success(false)
                    .message("Failed to fetch user fines")
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/my/total")
    public ResponseEntity<ApiResponse<Double>> total() {
        try {
            Long userId = currentUserService.getCurrentUserId();
            return ResponseEntity.ok(service.myTotal(userId));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Double>builder()
                    .success(false)
                    .message("Failed to fetch total fines")
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PagedResponse<FineResponse>>> all(Pageable pageable) {
        try {
            return ResponseEntity.ok(service.all(pageable));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<PagedResponse<FineResponse>>builder()
                    .success(false)
                    .message("Failed to fetch all fines")
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PagedResponse<FineResponse>>> pending(Pageable pageable) {
        try {
            return ResponseEntity.ok(service.pending(pageable));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<PagedResponse<FineResponse>>builder()
                    .success(false)
                    .message("Failed to fetch pending fines")
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/branch/{id}")
    public ResponseEntity<ApiResponse<PagedResponse<FineResponse>>> branch(@PathVariable Long id, Pageable pageable) {
        try {
            return ResponseEntity.ok(service.byBranch(id, pageable));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<PagedResponse<FineResponse>>builder()
                    .success(false)
                    .message("Failed to fetch branch fines")
                    .data(null)
                    .build());
        }
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<FineResponse>> pay(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.pay(id));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<FineResponse>builder()
                    .success(false)
                    .message("Failed to pay fine")
                    .data(null)
                    .build());
        }
    }

    @PutMapping("/{id}/waive")
    public ResponseEntity<ApiResponse<FineResponse>> waive(@PathVariable Long id,
                                                           @RequestParam String reason) {
        try {
            Long userId = currentUserService.getCurrentUserId();
            return ResponseEntity.ok(service.waive(id, reason, userId));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<FineResponse>builder()
                    .success(false)
                    .message("Failed to waive fine")
                    .data(null)
                    .build());
        }
    }

    @PutMapping("/{id}/partial-pay")
    public ResponseEntity<ApiResponse<FineResponse>> partialPay(@PathVariable Long id,
                                                                @RequestBody PartialPayRequest req) {
        try {
            return ResponseEntity.ok(service.partialPay(id, req.getAmount()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<FineResponse>builder()
                    .success(false)
                    .message("Failed to process partial payment")
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<FineResponse>>> user(@PathVariable Long userId, Pageable pageable) {
        try {
            return ResponseEntity.ok(service.byUser(userId, pageable));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<PagedResponse<FineResponse>>builder()
                    .success(false)
                    .message("Failed to fetch user fines")
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Double>>> summary() {
        try {
            return ResponseEntity.ok(service.summary());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Map<String, Double>>builder()
                    .success(false)
                    .message("Failed to fetch fine summary")
                    .data(null)
                    .build());
        }
    }
}
