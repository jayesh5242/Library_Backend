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
        FROM borrow_transactions b
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
        FROM borrow_transactions b
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

    // ✅ 1. Count by Status
    @Query(value = "SELECT COUNT(*) FROM borrow_transactions WHERE status = :status", nativeQuery = true)
    long countByStatus(@Param("status") String status);


    // ✅ 2. Count Active Borrowings for User
    @Query(value = "SELECT COUNT(*) FROM borrow_transactions WHERE user_id = :userId AND status = :status", nativeQuery = true)
    long countByUserAndStatus(@Param("userId") Long userId,
                              @Param("status") String status);


    // ✅ 3. Get Transactions by Status
    @Query(value = "SELECT * FROM borrow_transactions WHERE status = :status", nativeQuery = true)
    List<BorrowTransaction> findByStatus(@Param("status") String status);


    // ✅ 4. Monthly Borrowing Count
    @Query(value = """
        SELECT EXTRACT(MONTH FROM issue_date) AS month,
               COUNT(*) AS count
        FROM borrow_transactions
        WHERE EXTRACT(YEAR FROM issue_date) = :year
        GROUP BY month
        ORDER BY month
        """, nativeQuery = true)
    List<Object[]> getMonthlyBorrowingCounts(@Param("year") int year);


    // ✅ 5. Top Borrowed Books
    @Query(value = """
        SELECT book_id, COUNT(*) AS cnt
        FROM borrow_transactions
        GROUP BY book_id
        ORDER BY cnt DESC
        """, nativeQuery = true)
    List<Object[]> getTopBorrowedBooks(Pageable pageable);


    // ✅ 6. Most Active Users
    @Query(value = """
        SELECT user_id, COUNT(*) AS cnt
        FROM borrow_transactions
        GROUP BY user_id
        ORDER BY cnt DESC
        """, nativeQuery = true)
    List<Object[]> getMostActiveUsers(Pageable pageable);


    // ✅ 7. Branch-wise Borrowing Count
    @Query(value = """
        SELECT br.name, COUNT(*)
        FROM borrow_transactions bt
        JOIN branches br ON bt.branch_id = br.id
        GROUP BY br.name
        """, nativeQuery = true)
    List<Object[]> getBranchWiseBorrowings();


    // ✅ 8. Category-wise Borrowing
    @Query(value = """
        SELECT bk.category, COUNT(*)
        FROM borrow_transactions bt
        JOIN books bk ON bt.book_id = bk.id
        WHERE bk.category IS NOT NULL
        GROUP BY bk.category
        ORDER BY COUNT(*) DESC
        """, nativeQuery = true)
    List<Object[]> getCategoryWiseBorrowings();


    // ✅ 9. Overdue Transactions
    @Query(value = """
        SELECT *
        FROM borrow_transactions
        WHERE status = 'OVERDUE'
           OR (status = 'BORROWED' AND due_date < :today)
        """, nativeQuery = true)
    List<BorrowTransaction> getOverdueTransactions(@Param("today") LocalDate today);

}



