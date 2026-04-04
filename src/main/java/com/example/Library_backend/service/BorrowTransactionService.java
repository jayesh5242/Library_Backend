package com.example.Library_backend.service;

import com.example.Library_backend.dto.request.IssueBookRequest;
import com.example.Library_backend.dto.request.ReturnBookRequest;
import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.BorrowResponse;
import com.example.Library_backend.dto.response.PageResponse;
import com.example.Library_backend.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.Library_backend.entity.*;
import com.example.Library_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowTransactionService {

    private final BorrowTransactionRepository borrowRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final BookInventoryRepository bookInventoryRepo;
    private final HelperService helperService;

    public ApiResponse<?> issueBook(IssueBookRequest request) {
        try{

            if (!bookRepo.existsById(request.getBookId())) {
                return new ApiResponse<>(false, "Book not found", null);
            }
            if (!userRepo.existsById(request.getUserId())) {
                return new ApiResponse<>(false, "User not found", null);
            }
            Book book = bookRepo.findById(request.getBookId()).get();

            User user = userRepo.findById(request.getUserId()).get();

            // Get branch from book inventory (user may not have a branch assigned)
            List<BookInventory> inventories = bookInventoryRepo.findByBookId(book.getId());
            if (inventories.isEmpty()) {
                return new ApiResponse<>(false, "No inventory found for this book", null);
            }
            BookInventory inventory = inventories.stream()
                    .filter(inv -> inv.getAvailableCopies() > 0)
                    .findFirst()
                    .orElse(null);
            if (inventory == null) {
                return new ApiResponse<>(false, "Book not available", null);
            }

            if (inventory != null) {
                BorrowTransaction txn = BorrowTransaction.builder()
                        .book(book)
                        .user(user)
                        .issueDate(LocalDate.now())
                        .dueDate(LocalDate.now().plusDays(14))
                        .status(TransactionStatus.BORROWED)
                        .branch(inventory.getBranch())
                        .build();

                inventory.setAvailableCopies(inventory.getAvailableCopies() - 1);
                if (inventory.getAvailableCopies() == 0) book.setAvailable(false);
                borrowRepo.save(txn);
                bookRepo.save(book);
                bookInventoryRepo.save(inventory);

                return new ApiResponse<>(true, "Book issued successfully", mapToResponse(txn));
            } else {
                return new ApiResponse<>(false, "Book not available", null);
            }
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
            BorrowTransaction txn = borrowRepo.findByIdWithDetails(request.getTransactionId())
                    .orElse(null);
            if (txn == null) return new ApiResponse<>(false, "Transaction not found", null);
            if (txn.getStatus() != TransactionStatus.BORROWED && txn.getStatus() != TransactionStatus.RETURN_REQUESTED)
                return new ApiResponse<>(false, "Book is not in borrowed state", null);

            // Student requests return — does NOT complete it yet
            txn.setStatus(TransactionStatus.RETURN_REQUESTED);
            borrowRepo.save(txn);

            return new ApiResponse<>(true, "Return request submitted. Admin will confirm.", mapToResponse(txn));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to request return: " + e.getMessage(), null);
        }
    }

    // Admin accepts the return
    public ApiResponse<BorrowResponse> acceptReturn(Long id) {
        try {
            BorrowTransaction txn = borrowRepo.findByIdWithDetails(id).orElse(null);
            if (txn == null) return new ApiResponse<>(false, "Transaction not found", null);
            if (txn.getStatus() != TransactionStatus.RETURN_REQUESTED)
                return new ApiResponse<>(false, "No return request pending for this book", null);

            txn.setReturnDate(LocalDate.now());
            txn.setStatus(TransactionStatus.RETURNED);

            Book book = txn.getBook();
            book.setAvailable(true);
            borrowRepo.save(txn);
            bookRepo.save(book);

            bookInventoryRepo.findByBookId(book.getId()).stream()
                    .filter(inv -> inv.getAvailableCopies() < inv.getTotalCopies())
                    .findFirst()
                    .ifPresent(inv -> {
                        inv.setAvailableCopies(inv.getAvailableCopies() + 1);
                        bookInventoryRepo.save(inv);
                    });

            return new ApiResponse<>(true, "Return accepted successfully", mapToResponse(txn));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to accept return: " + e.getMessage(), null);
        }
    }

    // ---------------- RENEW BOOK ----------------
    public ApiResponse<BorrowResponse> renewBook(Long id) {
        try {

            if (!borrowRepo.existsById(id)) {
                return new ApiResponse<>(false, "Transaction not found", null);
            }

            BorrowTransaction txn = borrowRepo.findByIdWithDetails(id).get();

            if (txn.getStatus() != TransactionStatus.BORROWED) {
                return new ApiResponse<>(false, "Only borrowed books can be renewed", null);
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
    public ApiResponse<PageResponse<BorrowResponse>> getMyBorrowedBooks(Pageable pageable, Long userId) {
        try {
            Page<BorrowResponse> data = borrowRepo
                    .findByUserIdAndStatusIn(userId, java.util.List.of(TransactionStatus.BORROWED, TransactionStatus.RETURN_REQUESTED), pageable)
                    .map(this::mapToResponse);
            return new ApiResponse<>(true, "Fetched borrowed books successfully", helperService.toPageResponse(data));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch borrowed books: " + e.getMessage(), null);
        }
    }

    public ApiResponse<PageResponse<BorrowResponse>> getMyBorrowHistory(Pageable pageable, Long userId) {
        try {
            Page<BorrowResponse> data = borrowRepo
                    .findByUserIdAndStatusNot(userId, TransactionStatus.BORROWED, pageable)
                    .map(this::mapToResponse);
            return new ApiResponse<>(true, "Fetched borrow history successfully", helperService.toPageResponse(data));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch borrow history: " + e.getMessage(), null);
        }
    }

    // ---------------- ALL TRANSACTIONS ----------------
    public ApiResponse<PageResponse<BorrowResponse>> getAllTransactions(Pageable pageable) {
        try {
            Page<BorrowResponse> data = borrowRepo.findAllWithDetails(pageable)
                    .map(this::mapToResponse);
            return new ApiResponse<>(true, "Fetched all transactions successfully", helperService.toPageResponse(data));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch transactions: " + e.getMessage(), null);
        }
    }

    // ---------------- ALL OVERDUE ----------------
    public ApiResponse<PageResponse<BorrowResponse>> getAllOverdue(Pageable pageable) {
        try {
            Page<BorrowResponse> data = borrowRepo
                    .findOverdue(LocalDate.now(), pageable)
                    .map(this::mapToResponse);

            return new ApiResponse<>(true, "Fetched overdue books successfully",  helperService.toPageResponse(data));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch overdue books", null);
        }
    }

    // ---------------- OVERDUE BY BRANCH ----------------
    public ApiResponse<PageResponse<BorrowResponse>> getOverdueByBranch(Long branchId, Pageable pageable) {
        try {
            Page<BorrowResponse> data = borrowRepo
                    .findOverdueByBranch(branchId, LocalDate.now(), pageable)
                    .map(this::mapToResponse);

            return new ApiResponse<>(true, "Fetched branch overdue books successfully",  helperService.toPageResponse(data));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch branch overdue books", null);
        }
    }

    // ---------------- GET BY ID ----------------
    public ApiResponse<BorrowResponse> getTransactionById(Long id) {
        try {

            if (!borrowRepo.existsById(id)) {
                return new ApiResponse<>(false, "Transaction not found", null);
            }

            BorrowTransaction txn = borrowRepo.findById(id).get();

            return new ApiResponse<>(true, "Fetched transaction successfully", mapToResponse(txn));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch transaction", null);
        }
    }

    // ---------------- MARK AS LOST ----------------
    public ApiResponse<BorrowResponse> markAsLost(Long id) {
        try {

            if (!borrowRepo.existsById(id)) {
                return new ApiResponse<>(false, "Transaction not found", null);
            }

            BorrowTransaction txn = borrowRepo.findById(id).get();

            if (txn.getStatus() == TransactionStatus.RETURNED) {
                return new ApiResponse<>(false, "Returned book cannot be marked as lost", null);
            }

            txn.setStatus(TransactionStatus.LOST);

            borrowRepo.save(txn);

            return new ApiResponse<>(true, "Book marked as lost successfully", mapToResponse(txn));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to mark book as lost", null);
        }
    }

    // ---------------- USER HISTORY ----------------
    public ApiResponse<PageResponse<BorrowResponse>> getUserHistory(Long userId, Pageable pageable) {
        try {
            Page<BorrowResponse> data = borrowRepo
                    .findByUserId(userId, pageable)
                    .map(this::mapToResponse);

            return new ApiResponse<>(true, "Fetched user history successfully",  helperService.toPageResponse(data));

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
                .bookAuthor(txn.getBook().getAuthor())
                .coverImageUrl(txn.getBook().getCoverImageUrl())
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
