package com.example.Library_backend.repository;

import com.example.Library_backend.entity.Book;
import com.example.Library_backend.entity.BookInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookInventoryRepository extends JpaRepository<BookInventory, Long> {

    // Get all inventory records for a book across all branches

    List<BookInventory> findByBookId(Long bookId);


    // ✅ 1. Get inventory for a branch
    @Query(value = "SELECT * FROM book_inventory WHERE branch_id = :branchId", nativeQuery = true)
    List<BookInventory> findByBranch(@Param("branchId") Long branchId);


    // ✅ 2. Find specific book in specific branch
    @Query(value = "SELECT * FROM book_inventory WHERE book_id = :bookId AND branch_id = :branchId", nativeQuery = true)
    Optional<BookInventory> findByBookIdAndBranchId(@Param("bookId") Long bookId,
                                                    @Param("branchId") Long branchId);


    // ✅ 3. Count total books in system
    @Query(value = "SELECT COALESCE(SUM(total_copies), 0) FROM book_inventory", nativeQuery = true)
    Long getTotalBooks();


    // ✅ 4. Count available books
    @Query(value = "SELECT COALESCE(SUM(available_copies), 0) FROM book_inventory", nativeQuery = true)
    Long getTotalAvailableBooks();


    // ✅ 5. Get out of stock books
    @Query(value = "SELECT * FROM book_inventory WHERE available_copies = 0", nativeQuery = true)
    List<BookInventory> getOutOfStockBooks();
}
