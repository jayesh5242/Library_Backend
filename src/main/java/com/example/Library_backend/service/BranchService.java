package com.example.Library_backend.service;

import com.example.Library_backend.dto.request.BranchRequest;
import com.example.Library_backend.dto.response.*;
import com.example.Library_backend.entity.BookInventory;
import com.example.Library_backend.entity.BorrowTransaction;
import com.example.Library_backend.entity.Branch;
import com.example.Library_backend.entity.User;
import com.example.Library_backend.exception.DuplicateResourceException;
import com.example.Library_backend.exception.ResourceNotFoundException;
import com.example.Library_backend.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
 
@Service
@RequiredArgsConstructor
public class BranchService {
 
    private final BranchRepository branchRepository;
    private final BookInventoryRepository bookInventoryRepository;
    private final BorrowTransactionRepository borrowTransactionRepository;
    private final FineRepository fineRepository;
    private final UserRepository userRepository;
    /**
     * GET /api/branches
     * Fetches only active branches (is_active = true).
     * Returns list — not paginated.
     */
    public List<BranchResponse> getAllActiveBranches() {
        return branchRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * GET /api/branches/{id}
     * Finds branch by ID.
     * Throws ResourceNotFoundException (404) if not found.
     * Returns full branch details including librarian info.
     */
    public BranchResponse getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch not found with id: " + id));
        return mapToResponse(branch);
    }

    /**
     * GET /api/branches/{id}/books
     * Checks branch exists first.
     * Fetches paginated inventory records for the branch.
     * Maps each inventory record to BookResponse.
     */
    public PagedResponse<BookResponse> getBooksInBranch(
            Long branchId, int page, int size) {

        // Step 1: Check branch exists
        branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch not found with id: " + branchId));

        // Step 2: Get paginated inventory for branch
        Pageable pageable = PageRequest.of(page, size);
        Page<BookInventory> inventoryPage =
                bookInventoryRepository.findByBranchId(branchId, pageable);

        // Step 3: Map each inventory to BookResponse
        List<BookResponse> content = inventoryPage.getContent()
                .stream()
                .map(inv -> {
                    BookResponse res = new BookResponse();
                    res.setId(inv.getBook().getId());
                    res.setTitle(inv.getBook().getTitle());
                    res.setAuthor(inv.getBook().getAuthor());
                    res.setIsbn(inv.getBook().getIsbn());
                    res.setCategory(inv.getBook().getCategory());
                    res.setSubject(inv.getBook().getSubject());
                    res.setPublisher(inv.getBook().getPublisher());
                    res.setEdition(inv.getBook().getEdition());
                    res.setYear(inv.getBook().getYear());
                    res.setLanguage(inv.getBook().getLanguage());
                    res.setTotalPages(inv.getBook().getTotalPages());
                    res.setCoverImageUrl(inv.getBook().getCoverImageUrl());
                    res.setCreatedAt(inv.getBook().getCreatedAt());

                    // Branch-specific copy counts
                    res.setTotalCopies(inv.getTotalCopies());
                    res.setAvailableCopies(inv.getAvailableCopies());
                    return res;
                })
                .collect(Collectors.toList());

        // Step 4: Build paginated response
        return PagedResponse.<BookResponse>builder()
                .content(content)
                .pageNumber(inventoryPage.getNumber())
                .pageSize(inventoryPage.getSize())
                .totalElements(inventoryPage.getTotalElements())
                .totalPages(inventoryPage.getTotalPages())
                .lastPage(inventoryPage.isLast())
                .build();
    }

    /**
     * GET /api/branches/{id}/inventory
     * Checks branch exists.
     * Fetches all inventory records for the branch.
     * Maps each to InventoryResponse with borrowed count.
     */
    public List<InventoryResponse> getBranchInventory(Long branchId) {

        // Step 1: Check branch exists
        branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch not found with id: " + branchId));

        // Step 2: Get all inventory records for branch
        List<BookInventory> inventories =
                bookInventoryRepository.findByBranchId(branchId);

        // Step 3: Map to InventoryResponse
        return inventories.stream()
                .map(inv -> InventoryResponse.builder()
                        .id(inv.getId())
                        .bookId(inv.getBook().getId())
                        .bookTitle(inv.getBook().getTitle())
                        .bookAuthor(inv.getBook().getAuthor())
                        .isbn(inv.getBook().getIsbn())
                        .category(inv.getBook().getCategory())
                        .branchId(inv.getBranch().getId())
                        .branchName(inv.getBranch().getName())
                        .totalCopies(inv.getTotalCopies())
                        .availableCopies(inv.getAvailableCopies())
                        .borrowedCopies(inv.getTotalCopies() - inv.getAvailableCopies())
                        .shelfLocation(inv.getShelfLocation())
                        .condition(inv.getCondition())
                        .addedAt(inv.getAddedAt())
                        .build())
                .collect(Collectors.toList());

    }

    /**
     * GET /api/branches/{id}/stats
     * Aggregates data from multiple repositories:
     * - Inventory totals
     * - Active + overdue borrows
     * - Fine amounts
     * - User count
     */
    public BranchStatsResponse getBranchStats(Long branchId) {

        // Step 1: Check branch exists
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch not found with id: " + branchId));

        // Step 2: Inventory stats
        Integer totalBooks     = bookInventoryRepository.sumTotalCopiesByBranchId(branchId);
        Integer availableBooks = bookInventoryRepository.sumAvailableCopiesByBranchId(branchId);
        Integer borrowedBooks  = (totalBooks != null && availableBooks != null)
                ? totalBooks - availableBooks : 0;

        // Step 3: Transaction stats
        Integer activeBorrows       = borrowTransactionRepository.countActiveBorrowsByBranchId(branchId);
        Integer overdueCount        = borrowTransactionRepository.countOverdueByBranchId(branchId);
        Integer totalBorrowsAllTime = borrowTransactionRepository.countAllBorrowsByBranchId(branchId);

        // Step 4: Fine stats
        Double totalFinesPending   = fineRepository.sumPendingFinesByBranchId(branchId);
        Double totalFinesCollected = fineRepository.sumCollectedFinesByBranchId(branchId);

        // Step 5: User stats
        Integer totalUsers = userRepository.countUsersByBranchId(branchId);

        // Step 6: Build and return stats
        return BranchStatsResponse.builder()
                .branchId(branch.getId())
                .branchName(branch.getName())
                .department(branch.getDepartment())
                .totalBooks(totalBooks != null ? totalBooks : 0)
                .availableBooks(availableBooks != null ? availableBooks : 0)
                .borrowedBooks(borrowedBooks)
                .activeBorrows(activeBorrows != null ? activeBorrows : 0)
                .overdueCount(overdueCount != null ? overdueCount : 0)
                .totalBorrowsAllTime(totalBorrowsAllTime != null ? totalBorrowsAllTime : 0)
                .totalFinesPending(totalFinesPending != null ? totalFinesPending : 0.0)
                .totalFinesCollected(totalFinesCollected != null ? totalFinesCollected : 0.0)
                .totalUsers(totalUsers != null ? totalUsers : 0)
                .build();
    }


    /**
     * POST /api/branches
     * Checks duplicate branch name.
     * Assigns librarian if librarianId provided.
     * Sets default maxBorrowDays = 14 if not provided.
     * Sets default finePerDay = 2.00 if not provided.
     * Saves and returns created branch.
     */
    public BranchResponse createBranch(BranchRequest request) {

        // Step 1: Check duplicate branch name
        if (branchRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    "Branch with name '" + request.getName() + "' already exists");
        }

        // Step 2: Find librarian if provided
        User librarian = null;
        if (request.getLibrarianId() != null) {
            librarian = userRepository.findById(request.getLibrarianId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Librarian not found with id: " + request.getLibrarianId()));
        }

        // Step 3: Build branch entity
        Branch branch = Branch.builder()
                .name(request.getName())
                .department(request.getDepartment())
                .location(request.getLocation())
                .phone(request.getPhone())
                .email(request.getEmail())
                .librarian(librarian)
                .operatingHours(request.getOperatingHours())
                .maxBorrowDays(request.getMaxBorrowDays() != null
                        ? request.getMaxBorrowDays() : 14)
                .finePerDay(request.getFinePerDay() != null
                        ? request.getFinePerDay() : BigDecimal.valueOf(2.00))
                .isActive(true)
                .build();

        // Step 4: Save and return
        return mapToResponse(branchRepository.save(branch));
    }

    /**
     * PUT /api/branches/{id}
     * Finds branch by ID — throws 404 if not found.
     * If name changed — checks not taken by another branch.
     * Updates all fields from request.
     * Saves and returns updated branch.
     */
    public BranchResponse updateBranch(Long id, BranchRequest request) {

        // Step 1: Find existing branch
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch not found with id: " + id));

        // Step 2: If name changed check not taken by another branch
        if (!branch.getName().equals(request.getName())
                && branchRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException(
                    "Branch with name '" + request.getName() + "' already exists");
        }

        // Step 3: Find librarian if provided
        if (request.getLibrarianId() != null) {
            User librarian = userRepository.findById(request.getLibrarianId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Librarian not found with id: "
                                    + request.getLibrarianId()));
            branch.setLibrarian(librarian);
        }

        // Step 4: Update all fields
        branch.setName(request.getName());
        branch.setDepartment(request.getDepartment());
        branch.setLocation(request.getLocation());
        branch.setPhone(request.getPhone());
        branch.setEmail(request.getEmail());
        branch.setOperatingHours(request.getOperatingHours());

        if (request.getMaxBorrowDays() != null)
            branch.setMaxBorrowDays(request.getMaxBorrowDays());

        if (request.getFinePerDay() != null)
            branch.setFinePerDay(request.getFinePerDay());

        // Step 5: Save and return
        return mapToResponse(branchRepository.save(branch));
    }


    /**
     * DELETE /api/branches/{id}
     * Finds branch by ID — throws 404 if not found.
     * Sets isActive = false — soft delete.
     * Branch data is preserved in DB.
     * Branch will no longer appear in GET /api/branches list.
     */
    public void deactivateBranch(Long id) {

        // Step 1: Find existing branch
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch not found with id: " + id));

        // Step 2: Check if already deactivated
        if (!branch.getIsActive()) {
            throw new IllegalArgumentException(
                    "Branch is already deactivated");
        }

        // Step 3: Soft delete — set isActive = false
        branch.setIsActive(false);
        branchRepository.save(branch);
    }

    public BranchResponse assignLibrarian(Long branchId, Long librarianId) {

        // Step 1: Find branch
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch not found with id: " + branchId));

        // Step 2: Find user
        User librarian = userRepository.findById(librarianId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + librarianId));

        // Step 3: Validate user has LIBRARIAN role
        if (!librarian.getRole().name().equals("LIBRARIAN")) {
            throw new IllegalArgumentException(
                    "User with id " + librarianId
                            + " does not have LIBRARIAN role");
        }

        // Step 4: Assign librarian to branch
        branch.setLibrarian(librarian);

        // Step 5: Save and return
        return mapToResponse(branchRepository.save(branch));
    }

    /**
     * GET /api/branches/{id}/overdue
     * Finds branch — throws 404 if not found.
     * Fetches all OVERDUE transactions for the branch.
     * Calculates days overdue and fine amount for each.
     */
    public List<OverdueResponse> getOverdueBooks(Long branchId) {

        // Step 1: Check branch exists
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch not found with id: " + branchId));

        // Step 2: Get all overdue transactions for branch
        List<BorrowTransaction> overdueList =
                borrowTransactionRepository.findOverdueByBranchId(branchId);

        // Step 3: Map to OverdueResponse with calculated fields
        return overdueList.stream()
                .map(tx -> {

                    // Calculate days overdue
                    long daysOverdue = java.time.temporal.ChronoUnit.DAYS
                            .between(tx.getDueDate(), LocalDate.now());

                    // Calculate fine amount
                    double fineAmount = daysOverdue
                            * branch.getFinePerDay().doubleValue();

                    return OverdueResponse.builder()
                            .transactionId(tx.getId())
                            .bookId(tx.getBook().getId())
                            .bookTitle(tx.getBook().getTitle())
                            .isbn(tx.getBook().getIsbn())
                            .userId(tx.getUser().getId())
                            .userName(tx.getUser().getFullName())
                            .userEmail(tx.getUser().getEmail())
                            .userPhone(tx.getUser().getPhone())
                            .branchId(branch.getId())
                            .branchName(branch.getName())
                            .issueDate(tx.getIssueDate())
                            .dueDate(tx.getDueDate())
                            .daysOverdue((int) daysOverdue)
                            .fineAmount(fineAmount)
                            .createdAt(tx.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }





















    // ── shared helper ─────────────────────────────────────────
    protected BranchResponse mapToResponse(Branch branch) {
        BranchResponse res = new BranchResponse();
        res.setId(branch.getId());
        res.setName(branch.getName());
        res.setDepartment(branch.getDepartment());
        res.setLocation(branch.getLocation());
        res.setPhone(branch.getPhone());
        res.setEmail(branch.getEmail());
        res.setOperatingHours(branch.getOperatingHours());
        res.setMaxBorrowDays(branch.getMaxBorrowDays());
        res.setFinePerDay(branch.getFinePerDay());
        res.setIsActive(branch.getIsActive());
        res.setCreatedAt(branch.getCreatedAt());
 
        // Librarian info — null safe
        if (branch.getLibrarian() != null) {
            res.setLibrarianId(branch.getLibrarian().getId());
            res.setLibrarianName(branch.getLibrarian().getFullName());
        }
 
        return res;
    }
}