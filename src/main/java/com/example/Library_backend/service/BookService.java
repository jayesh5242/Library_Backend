package com.example.Library_backend.service;

import com.example.Library_backend.dto.request.BookRequest;
import com.example.Library_backend.dto.request.BookReviewRequest;
import com.example.Library_backend.dto.response.*;
import com.example.Library_backend.dto.response.authresponse.PagedResponse;
import com.example.Library_backend.entity.Book;
import com.example.Library_backend.entity.BookInventory;
import com.example.Library_backend.entity.BookReview;
import com.example.Library_backend.entity.User;
import com.example.Library_backend.exception.DuplicateResourceException;
import com.example.Library_backend.exception.ResourceNotFoundException;
import com.example.Library_backend.exception.UnauthorizedException;
import com.example.Library_backend.repository.BookInventoryRepository;
import com.example.Library_backend.repository.BookRepository;
import com.example.Library_backend.repository.BookReviewRepository;
import com.example.Library_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository           bookRepository;
    private final BookInventoryRepository  bookInventoryRepository;
    private final BookReviewRepository     bookReviewRepository;
    private final UserRepository           userRepository;

    // ─────────────────────────────────────────────────────────
    // API 1: GET /api/books
    // @Transactional keeps Hibernate session open while
    // mapToResponse() accesses book.getInventories() and
    // book.getReviews() (lazy collections)
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PagedResponse<BookResponse> getAllBooks(
            int page, int size, String sortBy, String sortDir,
            String title, String author, String category, String subject) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        boolean hasFilter = title != null || author != null
                || category != null || subject != null;

        Page<Book> books = hasFilter
                ? bookRepository.filterBooks(
                blankToNull(title), blankToNull(author),
                blankToNull(category), blankToNull(subject), pageable)
                : bookRepository.findAll(pageable);

        return buildPagedResponse(books);
    }

    // ─────────────────────────────────────────────────────────
    // API 2: GET /api/books/{id}
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + id));
        return mapToResponse(book);
    }

    // ─────────────────────────────────────────────────────────
    // API 3: GET /api/books/search
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PagedResponse<BookResponse> searchBooks(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.searchBooks(q, pageable);
        return buildPagedResponse(books);
    }

    // ─────────────────────────────────────────────────────────
    // API 4: GET /api/books/popular
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<BookResponse> getPopularBooks() {
        return bookRepository.findTopBorrowedBooks()
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────
    // API 5: GET /api/books/trending
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<BookResponse> getTrendingBooks() {
        return bookRepository.findTrendingBooks()
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────
    // API 6: GET /api/books/new-arrivals
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<BookResponse> getNewArrivals() {
        return bookRepository.findNewArrivals()
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────
    // API 7: GET /api/books/category/{category}
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PagedResponse<BookResponse> getBooksByCategory(
            String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findByCategory(category, pageable);
        return buildPagedResponse(books);
    }

    // ─────────────────────────────────────────────────────────
    // API 8: GET /api/books/{id}/availability
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public BookAvailabilityResponse getBookAvailability(Long bookId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + bookId));

        List<BookInventory> inventories =
                bookInventoryRepository.findByBookId(bookId);

        List<BookAvailabilityResponse.BranchStock> branches = inventories.stream()
                .map(inv -> BookAvailabilityResponse.BranchStock.builder()
                        .branchId(inv.getBranch().getId())
                        .branchName(inv.getBranch().getName())
                        .location(inv.getBranch().getLocation())
                        .totalCopies(inv.getTotalCopies())
                        .availableCopies(inv.getAvailableCopies())
                        .shelfLocation(inv.getShelfLocation())
                        .isAvailable(inv.getAvailableCopies() > 0)
                        .build())
                .collect(Collectors.toList());

        int totalCopies = inventories.stream()
                .mapToInt(BookInventory::getTotalCopies).sum();
        int availableCopies = inventories.stream()
                .mapToInt(BookInventory::getAvailableCopies).sum();

        return BookAvailabilityResponse.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .author(book.getAuthor())
                .totalCopiesAllBranches(totalCopies)
                .availableCopiesAllBranches(availableCopies)
                .branches(branches)
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // API 9: GET /api/books/{id}/reviews
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ReviewResponse> getBookReviews(Long bookId) {

        bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + bookId));

        List<BookReview> reviews =
                bookReviewRepository.findByBookIdAndIsApprovedTrue(bookId);

        return reviews.stream()
                .map(review -> ReviewResponse.builder()
                        .id(review.getId())
                        .bookId(review.getBook().getId())
                        .userId(review.getUser().getId())
                        .userName(review.getUser().getFullName())
                        .rating(review.getRating())
                        .reviewText(review.getReviewText())
                        .isApproved(review.getIsApproved())
                        .createdAt(review.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────
    // API 10: POST /api/books
    // ─────────────────────────────────────────────────────────
    @Transactional
    public BookResponse createBook(BookRequest request) {

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException(
                    "Book with ISBN " + request.getIsbn() + " already exists");
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publisher(request.getPublisher())
                .edition(request.getEdition())
                .year(request.getYear())
                .category(request.getCategory())
                .subject(request.getSubject())
                .description(request.getDescription())
                .coverImageUrl(request.getCoverImageUrl())
                .language(request.getLanguage() != null
                        ? request.getLanguage() : "English")
                .totalPages(request.getTotalPages())
                .build();

        return mapToResponse(bookRepository.save(book));
    }

    // ─────────────────────────────────────────────────────────
    // API 11: PUT /api/books/{id}
    // ─────────────────────────────────────────────────────────
    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + id));

        if (!book.getIsbn().equals(request.getIsbn())
                && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException(
                    "ISBN " + request.getIsbn() + " is already used by another book");
        }

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPublisher(request.getPublisher());
        book.setEdition(request.getEdition());
        book.setYear(request.getYear());
        book.setCategory(request.getCategory());
        book.setSubject(request.getSubject());
        book.setDescription(request.getDescription());
        book.setCoverImageUrl(request.getCoverImageUrl());
        book.setLanguage(request.getLanguage() != null
                ? request.getLanguage() : "English");
        book.setTotalPages(request.getTotalPages());

        return mapToResponse(bookRepository.save(book));
    }

    // ─────────────────────────────────────────────────────────
    // API 12: DELETE /api/books/{id}
    // ─────────────────────────────────────────────────────────
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + id));
        bookRepository.delete(book);
    }

    // ─────────────────────────────────────────────────────────
    // API 13: POST /api/books/{id}/reviews
    // ─────────────────────────────────────────────────────────
    @Transactional
    public ReviewResponse submitReview(
            Long bookId, BookReviewRequest request, String userEmail) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + bookId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        if (bookReviewRepository.existsByBookIdAndUserId(bookId, user.getId())) {
            throw new DuplicateResourceException(
                    "You have already reviewed this book");
        }

        BookReview review = BookReview.builder()
                .book(book)
                .user(user)
                .rating(request.getRating())
                .reviewText(request.getReviewText())
                .isApproved(true)
                .build();

        BookReview saved = bookReviewRepository.save(review);

        return ReviewResponse.builder()
                .id(saved.getId())
                .bookId(saved.getBook().getId())
                .userId(saved.getUser().getId())
                .userName(saved.getUser().getFullName())
                .rating(saved.getRating())
                .reviewText(saved.getReviewText())
                .isApproved(saved.getIsApproved())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // API 14: PUT /api/books/{id}/reviews/{rid}
    // ─────────────────────────────────────────────────────────
    @Transactional
    public ReviewResponse updateReview(
            Long bookId, Long reviewId,
            BookReviewRequest request, String userEmail) {

        bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + bookId));

        BookReview review = bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found with id: " + reviewId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException(
                    "You are not allowed to edit someone else's review");
        }

        review.setRating(request.getRating());
        if (request.getReviewText() != null) {
            review.setReviewText(request.getReviewText());
        }

        BookReview updated = bookReviewRepository.save(review);

        return ReviewResponse.builder()
                .id(updated.getId())
                .bookId(updated.getBook().getId())
                .userId(updated.getUser().getId())
                .userName(updated.getUser().getFullName())
                .rating(updated.getRating())
                .reviewText(updated.getReviewText())
                .isApproved(updated.getIsApproved())
                .createdAt(updated.getCreatedAt())
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // API 15: DELETE /api/books/{id}/reviews/{rid}
    // ─────────────────────────────────────────────────────────
    @Transactional
    public void deleteReview(Long bookId, Long reviewId, String userEmail) {

        bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + bookId));

        BookReview review = bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found with id: " + reviewId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        boolean isAdmin = user.getRole().name().equals("SUPER_ADMIN");
        boolean isOwner = review.getUser().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedException(
                    "You are not allowed to delete someone else's review");
        }

        bookReviewRepository.delete(review);
    }

    // ─────────────────────────────────────────────────────────
    // SHARED HELPERS
    // ─────────────────────────────────────────────────────────

    // NOTE: This method is called INSIDE @Transactional methods
    //       so the Hibernate session is still active here.
    //       book.getInventories() and book.getReviews() are
    //       safely loaded within the open session.
    public BookResponse mapToResponse(Book book) {
        BookResponse res = new BookResponse();
        res.setId(book.getId());
        res.setTitle(book.getTitle());
        res.setAuthor(book.getAuthor());
        res.setIsbn(book.getIsbn());
        res.setPublisher(book.getPublisher());
        res.setEdition(book.getEdition());
        res.setYear(book.getYear());
        res.setCategory(book.getCategory());
        res.setSubject(book.getSubject());
        res.setDescription(book.getDescription());
        res.setCoverImageUrl(book.getCoverImageUrl());
        res.setLanguage(book.getLanguage());
        res.setTotalPages(book.getTotalPages());
        res.setCreatedAt(book.getCreatedAt());

        // ← These lazy collections are safely loaded
        //   because we are inside a @Transactional method
        if (book.getInventories() != null) {
            res.setTotalCopies(book.getInventories().stream()
                    .mapToInt(BookInventory::getTotalCopies).sum());
            res.setAvailableCopies(book.getInventories().stream()
                    .mapToInt(BookInventory::getAvailableCopies).sum());
        }

        if (book.getReviews() != null && !book.getReviews().isEmpty()) {
            double avg = book.getReviews().stream()
                    .mapToInt(BookReview::getRating)
                    .average().orElse(0.0);
            res.setAverageRating(Math.round(avg * 10.0) / 10.0);
            res.setReviewCount(book.getReviews().size());
        }

        return res;
    }

    public PagedResponse<BookResponse> buildPagedResponse(Page<Book> page) {
        List<BookResponse> content = page.getContent()
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<BookResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .lastPage(page.isLast())
                .build();
    }

    private String blankToNull(String val) {
        return (val == null || val.isBlank()) ? null : val;
    }

    public BookReviewResponse mapToReviewResponse(BookReview review) {
        return BookReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .reviewText(review.getReviewText())
                .isApproved(review.getIsApproved())
                .createdAt(review.getCreatedAt())
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .userName(review.getUser() != null ? review.getUser().getFullName() : null)
                .build();
    }
}