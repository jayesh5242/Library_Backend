package com.example.Library_backend.service;

import com.example.Library_backend.dto.request.IssueBookRequest;
import com.example.Library_backend.dto.request.ReturnBookRequest;
import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.BorrowResponse;
import com.example.Library_backend.dto.response.authresponse.PagedResponse;
import com.example.Library_backend.enums.TransactionStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import com.example.Library_backend.entity.*;
import com.example.Library_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class BorrowTransactionService {

    private final BorrowTransactionRepository borrowRepo;
    private final BookRepository bookRepo;
    private final HelperService helperService;
    private final UserRepository userRepo;

    public ApiResponse<BorrowResponse> issueBook(IssueBookRequest request) {
        try {
            Book book = bookRepo.findById(request.getBookId())
                    .orElseThrow(() -> new RuntimeException("Book not found"));

            User user = userRepo.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!book.getAvailable()) {
                throw new RuntimeException("Book not available");
            }

            BorrowTransaction txn = BorrowTransaction.builder()
                    .book(book)
                    .user(user)
                    .issueDate(LocalDate.now())
                    .dueDate(LocalDate.now().plusDays(14))
                    .status(TransactionStatus.BORROWED)
                    .build();

            book.setAvailable(false);

            borrowRepo.save(txn);
            bookRepo.save(book);

            return new ApiResponse<>(true, "Book issued successfully", mapToResponse(txn));
        } catch (Exception e) {

            e.printStackTrace();

            return new ApiResponse<>(
                    false,
                    "Failed to fetch issuing book",
                    null
            );
        }
    }

    // ---------------- RETURN BOOK ----------------
    public ApiResponse<BorrowResponse> returnBook(ReturnBookRequest request) {
        try {
            BorrowTransaction txn = borrowRepo.findById(request.getTransactionId())
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            txn.setReturnDate(LocalDate.now());
            txn.setStatus(TransactionStatus.RETURNED);

            Book book = txn.getBook();
            book.setAvailable(true);

            borrowRepo.save(txn);
            bookRepo.save(book);

            return new ApiResponse<>(true, "Book returned successfully", mapToResponse(txn));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to return book", null);
        }
    }

    // ---------------- RENEW BOOK ----------------
    public ApiResponse<BorrowResponse> renewBook(Long id) {
        try {
            BorrowTransaction txn = borrowRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            if (txn.getStatus() != TransactionStatus.BORROWED) {
                throw new RuntimeException("Only borrowed books can be renewed");
            }

            txn.setDueDate(txn.getDueDate().plusDays(7));

            borrowRepo.save(txn);

            return new ApiResponse<>(true, "Book renewed successfully", mapToResponse(txn));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to renew book", null);
        }
    }

    // ---------------- MY CURRENT BORROWED BOOKS ----------------
    public ApiResponse<PagedResponse<BorrowResponse>> getMyBorrowedBooks(Pageable pageable, Long userId) {
        try {

            PagedResponse<BorrowResponse> data = helperService.toPagedResponse(
                    borrowRepo
                            .findByUserIdAndStatus(userId, TransactionStatus.BORROWED, pageable)
                            .map(this::mapToResponse),
                    "Fetched borrowed books successfully"
            );
            return new ApiResponse<>(true, "Fetched borrowed books successfully", data);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch borrowed books", null);
        }
    }

    // ---------------- MY BORROW HISTORY ----------------
    public ApiResponse<PagedResponse<BorrowResponse>> getMyBorrowHistory(Pageable pageable, Long userId) {
        try {

            PagedResponse<BorrowResponse> data = helperService.toPagedResponse(
                    borrowRepo
                            .findByUserId(userId, pageable)
                            .map(this::mapToResponse),
                    "Fetched borrow history successfully"
            );

            return new ApiResponse<>(true, "Fetched borrow history successfully", data);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch borrow history", null);
        }
    }
    // ---------------- ALL TRANSACTIONS ----------------
    public ApiResponse<PagedResponse<BorrowResponse>> getAllTransactions(Pageable pageable) {
        try {
            PagedResponse<BorrowResponse> data = helperService.toPagedResponse(
                    borrowRepo.findAll(pageable)
                            .map(this::mapToResponse),
                    "Fetched all transactions successfully"
            );
            return new ApiResponse<>(true, "Fetched transactions successfully", data);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch transactions", null);
        }
    }

    // ---------------- ALL OVERDUE ----------------
    public ApiResponse<PagedResponse<BorrowResponse>> getAllOverdue(Pageable pageable) {
        try {

            PagedResponse<BorrowResponse> data = helperService.toPagedResponse(
                    borrowRepo
                            .findOverdue(LocalDate.now(), pageable)
                            .map(this::mapToResponse),
                    "Fetched overdue books successfully"
            );

            return new ApiResponse<>(true, "Fetched overdue books successfully", data);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch overdue books", null);
        }
    }

    // ---------------- OVERDUE BY BRANCH ----------------
    public ApiResponse<PagedResponse<BorrowResponse>> getOverdueByBranch(Long branchId, Pageable pageable) {
        try {

            PagedResponse<BorrowResponse> data = helperService.toPagedResponse(
                    borrowRepo
                            .findOverdueByBranch(branchId, LocalDate.now(), pageable)
                            .map(this::mapToResponse),
                    "Fetched branch overdue books successfully"
            );

            return new ApiResponse<>(true, "Fetched branch overdue books successfully", data);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch branch overdue books", null);
        }
    }

    // ---------------- GET BY ID ----------------
    public ApiResponse<BorrowResponse> getTransactionById(Long id) {
        try {
            BorrowTransaction txn = borrowRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            return new ApiResponse<>(true, "Fetched transaction successfully", mapToResponse(txn));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch transaction", null);
        }
    }

    // ---------------- MARK AS LOST ----------------
    public ApiResponse<BorrowResponse> markAsLost(Long id) {
        try {
            BorrowTransaction txn = borrowRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            txn.setStatus(TransactionStatus.LOST);

            borrowRepo.save(txn);

            return new ApiResponse<>(true, "Book marked as lost successfully", mapToResponse(txn));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to mark book as lost", null);
        }
    }

    // ---------------- USER HISTORY ----------------
    public ApiResponse<PagedResponse<BorrowResponse>> getUserHistory(Long userId, Pageable pageable) {
        try {

            PagedResponse<BorrowResponse> data = helperService.toPagedResponse(
                    borrowRepo
                            .findByUserId(userId, pageable)
                            .map(this::mapToResponse),
                    "Fetched user history successfully"
            );

            return new ApiResponse<>(true, "Fetched user history successfully", data);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch user history", null);
        }
    }
    private BorrowResponse mapToResponse(BorrowTransaction txn) {
        return BorrowResponse.builder()
                .id(txn.getId())
                .bookId(txn.getBook().getId())
                .bookTitle(txn.getBook().getTitle())
                .userId(txn.getUser().getId())
                .userName(txn.getUser().getFullName())
                .issueDate(txn.getIssueDate())
                .dueDate(txn.getDueDate())
                .returnDate(txn.getReturnDate())
                .status(txn.getStatus().name())
                .build();
    }

    // Replace with Spring Security later
    private Long getLoggedInUserId() {
        return 1L; // TODO: fetch from SecurityContext
    }
}
