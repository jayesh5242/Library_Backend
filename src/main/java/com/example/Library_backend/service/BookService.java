package com.example.Library_backend.service;


import com.example.Library_backend.dto.request.BookRequest;
import com.example.Library_backend.dto.request.BookReviewRequest;
import com.example.Library_backend.dto.response.*;
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
 
import java.util.List;
import java.util.stream.Collectors;
 
@Service
@RequiredArgsConstructor
public class BookService {
 
    private final BookRepository bookRepository;
    private final BookInventoryRepository  bookInventoryRepository;
    private final BookReviewRepository bookReviewRepository;
    private final UserRepository userRepository;

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
                blankToNull(title),
                blankToNull(author),
                blankToNull(category),
                blankToNull(subject),
                pageable)
                : bookRepository.findAll(pageable);

        return buildPagedResponse(books);
    }

    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + id));
        return mapToResponse(book);
    }

    public List<BookResponse> getPopularBooks() {
        return bookRepository.findTopBorrowedBooks()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BookResponse> getTrendingBooks() {
        return bookRepository.findTrendingBooks()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BookResponse> getNewArrivals() {
        return bookRepository.findNewArrivals()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PagedResponse<BookResponse> getBooksByCategory(
            String category, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findByCategory(category, pageable);
        return buildPagedResponse(books);
    }


    public BookAvailabilityResponse getBookAvailability(Long bookId) {

        // Step 1: Find the book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + bookId));

        // Step 2: Get all inventory records for this book
        List<BookInventory> inventories =
                bookInventoryRepository.findByBookId(bookId);

        // Step 3: Build per-branch breakdown
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

        // Step 4: Compute totals across all branches
        int totalCopies = inventories.stream()
                .mapToInt(BookInventory::getTotalCopies).sum();
        int availableCopies = inventories.stream()
                .mapToInt(BookInventory::getAvailableCopies).sum();

        // Step 5: Build and return response
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


    public List<ReviewResponse> getBookReviews(Long bookId) {

        // Check book exists first
        bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + bookId));

        // Fetch only approved reviews
        List<BookReview> reviews =
                bookReviewRepository.findByBookIdAndIsApprovedTrue(bookId);

        // Map to response
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

    public BookResponse updateBook(Long id, BookRequest request) {

        // Step 1: Find existing book
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + id));

        // Step 2: If ISBN changed, check it's not taken by another book
        if (!book.getIsbn().equals(request.getIsbn())
                && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException(
                    "ISBN " + request.getIsbn() + " is already used by another book");
        }

        // Step 3: Update all fields
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

        // Step 4: Save and return
        return mapToResponse(bookRepository.save(book));
    }

    public void deleteBook(Long id) {

        // Step 1: Find existing book — throw 404 if not found
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + id));

        // Step 2: Delete book
        // CascadeType.ALL on inventories + reviews
        // means they get deleted automatically
        bookRepository.delete(book);
    }

    public ReviewResponse submitReview(
            Long bookId, BookReviewRequest request, String userEmail) {

        // Step 1: Find book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + bookId));

        // Step 2: Find logged-in user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        // Step 3: Check duplicate review
        if (bookReviewRepository.existsByBookIdAndUserId(bookId, user.getId())) {
            throw new DuplicateResourceException(
                    "You have already reviewed this book");
        }

        // Step 4: Build and save review
        BookReview review = BookReview.builder()
                .book(book)
                .user(user)
                .rating(request.getRating())
                .reviewText(request.getReviewText())
                .isApproved(true)
                .build();

        BookReview saved = bookReviewRepository.save(review);

        // Step 5: Return response
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


    public ReviewResponse updateReview(
            Long bookId, Long reviewId,
            BookReviewRequest request, String userEmail) {

        // Step 1: Find book — throws 404 if not found
        bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + bookId));

        // Step 2: Find review — throws 404 if not found
        BookReview review = bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found with id: " + reviewId));

        // Step 3: Find logged-in user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        // Step 4: Verify ownership — user can only edit their own review
        if (!review.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException(
                    "You are not allowed to edit someone else's review");
        }

        // Step 5: Update fields
        review.setRating(request.getRating());
        if (request.getReviewText() != null) {
            review.setReviewText(request.getReviewText());
        }

        // Step 6: Save and return
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
    public void deleteReview(Long bookId, Long reviewId, String userEmail) {

        // Step 1: Find book — throws 404 if not found
        bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + bookId));

        // Step 2: Find review — throws 404 if not found
        BookReview review = bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found with id: " + reviewId));

        // Step 3: Find logged-in user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        // Step 4: Check ownership
        // Admin can delete any review
        // Student/Faculty can only delete their own review
        boolean isAdmin = user.getRole().name().equals("SUPER_ADMIN");
        boolean isOwner = review.getUser().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedException(
                    "You are not allowed to delete someone else's review");
        }

        // Step 5: Delete review
        bookReviewRepository.delete(review);
    }




    // ── shared helpers (used by all Book service methods) ─────

    protected BookResponse mapToResponse(Book book) {
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

        if (book.getInventories() != null) {
            res.setTotalCopies(book.getInventories().stream()
                    .mapToInt(BookInventory::getTotalCopies).sum());
            res.setAvailableCopies(book.getInventories().stream()
                    .mapToInt(BookInventory::getAvailableCopies).sum());
        }

        if (book.getReviews() != null && !book.getReviews().isEmpty()) {
            double avg = book.getReviews().stream()
                    .mapToInt(BookReview::getRating).average().orElse(0.0);
            res.setAverageRating(Math.round(avg * 10.0) / 10.0);
            res.setReviewCount(book.getReviews().size());
        }

        return res;
    }
    public PagedResponse<BookResponse> searchBooks(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.searchBooks(q, pageable);
        return buildPagedResponse(books);
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

    public BookResponse createBook(BookRequest request) {

        // Step 1: Check duplicate ISBN
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException(
                    "Book with ISBN " + request.getIsbn() + " already exists");
        }

        // Step 2: Build entity from request
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

        // Step 3: Save and return
        return mapToResponse(bookRepository.save(book));
    }
    protected PagedResponse<BookResponse> buildPagedResponse(Page<Book> page) {
        List<BookResponse> content = page.getContent()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
        return PagedResponse.<BookResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .lastPage(page.isLast())
                .build();
    }

    protected String blankToNull(String val) {
        return (val == null || val.isBlank()) ? null : val;
    }
}

 