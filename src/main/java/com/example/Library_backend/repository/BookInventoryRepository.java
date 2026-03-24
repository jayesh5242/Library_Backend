package com.example.Library_backend.repository;

import com.example.Library_backend.entity.Book;
import com.example.Library_backend.entity.BookInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookInventoryRepository extends JpaRepository<BookInventory, Long> {

    // Get all inventory records for a book across all branches
    List<BookInventory> findByBookId(Long bookId);

    // Get paginated books in a branch
    Page<BookInventory> findByBranchId(Long branchId, Pageable pageable);

    // Get all inventory in a branch (no pagination)
    List<BookInventory> findByBranchId(Long branchId);

    // Find specific book in specific branch
    Optional<BookInventory> findByBookIdAndBranchId(Long bookId, Long branchId);

    // Check duplicate — one record per book per branch
    boolean existsByBookIdAndBranchId(Long bookId, Long branchId);

    // Total copies in a branch — used in stats
    @Query(value = "SELECT COALESCE(SUM(i.total_copies), 0) " +
            "FROM book_inventory i WHERE i.branch_id = :branchId",
            nativeQuery = true)
    Integer sumTotalCopiesByBranchId(@Param("branchId") Long branchId);

    // Available copies in a branch — used in stats
    @Query(value = "SELECT COALESCE(SUM(i.available_copies), 0) " +
            "FROM book_inventory i WHERE i.branch_id = :branchId",
            nativeQuery = true)
    Integer sumAvailableCopiesByBranchId(@Param("branchId") Long branchId);

    // Low stock — only 1 copy left
    @Query(value = "SELECT * FROM book_inventory i " +
            "WHERE i.available_copies <= 1",
            nativeQuery = true)
    List<BookInventory> findAllLowStock();

    // Out of stock — 0 available copies
    @Query(value = "SELECT * FROM book_inventory i " +
            "WHERE i.available_copies = 0",
            nativeQuery = true)
    List<BookInventory> findAllOutOfStock();
}
