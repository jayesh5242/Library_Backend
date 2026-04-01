package com.example.Library_backend.service;

import com.example.Library_backend.dto.request.CreateReservationRequest;
import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.ReservationResponse;
import com.example.Library_backend.dto.response.authresponse.PagedResponse;
import com.example.Library_backend.entity.Book;
import com.example.Library_backend.entity.Reservation;
import com.example.Library_backend.entity.User;
import com.example.Library_backend.enums.ReservationStatus;
import com.example.Library_backend.repository.BookRepository;
import com.example.Library_backend.repository.ReservationRepository;
import com.example.Library_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final HelperService helperService;
    private final CurrentUserService currentUserService;

    // ---------------- CREATE ----------------
    public ApiResponse<ReservationResponse> createReservation(CreateReservationRequest request) {
        try {
            Book book = bookRepo.findById(request.getBookId())
                    .orElseThrow(() -> new RuntimeException("Book not found"));

            User user = userRepo.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Reservation reservation = Reservation.builder()
                    .book(book)
                    .user(user)
                    .status(ReservationStatus.PENDING)
                    .reservedAt(LocalDateTime.now())
                    .expiryDate(LocalDate.now().plusDays(3))
                    .build();

            reservationRepo.save(reservation);

            return new ApiResponse<>(true, "Reservation created successfully", mapToResponse(reservation));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to create reservation", null);
        }
    }

    // ---------------- MY RESERVATIONS ----------------
    public ApiResponse<PagedResponse<ReservationResponse>> getMyReservations(Pageable pageable, Long userId) {
        try {
            return new ApiResponse<>(true, "Fetched reservations successfully",
                    toPaged(reservationRepo.findByUserId(userId, pageable), "Fetched reservations successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch reservations", null);
        }
    }

    // ---------------- CANCEL ----------------
    public ApiResponse<Void> cancelReservation(Long id) {
        try {
            Reservation r = reservationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reservation not found"));

            r.setStatus(ReservationStatus.CANCELLED);
            reservationRepo.save(r);

            return new ApiResponse<>(true, "Reservation cancelled", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to cancel reservation", null);
        }
    }

    // ---------------- ALL ----------------
    public ApiResponse<PagedResponse<ReservationResponse>> getAllReservations(Pageable pageable) {
        try {
            return new ApiResponse<>(true, "Fetched all reservations",
                    toPaged(reservationRepo.findAll(pageable), "Fetched all reservations"));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch reservations", null);
        }
    }

    // ---------------- PENDING BY BRANCH ----------------
    public ApiResponse<PagedResponse<ReservationResponse>> getPendingByBranch(Long branchId, Pageable pageable) {
        try {
            return new ApiResponse<>(true, "Fetched pending reservations",
                    toPaged(reservationRepo.findPendingByBranch(branchId, pageable), "Fetched pending reservations"));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch pending reservations", null);
        }
    }

    // ---------------- APPROVE ----------------
    public ApiResponse<ReservationResponse> approveReservation(Long id) {
        try {
            Reservation r = reservationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reservation not found"));

            r.setStatus(ReservationStatus.APPROVED);
            reservationRepo.save(r);

            return new ApiResponse<>(true, "Reservation approved", mapToResponse(r));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to approve reservation", null);
        }
    }

    // ---------------- READY ----------------
    public ApiResponse<ReservationResponse> markReady(Long id) {
        try {
            Reservation r = reservationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reservation not found"));

            r.setStatus(ReservationStatus.READY);
            reservationRepo.save(r);

            return new ApiResponse<>(true, "Marked as ready", mapToResponse(r));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to mark ready", null);
        }
    }

    // ---------------- COLLECTED ----------------
    public ApiResponse<ReservationResponse> markCollected(Long id) {
        try {
            Reservation r = reservationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reservation not found"));

            r.setStatus(ReservationStatus.COLLECTED);
            reservationRepo.save(r);

            return new ApiResponse<>(true, "Marked as collected", mapToResponse(r));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to mark collected", null);
        }
    }

    // ---------------- EXPIRE ----------------
    public ApiResponse<ReservationResponse> expireReservation(Long id) {
        try {
            Reservation r = reservationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reservation not found"));

            r.setStatus(ReservationStatus.EXPIRED);
            reservationRepo.save(r);

            return new ApiResponse<>(true, "Reservation expired", mapToResponse(r));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to expire reservation", null);
        }
    }

    // ---------------- BY BOOK ----------------
    public ApiResponse<PagedResponse<ReservationResponse>> getByBook(Long bookId, Pageable pageable) {
        try {
            return new ApiResponse<>(true, "Fetched book reservations",
                    toPaged(reservationRepo.findByBookId(bookId, pageable), "Fetched book reservations"));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch reservations", null);
        }
    }

    // ---------------- PAGINATION HELPER ----------------
    private PagedResponse<ReservationResponse> toPaged(Page<Reservation> page, String msg) {
        return helperService.toPagedResponse(
                page.map(this::mapToResponse),
                msg
        );
    }

    // ---------------- MAPPER ----------------
    private ReservationResponse mapToResponse(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .bookId(r.getBook().getId())
                .userId(r.getUser().getId())
                .bookName(r.getBook().getTitle())
                .userName(r.getUser().getFullName())
                .status(r.getStatus())
                .reservedAt(r.getReservedAt())
                .expiryDate(r.getExpiryDate())
                .build();
    }

    private Long getLoggedInUserId() {
        return currentUserService.getCurrentUserId(); // replace with actual auth
    }
}