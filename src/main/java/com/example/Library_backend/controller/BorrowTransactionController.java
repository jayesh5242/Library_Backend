package com.example.Library_backend.controller;

import com.example.Library_backend.dto.request.IssueBookRequest;
import com.example.Library_backend.dto.request.ReturnBookRequest;
import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.BorrowResponse;

import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.BorrowResponse;
import com.example.Library_backend.service.BorrowTransactionService;
import com.example.Library_backend.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowTransactionController {

    private final BorrowTransactionService borrowService;
    private final CurrentUserService currentUserService;

    @PostMapping("/issue")
    public ResponseEntity<ApiResponse<?>> issueBook(@RequestBody IssueBookRequest request) {
        try {
            return ResponseEntity.ok(borrowService.issueBook(request));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<BorrowResponse>builder()
                    .success(false)
                    .message("Failed to issue book")
                    .data(null)
                    .build());
        }
    }
    @PostMapping("/return")
    public ResponseEntity<ApiResponse<BorrowResponse>> returnBook(@RequestBody ReturnBookRequest request) {
        try {
            return ResponseEntity.ok(borrowService.returnBook(request));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<BorrowResponse>builder()
                    .success(false)
                    .message("Failed to return book")
                    .data(null)
                    .build());
        }
    }

    @PostMapping("/renew/{id}")
    public ResponseEntity<ApiResponse<BorrowResponse>> renewBook(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(borrowService.renewBook(id));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<BorrowResponse>builder()
                    .success(false)
                    .message("Failed to renew book")
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<BorrowResponse>>> getMyBorrowedBooks(@RequestHeader("Authorization") String authToken, Pageable pageable) {
        try {
            Long userId = 1L;
            return ResponseEntity.ok(borrowService.getMyBorrowedBooks(pageable,userId));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Page<BorrowResponse>>builder()
                    .success(false)
                    .message("Failed to fetch borrowed books")
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/my/history")
    public ResponseEntity<ApiResponse<Page<BorrowResponse>>> getMyBorrowHistory(Pageable pageable) {
        try {
            Long userId = currentUserService.getCurrentUserId();
            return ResponseEntity.ok(borrowService.getMyBorrowHistory(pageable,userId));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Page<BorrowResponse>>builder()
                    .success(false)
                    .message("Failed to fetch borrow history")
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<BorrowResponse>>> getAllTransactions(Pageable pageable) {
        try {
            return ResponseEntity.ok(borrowService.getAllTransactions(pageable));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Page<BorrowResponse>>builder()
                    .success(false)
                    .message("Failed to fetch transactions")
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<Page<BorrowResponse>>> getAllOverdue(Pageable pageable) {
        try {
            return ResponseEntity.ok(borrowService.getAllOverdue(pageable));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Page<BorrowResponse>>builder()
                    .success(false)
                    .message("Failed to fetch overdue books")
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/overdue/branch/{id}")
    public ResponseEntity<ApiResponse<Page<BorrowResponse>>> getOverdueByBranch(@PathVariable Long id, Pageable pageable) {
        try {
            return ResponseEntity.ok(borrowService.getOverdueByBranch(id, pageable));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Page<BorrowResponse>>builder()
                    .success(false)
                    .message("Failed to fetch branch overdue books")
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BorrowResponse>> getTransactionById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(borrowService.getTransactionById(id));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<BorrowResponse>builder()
                    .success(false)
                    .message("Failed to fetch transaction")
                    .data(null)
                    .build());
        }
    }

    @PutMapping("/{id}/lost")
    public ResponseEntity<ApiResponse<BorrowResponse>> markAsLost(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(borrowService.markAsLost(id));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<BorrowResponse>builder()
                    .success(false)
                    .message("Failed to mark book as lost")
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<BorrowResponse>>> getUserHistory(@PathVariable Long userId, Pageable pageable) {
        try {
            return ResponseEntity.ok(borrowService.getUserHistory(userId, pageable));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Page<BorrowResponse>>builder()
                    .success(false)
                    .message("Failed to fetch user history")
                    .data(null)
                    .build());
        }
    }
}
