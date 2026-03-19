package com.example.Library_backend.controller;

import com.example.Library_backend.dto.request.BookRequest;
import com.example.Library_backend.dto.request.BookReviewRequest;
import com.example.Library_backend.dto.response.*;
import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.repository.BookInventoryRepository;
import com.example.Library_backend.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Book catalogue — public access")
public class BookController {

    private final BookService bookService;
    private final BookInventoryRepository bookInventoryRepository;


    @GetMapping
    @Operation(summary = "Get all books with pagination & optional filters")
    public ResponseEntity<ApiResponse<PagedResponse<BookResponse>>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subject) {

        PagedResponse<BookResponse> data = bookService.getAllBooks(
                page, size, sortBy, sortDir, title, author, category, subject);

        return ResponseEntity.ok(ApiResponse.success("Books fetched successfully", data));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get complete book details by ID")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success("Book fetched successfully",
                        bookService.getBookById(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search books by title, author, ISBN or subject")
    public ResponseEntity<ApiResponse<PagedResponse<BookResponse>>> searchBooks(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                ApiResponse.success("Search results fetched",
                        bookService.searchBooks(q, page, size)));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get top 10 most borrowed books")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getPopularBooks() {

        return ResponseEntity.ok(
                ApiResponse.success("Popular books fetched",
                        bookService.getPopularBooks()));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get top 10 trending books this semester (last 6 months)")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getTrendingBooks() {

        return ResponseEntity.ok(
                ApiResponse.success("Trending books fetched",
                        bookService.getTrendingBooks()));
    }

    @GetMapping("/new-arrivals")
    @Operation(summary = "Get recently added books (last 30 days)")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getNewArrivals() {

        return ResponseEntity.ok(
                ApiResponse.success("New arrivals fetched",
                        bookService.getNewArrivals()));
    }


        @GetMapping("/category/{category}")
    @Operation(summary = "Get books filtered by category")
    public ResponseEntity<ApiResponse<PagedResponse<BookResponse>>> getBooksByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                ApiResponse.success("Books by category fetched",
                        bookService.getBooksByCategory(category, page, size)));
    }

    @GetMapping("/{id}/availability")
    @Operation(
            summary = "Get book availability across all branches",
            security = @SecurityRequirement(name = "bearerAuth")

    )
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'LIBRARIAN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BookAvailabilityResponse>> getBookAvailability(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success("Book availability fetched",
                        bookService.getBookAvailability(id)));
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "Get all reviews and ratings for a book")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getBookReviews(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success("Reviews fetched",
                        bookService.getBookReviews(id)));
    }

    @PostMapping
    @Operation(
            summary = "Add a new book to the system",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @Valid @RequestBody BookRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book created successfully",
                        bookService.createBook(request)));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update book metadata",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Book updated successfully",
                        bookService.updateBook(id, request)));
    }


    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a book from the system — Super Admin only",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @PathVariable Long id) {

        bookService.deleteBook(id);

        return ResponseEntity.ok(
                ApiResponse.success("Book deleted successfully", null));
    }


    @PostMapping("/{id}/reviews")
    @Operation(
            summary = "Submit a review and rating for a book",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY')")
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @PathVariable Long id,
            @Valid @RequestBody BookReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully",
                        bookService.submitReview(id, request,
                                userDetails.getUsername())));
    }

    @PutMapping("/{id}/reviews/{rid}")
    @Operation(
            summary = "Edit your own book review",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY')")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long id,
            @PathVariable Long rid,
            @Valid @RequestBody BookReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                ApiResponse.success("Review updated successfully",
                        bookService.updateReview(id, rid, request,
                                userDetails.getUsername())));
    }

    @DeleteMapping("/{id}/reviews/{rid}")
    @Operation(
            summary = "Delete a book review — own review or Admin",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long id,
            @PathVariable Long rid,
            @AuthenticationPrincipal UserDetails userDetails) {

        bookService.deleteReview(id, rid, userDetails.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success("Review deleted successfully", null));
    }


}


