package com.example.Library_backend.controller;

import com.example.Library_backend.dto.request.CreateReservationRequest;
import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.ReservationResponse;
import com.example.Library_backend.dto.response.authresponse.PagedResponse;
import com.example.Library_backend.service.CurrentUserService;
import com.example.Library_backend.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> create(@RequestBody CreateReservationRequest request) {
        try {
            return ResponseEntity.ok(reservationService.createReservation(request));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed to create reservation", null));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PagedResponse<ReservationResponse>>> my(Pageable pageable) {
        try {
            Long userId = currentUserService.getCurrentUserId();
            return ResponseEntity.ok(reservationService.getMyReservations(pageable,userId));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed to fetch reservations", null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(reservationService.cancelReservation(id));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed to cancel reservation", null));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PagedResponse<ReservationResponse>>> all(Pageable pageable) {
        try {
            return ResponseEntity.ok(reservationService.getAllReservations(pageable));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed to fetch reservations", null));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PagedResponse<ReservationResponse>>> pending(@RequestParam Long branchId, Pageable pageable) {
        try {
            return ResponseEntity.ok(reservationService.getPendingByBranch(branchId, pageable));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed to fetch pending reservations", null));
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ReservationResponse>> approve(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(reservationService.approveReservation(id));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed to approve reservation", null));
        }
    }

    @PutMapping("/{id}/ready")
    public ResponseEntity<ApiResponse<ReservationResponse>> ready(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(reservationService.markReady(id));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed to mark ready", null));
        }
    }

    @PutMapping("/{id}/collected")
    public ResponseEntity<ApiResponse<ReservationResponse>> collected(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(reservationService.markCollected(id));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed to mark collected", null));
        }
    }

    @PutMapping("/{id}/expire")
    public ResponseEntity<ApiResponse<ReservationResponse>> expire(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(reservationService.expireReservation(id));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed to expire reservation", null));
        }
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<ApiResponse<PagedResponse<ReservationResponse>>> byBook(@PathVariable Long bookId, Pageable pageable) {
        try {
            return ResponseEntity.ok(reservationService.getByBook(bookId, pageable));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Failed to fetch book reservations", null));
        }
    }
}