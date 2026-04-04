package com.example.Library_backend.service;

import com.example.Library_backend.dto.request.CreateReservationRequest;
import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.ReservationResponse;
import com.example.Library_backend.dto.response.PageResponse;
import com.example.Library_backend.entity.Book;
import com.example.Library_backend.entity.BookInventory;
import com.example.Library_backend.entity.BorrowTransaction;
import com.example.Library_backend.entity.Branch;
import com.example.Library_backend.entity.Reservation;
import com.example.Library_backend.entity.User;
import com.example.Library_backend.enums.ReservationStatus;
import com.example.Library_backend.enums.TransactionStatus;
import com.example.Library_backend.repository.BookInventoryRepository;
import com.example.Library_backend.repository.BookRepository;
import com.example.Library_backend.repository.BorrowTransactionRepository;
import com.example.Library_backend.repository.ReservationRepository;
import com.example.Library_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final BookInventoryRepository bookInventoryRepo;
    private final BorrowTransactionRepository borrowRepo;
    private final HelperService helperService;

    public ApiResponse<ReservationResponse> createReservation(CreateReservationRequest request) {
        try {
            Book book = bookRepo.findById(request.getBookId())
                    .orElseThrow(() -> new RuntimeException("Book not found"));

            User user = userRepo.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Get branch from book inventory
            List<BookInventory> inventories = bookInventoryRepo.findByBookId(book.getId());
            if (inventories.isEmpty()) {
                return new ApiResponse<>(false, "No inventory found for this book", null);
            }
            Branch branch = inventories.get(0).getBranch();

            Reservation reservation = Reservation.builder()
                    .book(book)
                    .user(user)
                    .branch(branch)
                    .status(ReservationStatus.PENDING)
                    .reservedAt(LocalDateTime.now())
                    .expiryDate(LocalDate.now().plusDays(3))
                    .build();

            reservationRepo.save(reservation);

            return new ApiResponse<>(true, "Reservation created successfully", mapToResponse(reservation));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to create reservation: " + e.getMessage(), null);
        }
    }

    public ApiResponse<PageResponse<ReservationResponse>> getMyReservations(Pageable pageable, Long userId) {
        try {
            Page<ReservationResponse> data = reservationRepo
                    .findByUserId(userId, pageable)
                    .map(this::mapToResponse);
            return new ApiResponse<>(true, "Fetched reservations successfully", helperService.toPageResponse(data));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch reservations: " + e.getMessage(), null);
        }
    }

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

    public ApiResponse<PageResponse<ReservationResponse>> getAllReservations(Pageable pageable) {
        try {
            Page<ReservationResponse> data = reservationRepo.findAllWithDetails(pageable)
                    .map(this::mapToResponse);
            return new ApiResponse<>(true, "Fetched all reservations", helperService.toPageResponse(data));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch reservations: " + e.getMessage(), null);
        }
    }

    public ApiResponse<PageResponse<ReservationResponse>> getPendingByBranch(Long branchId, Pageable pageable) {
        try {
            Page<ReservationResponse> data = reservationRepo
                    .findPendingByBranch(branchId, pageable)
                    .map(this::mapToResponse);

            return new ApiResponse<>(true, "Fetched pending reservations", helperService.toPageResponse(data));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch pending reservations", null);
        }
    }

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

    public ApiResponse<ReservationResponse> markCollected(Long id) {
        try {
            Reservation r = reservationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reservation not found"));

            r.setStatus(ReservationStatus.COLLECTED);
            reservationRepo.save(r);

            // Auto-create BorrowTransaction when book is collected
            Book book = r.getBook();
            List<BookInventory> inventories = bookInventoryRepo.findByBookId(book.getId());
            BookInventory inventory = inventories.stream()
                    .filter(inv -> inv.getAvailableCopies() > 0)
                    .findFirst().orElse(inventories.isEmpty() ? null : inventories.get(0));

            BorrowTransaction txn = BorrowTransaction.builder()
                    .book(book)
                    .user(r.getUser())
                    .branch(r.getBranch())
                    .issueDate(java.time.LocalDate.now())
                    .dueDate(java.time.LocalDate.now().plusDays(14))
                    .status(TransactionStatus.BORROWED)
                    .build();
            borrowRepo.save(txn);

            // Decrement inventory
            if (inventory != null && inventory.getAvailableCopies() > 0) {
                inventory.setAvailableCopies(inventory.getAvailableCopies() - 1);
                bookInventoryRepo.save(inventory);
                if (inventory.getAvailableCopies() == 0) {
                    book.setAvailable(false);
                    bookRepo.save(book);
                }
            }

            return new ApiResponse<>(true, "Marked as collected and book issued", mapToResponse(r));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to mark collected", null);
        }
    }

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

    public ApiResponse<PageResponse<ReservationResponse>> getByBook(Long bookId, Pageable pageable) {
        try {
            Page<ReservationResponse> data = reservationRepo
                    .findByBookId(bookId, pageable)
                    .map(this::mapToResponse);

            return new ApiResponse<>(true, "Fetched book reservations", helperService.toPageResponse(data));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch reservations", null);
        }
    }

    private ReservationResponse mapToResponse(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .bookId(r.getBook().getId())
                .userId(r.getUser().getId())
                .bookName(r.getBook().getTitle())   // adjust field name if different
                .userName(r.getUser().getFullName())    // adjust field name if different
                .status(r.getStatus())
                .reservedAt(r.getReservedAt())
                .expiryDate(r.getExpiryDate())
                .build();
    }

    private Long getLoggedInUserId() {
        return 1L; // replace with actual auth
    }
}
