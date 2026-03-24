package com.example.Library_backend.repository;

import com.example.Library_backend.entity.BorrowTransaction;
import com.example.Library_backend.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BorrowTransactionRepository extends JpaRepository<BorrowTransaction,Long> {
    Page<BorrowTransaction> findByUserId(Long userId, Pageable pageable);

    Page<BorrowTransaction> findByUserIdAndStatus(Long userId, TransactionStatus status, Pageable pageable);

    @Query(value = """
        SELECT *
        FROM borrow_transaction b
        WHERE b.due_date < :now
          AND b.status = 'BORROWED'
    """,
            countQuery = """
        SELECT COUNT(*)
        FROM borrow_transaction b
        WHERE b.due_date < :now
          AND b.status = 'BORROWED'
    """,
            nativeQuery = true)
    Page<BorrowTransaction> findOverdue(LocalDate now, Pageable pageable);

    @Query(value = """
        SELECT b.*
        FROM borrow_transaction b
        JOIN books bk ON b.book_id = bk.id
        WHERE bk.branch_id = :branchId
          AND b.due_date < :now
          AND b.status = 'BORROWED'
    """,
            countQuery = """
        SELECT COUNT(*)
        FROM borrow_transaction b
        JOIN books bk ON b.book_id = bk.id
        WHERE bk.branch_id = :branchId
          AND b.due_date < :now
          AND b.status = 'BORROWED'
    """,
            nativeQuery = true)
    Page<BorrowTransaction> findOverdueByBranch(Long branchId, LocalDate now, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM borrow_transactions " +
            "WHERE branch_id = :branchId AND status = 'BORROWED'",
            nativeQuery = true)
    Integer countActiveBorrowsByBranchId(@Param("branchId") Long branchId);

    // Count overdue books in a branch
    @Query(value = "SELECT COUNT(*) FROM borrow_transactions " +
            "WHERE branch_id = :branchId AND status = 'OVERDUE'",
            nativeQuery = true)
    Integer countOverdueByBranchId(@Param("branchId") Long branchId);

    // Count all borrows ever in a branch
    @Query(value = "SELECT COUNT(*) FROM borrow_transactions " +
            "WHERE branch_id = :branchId",
            nativeQuery = true)
    Integer countAllBorrowsByBranchId(@Param("branchId") Long branchId);

    // All overdue transactions for a branch
    @Query(value = "SELECT * FROM borrow_transactions bt " +
            "WHERE bt.branch_id = :branchId " +
            "AND bt.status = 'OVERDUE' " +
            "ORDER BY bt.due_date ASC",
            nativeQuery = true)
    List<BorrowTransaction> findOverdueByBranchId(
            @Param("branchId") Long branchId);




}
