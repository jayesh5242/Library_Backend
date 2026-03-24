package com.example.Library_backend.service;

import com.example.Library_backend.dto.request.InventoryRequest;
import com.example.Library_backend.dto.response.InventoryResponse;
import com.example.Library_backend.entity.Book;
import com.example.Library_backend.entity.BookInventory;
import com.example.Library_backend.entity.Branch;
import com.example.Library_backend.exception.DuplicateResourceException;
import com.example.Library_backend.exception.ResourceNotFoundException;
import com.example.Library_backend.repository.BookInventoryRepository;
import com.example.Library_backend.repository.BookRepository;
import com.example.Library_backend.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
import java.util.stream.Collectors;
 
@Service
@RequiredArgsConstructor
public class InventoryService {
 
    private final BookInventoryRepository inventoryRepository;
    private final BookRepository          bookRepository;
    private final BranchRepository        branchRepository;
 
    // ─────────────────────────────────────────────────────────
    // API 1: GET /api/inventory
    // All inventory across all branches — Librarian/Admin only
    // ─────────────────────────────────────────────────────────
    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
 
    // ─────────────────────────────────────────────────────────
    // API 2: GET /api/inventory/branch/{id}
    // Inventory for a specific branch — Librarian/Admin only
    // ─────────────────────────────────────────────────────────
    public List<InventoryResponse> getInventoryByBranch(Long branchId) {
 
        // Check branch exists
        branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch not found with id: " + branchId));
 
        return inventoryRepository.findByBranchId(branchId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
 
    // ─────────────────────────────────────────────────────────
    // API 3: POST /api/inventory
    // Add book copies to a branch — Librarian/Admin only
    // ─────────────────────────────────────────────────────────
    @Transactional
    public InventoryResponse addInventory(InventoryRequest request) {
 
        // Step 1: Find book
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + request.getBookId()));
 
        // Step 2: Find branch
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch not found with id: " + request.getBranchId()));
 
        // Step 3: Check duplicate — one record per book per branch
        if (inventoryRepository.existsByBookIdAndBranchId(
                request.getBookId(), request.getBranchId())) {
            throw new DuplicateResourceException(
                    "Inventory record already exists for this book " +
                    "in this branch. Use PUT to update copies.");
        }
 
        // Step 4: Build inventory record
        BookInventory inventory = BookInventory.builder()
                .book(book)
                .branch(branch)
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getAvailableCopies() != null
                        ? request.getAvailableCopies()
                        : request.getTotalCopies())
                .shelfLocation(request.getShelfLocation())
                .condition(request.getCondition() != null
                        ? request.getCondition() : "GOOD")
                .build();
 
        // Step 5: Save and return
        return mapToResponse(inventoryRepository.save(inventory));
    }
 
    // ─────────────────────────────────────────────────────────
    // API 4: PUT /api/inventory/{id}
    // Update copy count or shelf location — Librarian/Admin only
    // ─────────────────────────────────────────────────────────
    @Transactional
    public InventoryResponse updateInventory(Long id, InventoryRequest request) {
 
        // Step 1: Find inventory record
        BookInventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory record not found with id: " + id));
 
        // Step 2: Update only provided fields
        if (request.getTotalCopies() != null)
            inventory.setTotalCopies(request.getTotalCopies());
 
        if (request.getAvailableCopies() != null)
            inventory.setAvailableCopies(request.getAvailableCopies());
 
        if (request.getShelfLocation() != null)
            inventory.setShelfLocation(request.getShelfLocation());
 
        if (request.getCondition() != null)
            inventory.setCondition(request.getCondition());
 
        // Step 3: Save and return
        return mapToResponse(inventoryRepository.save(inventory));
    }
 
    // ─────────────────────────────────────────────────────────
    // API 5: DELETE /api/inventory/{id}
    // Remove book from branch inventory — Admin only
    // ─────────────────────────────────────────────────────────
    @Transactional
    public void removeInventory(Long id) {
 
        BookInventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory record not found with id: " + id));
 
        inventoryRepository.delete(inventory);
    }
 
    // ─────────────────────────────────────────────────────────
    // API 6: PUT /api/inventory/{id}/condition
    // Update book physical condition — Librarian only
    // ─────────────────────────────────────────────────────────
    @Transactional
    public InventoryResponse updateCondition(Long id, String condition) {
 
        // Validate condition value
        List<String> validConditions =
                List.of("GOOD", "FAIR", "POOR", "DAMAGED");
 
        if (!validConditions.contains(condition.toUpperCase())) {
            throw new IllegalArgumentException(
                    "Invalid condition. Must be one of: GOOD, FAIR, POOR, DAMAGED");
        }
 
        BookInventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory record not found with id: " + id));
 
        inventory.setCondition(condition.toUpperCase());
        return mapToResponse(inventoryRepository.save(inventory));
    }
 
    // ─────────────────────────────────────────────────────────
    // API 7: GET /api/inventory/low-stock
    // Books with only 1 copy left — Librarian/Admin only
    // ─────────────────────────────────────────────────────────
    public List<InventoryResponse> getLowStockBooks() {
        return inventoryRepository.findAllLowStock()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
 
    // ─────────────────────────────────────────────────────────
    // API 8: GET /api/inventory/out-of-stock
    // Books with 0 available copies — Librarian/Admin only
    // ─────────────────────────────────────────────────────────
    public List<InventoryResponse> getOutOfStockBooks() {
        return inventoryRepository.findAllOutOfStock()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
 
    // ─────────────────────────────────────────────────────────
    // SHARED HELPER — map entity to response DTO
    // ─────────────────────────────────────────────────────────
    private InventoryResponse mapToResponse(BookInventory inv) {
        return InventoryResponse.builder()
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
                .build();
    }
}
 