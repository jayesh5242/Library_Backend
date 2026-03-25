package com.example.Library_backend.service;

import com.example.Library_backend.dto.request.PurchaseRequestRequest;
import com.example.Library_backend.dto.request.ReadingListRequest;
import com.example.Library_backend.dto.response.BookResponse;
import com.example.Library_backend.dto.response.PurchaseRequestResponse;
import com.example.Library_backend.dto.response.ReadingListResponse;
import com.example.Library_backend.entity.*;
import com.example.Library_backend.enums.Priority;
import com.example.Library_backend.enums.RequestStatus;
import com.example.Library_backend.exception.ResourceNotFoundException;
import com.example.Library_backend.exception.UnauthorizedException;
import com.example.Library_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final ReadingListRepository     readingListRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final BookRepository            bookRepository;
    private final BranchRepository          branchRepository;
    private final UserRepository            userRepository;
    private final BookService               bookService;

    // ─────────────────────────────────────────────────────────
    // API 1: GET /api/reading-lists
    // Browse all public reading lists — Public
    // ─────────────────────────────────────────────────────────
    public List<ReadingListResponse> getAllPublicReadingLists() {
        return readingListRepository.findByIsPublicTrue()
                .stream()
                .map(rl -> mapToResponse(rl, false))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────
    // API 2: GET /api/reading-lists/{id}
    // Get reading list with all books — Public
    // ─────────────────────────────────────────────────────────
    public ReadingListResponse getReadingListById(Long id) {
        ReadingList rl = readingListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reading list not found with id: " + id));
        return mapToResponse(rl, true);
    }

    // ─────────────────────────────────────────────────────────
    // API 3: GET /api/reading-lists/my
    // Faculty's own reading lists — Faculty only
    // ─────────────────────────────────────────────────────────
    public List<ReadingListResponse> getMyReadingLists(String email) {
        User faculty = findUserByEmail(email);
        return readingListRepository.findByFacultyId(faculty.getId())
                .stream()
                .map(rl -> mapToResponse(rl, false))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────
    // API 4: POST /api/reading-lists
    // Create a new reading list — Faculty only
    // ─────────────────────────────────────────────────────────
    @Transactional
    public ReadingListResponse createReadingList(
            ReadingListRequest request, String email) {

        User faculty = findUserByEmail(email);

        ReadingList rl = ReadingList.builder()
                .faculty(faculty)
                .title(request.getTitle())
                .subject(request.getSubject())
                .semester(request.getSemester())
                .description(request.getDescription())
                .isPublic(request.getIsPublic() != null
                        ? request.getIsPublic() : true)
                .build();

        return mapToResponse(readingListRepository.save(rl), false);
    }

    // ─────────────────────────────────────────────────────────
    // API 5: PUT /api/reading-lists/{id}
    // Update reading list — Faculty only (own list)
    // ─────────────────────────────────────────────────────────
    @Transactional
    public ReadingListResponse updateReadingList(
            Long id, ReadingListRequest request, String email) {

        ReadingList rl = readingListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reading list not found with id: " + id));

        User faculty = findUserByEmail(email);

        if (!rl.getFaculty().getId().equals(faculty.getId()))
            throw new UnauthorizedException(
                    "You can only update your own reading lists");

        rl.setTitle(request.getTitle());
        rl.setSubject(request.getSubject());
        rl.setSemester(request.getSemester());
        rl.setDescription(request.getDescription());
        if (request.getIsPublic() != null)
            rl.setIsPublic(request.getIsPublic());

        return mapToResponse(readingListRepository.save(rl), false);
    }

    // ─────────────────────────────────────────────────────────
    // API 6: DELETE /api/reading-lists/{id}
    // Delete reading list — Faculty (own) or Admin
    // ─────────────────────────────────────────────────────────
    @Transactional
    public void deleteReadingList(Long id, String email) {

        ReadingList rl = readingListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reading list not found with id: " + id));

        User user = findUserByEmail(email);

        boolean isAdmin = user.getRole().name().equals("SUPER_ADMIN");
        boolean isOwner = rl.getFaculty().getId().equals(user.getId());

        if (!isAdmin && !isOwner)
            throw new UnauthorizedException(
                    "You can only delete your own reading lists");

        readingListRepository.delete(rl);
    }

    // ─────────────────────────────────────────────────────────
    // API 7: POST /api/reading-lists/{id}/books
    // Add book to reading list — Faculty only (own list)
    // ─────────────────────────────────────────────────────────
    @Transactional
    public ReadingListResponse addBookToReadingList(
            Long readingListId, Long bookId, String email) {

        ReadingList rl = readingListRepository.findById(readingListId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reading list not found with id: " + readingListId));

        User faculty = findUserByEmail(email);

        if (!rl.getFaculty().getId().equals(faculty.getId()))
            throw new UnauthorizedException(
                    "You can only add books to your own reading lists");

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + bookId));

        if (rl.getBooks().stream().anyMatch(b -> b.getId().equals(bookId)))
            throw new IllegalArgumentException(
                    "Book is already in this reading list");

        rl.getBooks().add(book);
        return mapToResponse(readingListRepository.save(rl), true);
    }

    // ─────────────────────────────────────────────────────────
    // API 8: DELETE /api/reading-lists/{id}/books/{bookId}
    // Remove book from reading list — Faculty only (own list)
    // ─────────────────────────────────────────────────────────
    @Transactional
    public ReadingListResponse removeBookFromReadingList(
            Long readingListId, Long bookId, String email) {

        ReadingList rl = readingListRepository.findById(readingListId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reading list not found with id: " + readingListId));

        User faculty = findUserByEmail(email);

        if (!rl.getFaculty().getId().equals(faculty.getId()))
            throw new UnauthorizedException(
                    "You can only remove books from your own reading lists");

        rl.getBooks().removeIf(b -> b.getId().equals(bookId));
        return mapToResponse(readingListRepository.save(rl), true);
    }

    // ─────────────────────────────────────────────────────────
    // API 9: GET /api/purchase-requests/my
    // Faculty's own purchase requests — Faculty only
    // ─────────────────────────────────────────────────────────
    public List<PurchaseRequestResponse> getMyPurchaseRequests(String email) {
        User faculty = findUserByEmail(email);
        return purchaseRequestRepository
                .findByRequestedById(faculty.getId())
                .stream()
                .map(this::mapToPurchaseResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────
    // API 10: POST /api/purchase-requests
    // Submit a book purchase request — Faculty only
    // ─────────────────────────────────────────────────────────
    @Transactional
    public PurchaseRequestResponse submitPurchaseRequest(
            PurchaseRequestRequest request, String email) {

        User faculty = findUserByEmail(email);

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch not found with id: " + request.getBranchId()));

        Priority priority;
        try {
            priority = request.getPriority() != null
                    ? Priority.valueOf(request.getPriority().toUpperCase())
                    : Priority.NORMAL;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid priority. Must be: LOW, NORMAL, HIGH, URGENT");
        }

        BookPurchaseRequest purchaseRequest = BookPurchaseRequest.builder()
                .requestedBy(faculty)
                .branch(branch)
                .bookTitle(request.getBookTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .reason(request.getReason())
                .priority(priority)
                .status(RequestStatus.PENDING)
                .build();

        return mapToPurchaseResponse(
                purchaseRequestRepository.save(purchaseRequest));
    }

    // ─────────────────────────────────────────────────────────
    // API 11: GET /api/purchase-requests/all
    // All purchase requests — Librarian/Admin only
    // ─────────────────────────────────────────────────────────
    public List<PurchaseRequestResponse> getAllPurchaseRequests() {
        return purchaseRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToPurchaseResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────
    // API 12: PUT /api/purchase-requests/{id}/approve
    // Approve a purchase request — Admin only
    // ─────────────────────────────────────────────────────────
    @Transactional
    public PurchaseRequestResponse approvePurchaseRequest(
            Long id, String adminEmail) {

        BookPurchaseRequest request = findPurchaseRequestOrThrow(id);
        User admin = findUserByEmail(adminEmail);

        request.setStatus(RequestStatus.APPROVED);
        request.setApprovedBy(admin);

        return mapToPurchaseResponse(purchaseRequestRepository.save(request));
    }

    // ─────────────────────────────────────────────────────────
    // API 13: PUT /api/purchase-requests/{id}/reject
    // Reject a purchase request with reason — Admin only
    // ─────────────────────────────────────────────────────────
    @Transactional
    public PurchaseRequestResponse rejectPurchaseRequest(
            Long id, String adminNotes, String adminEmail) {

        BookPurchaseRequest request = findPurchaseRequestOrThrow(id);
        User admin = findUserByEmail(adminEmail);

        request.setStatus(RequestStatus.REJECTED);
        request.setApprovedBy(admin);
        request.setAdminNotes(adminNotes);

        return mapToPurchaseResponse(purchaseRequestRepository.save(request));
    }

    // ─────────────────────────────────────────────────────────
    // SHARED HELPERS
    // ─────────────────────────────────────────────────────────

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));
    }

    private BookPurchaseRequest findPurchaseRequestOrThrow(Long id) {
        return purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase request not found with id: " + id));
    }

    public ReadingListResponse mapToResponse(
            ReadingList rl, boolean includeBooks) {

        ReadingListResponse res = ReadingListResponse.builder()
                .id(rl.getId())
                .facultyId(rl.getFaculty().getId())
                .facultyName(rl.getFaculty().getFullName())
                .title(rl.getTitle())
                .subject(rl.getSubject())
                .semester(rl.getSemester())
                .description(rl.getDescription())
                .isPublic(rl.getIsPublic())
                .bookCount(rl.getBooks() != null ? rl.getBooks().size() : 0)
                .createdAt(rl.getCreatedAt())
                .build();

        if (includeBooks && rl.getBooks() != null) {
            List<BookResponse> books = rl.getBooks().stream()
                    .map(bookService::mapToResponse)
                    .collect(Collectors.toList());
            res.setBooks(books);
        }

        return res;
    }

    public PurchaseRequestResponse mapToPurchaseResponse(
            BookPurchaseRequest req) {

        PurchaseRequestResponse res = PurchaseRequestResponse.builder()
                .id(req.getId())
                .requestedById(req.getRequestedBy().getId())
                .requestedByName(req.getRequestedBy().getFullName())
                .branchId(req.getBranch().getId())
                .branchName(req.getBranch().getName())
                .bookTitle(req.getBookTitle())
                .author(req.getAuthor())
                .isbn(req.getIsbn())
                .reason(req.getReason())
                .priority(req.getPriority() != null
                        ? req.getPriority().name() : null)
                .status(req.getStatus() != null
                        ? req.getStatus().name() : null)
                .adminNotes(req.getAdminNotes())
                .createdAt(req.getCreatedAt())
                .build();

        if (req.getApprovedBy() != null) {
            res.setApprovedById(req.getApprovedBy().getId());
            res.setApprovedByName(req.getApprovedBy().getFullName());
        }

        return res;
    }
}
