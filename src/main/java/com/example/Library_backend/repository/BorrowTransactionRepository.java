package com.example.Library_backend.repository;

import com.example.Library_backend.entity.BorrowTransaction;
import com.example.Library_backend.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
}
