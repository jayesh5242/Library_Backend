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
    @Query("SELECT b FROM BorrowTransaction b JOIN FETCH b.book JOIN FETCH b.user WHERE b.user.id = :userId")
    Page<BorrowTransaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT b FROM BorrowTransaction b JOIN FETCH b.book JOIN FETCH b.user WHERE b.user.id = :userId AND b.status = :status")
    Page<BorrowTransaction> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TransactionStatus status, Pageable pageable);

    @Query("SELECT b FROM BorrowTransaction b JOIN FETCH b.book JOIN FETCH b.user WHERE b.user.id = :userId AND b.status IN :statuses")
    Page<BorrowTransaction> findByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") java.util.List<TransactionStatus> statuses, Pageable pageable);

    @Query("SELECT b FROM BorrowTransaction b JOIN FETCH b.book JOIN FETCH b.user WHERE b.user.id = :userId AND b.status <> :status")
    Page<BorrowTransaction> findByUserIdAndStatusNot(@Param("userId") Long userId, @Param("status") TransactionStatus status, Pageable pageable);

    @Query("SELECT b FROM BorrowTransaction b JOIN FETCH b.book JOIN FETCH b.user")
    Page<BorrowTransaction> findAllWithDetails(Pageable pageable);

    @Query("SELECT b FROM BorrowTransaction b JOIN FETCH b.book JOIN FETCH b.user WHERE b.id = :id")
    java.util.Optional<BorrowTransaction> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT b FROM BorrowTransaction b JOIN FETCH b.book JOIN FETCH b.user WHERE b.dueDate < :now AND b.status = 'BORROWED'")
    Page<BorrowTransaction> findOverdue(@Param("now") LocalDate now, Pageable pageable);

    @Query("SELECT b FROM BorrowTransaction b JOIN FETCH b.book JOIN FETCH b.user WHERE b.branch.id = :branchId AND b.dueDate < :now AND b.status = 'BORROWED'")
    Page<BorrowTransaction> findOverdueByBranch(@Param("branchId") Long branchId, @Param("now") LocalDate now, Pageable pageable);

    // ✅ 1. Count by Status
    @Query(value = "SELECT COUNT(*) FROM borrow_transactions WHERE status = :status", nativeQuery = true)
    long countByStatus(@Param("status") String status);


    // ✅ 2. Count Active Borrowings for User
    @Query(value = "SELECT COUNT(*) FROM borrow_transactions WHERE user_id = :userId AND status = :status", nativeQuery = true)
    long countByUserAndStatus(@Param("userId") Long userId,
                              @Param("status") String status);


    // ✅ 3. Get Transactions by Status
    @Query("SELECT b FROM BorrowTransaction b JOIN FETCH b.book JOIN FETCH b.user WHERE b.status = :status")
    List<BorrowTransaction> findByStatus(@Param("status") TransactionStatus status);


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
    @Query("SELECT b FROM BorrowTransaction b JOIN FETCH b.book JOIN FETCH b.user WHERE b.status = 'OVERDUE' OR (b.status = 'BORROWED' AND b.dueDate < :today)")
    List<BorrowTransaction> getOverdueTransactions(@Param("today") LocalDate today);


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
    @Query("SELECT b FROM BorrowTransaction b JOIN FETCH b.book JOIN FETCH b.user WHERE b.branch.id = :branchId AND b.status = 'OVERDUE' ORDER BY b.dueDate ASC")
    List<BorrowTransaction> findOverdueByBranchId(@Param("branchId") Long branchId);
}



