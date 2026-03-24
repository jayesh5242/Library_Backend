package com.example.Library_backend.repository;

import com.example.Library_backend.entity.Fine;
import com.example.Library_backend.enums.FineStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FineRepository
        extends JpaRepository<Fine, Long> {

    // ─────────────────────────────────────────────────────────
    // Used in: GET /api/fines/my — user's own fines
    // ─────────────────────────────────────────────────────────
    List<Fine> findByUserId(Long userId);
    @Query("SELECT f FROM Fine f WHERE f.user.id = :userId")
    Page<Fine> findByUserIdWithPagination(@Param("userId") Long userId, Pageable pageable);

    // ─────────────────────────────────────────────────────────
    // Used in: GET /api/fines/my/total — total outstanding amount
    // ─────────────────────────────────────────────────────────
    @Query(value = "SELECT COALESCE(SUM(f.total_amount - f.paid_amount), 0) " +
            "FROM fines f WHERE f.user_id = :userId " +
            "AND f.status IN ('PENDING', 'PARTIAL')",
            nativeQuery = true)
    Double sumOutstandingFinesByUserId(@Param("userId") Long userId);

    // ─────────────────────────────────────────────────────────
    // Used in: GET /api/fines/all — all fines paginated
    // ─────────────────────────────────────────────────────────
    Page<Fine> findAll(Pageable pageable);

    // ─────────────────────────────────────────────────────────
    // Used in: GET /api/fines/pending — all unpaid fines
    // ─────────────────────────────────────────────────────────
    @Query(value = "SELECT * FROM fines f " +
            "WHERE f.status IN ('PENDING', 'PARTIAL') " +
            "ORDER BY f.created_at DESC",
            nativeQuery = true)
    List<Fine> findAllPendingFines();

    // ─────────────────────────────────────────────────────────
    // Used in: GET /api/fines/branch/{id} — fines for a branch
    // ─────────────────────────────────────────────────────────
    @Query(value = "SELECT f.* FROM fines f " +
            "JOIN borrow_transactions bt ON f.transaction_id = bt.id " +
            "WHERE bt.branch_id = :branchId " +
            "ORDER BY f.created_at DESC",
            nativeQuery = true)
    List<Fine> findFinesByBranchId(@Param("branchId") Long branchId);

    // ─────────────────────────────────────────────────────────
    // Used in: GET /api/fines/user/{userId} — fines for a user
    // ─────────────────────────────────────────────────────────
    List<Fine> findByUserIdOrderByCreatedAtDesc(Long userId);

    // ─────────────────────────────────────────────────────────
    // Used in: GET /api/fines/summary — fine collection summary
    // ─────────────────────────────────────────────────────────
    @Query(value = "SELECT COALESCE(SUM(f.paid_amount), 0) FROM fines f " +
            "WHERE f.status IN ('PAID', 'PARTIAL')",
            nativeQuery = true)
    Double sumTotalFinesCollected();

    @Query(value = "SELECT COALESCE(SUM(f.total_amount - f.paid_amount), 0) " +
            "FROM fines f WHERE f.status IN ('PENDING', 'PARTIAL')",
            nativeQuery = true)
    Double sumTotalFinesPending();

    // ─────────────────────────────────────────────────────────
    // Used in: Branch stats API — fines per branch
    // ─────────────────────────────────────────────────────────
    @Query(value = "SELECT COALESCE(SUM(f.total_amount - f.paid_amount), 0) " +
            "FROM fines f " +
            "JOIN borrow_transactions bt ON f.transaction_id = bt.id " +
            "WHERE bt.branch_id = :branchId AND f.status IN ('PENDING','PARTIAL')",
            nativeQuery = true)
    Double sumPendingFinesByBranchId(@Param("branchId") Long branchId);

    @Query(value = "SELECT COALESCE(SUM(f.paid_amount), 0) " +
            "FROM fines f " +
            "JOIN borrow_transactions bt ON f.transaction_id = bt.id " +
            "WHERE bt.branch_id = :branchId AND f.status IN ('PAID','PARTIAL')",
            nativeQuery = true)
    Double sumCollectedFinesByBranchId(@Param("branchId") Long branchId);

    // ✅ 1. Find fine by transaction
    @Query(value = "SELECT * FROM fines WHERE transaction_id = :transactionId", nativeQuery = true)
    Optional<Fine> findByTransactionId(@Param("transactionId") Long transactionId);


    // ✅ 2. Get pending fines for user
    @Query(value = "SELECT * FROM fines WHERE user_id = :userId AND status = :status", nativeQuery = true)
    List<Fine> findByUserAndStatus(@Param("userId") Long userId,
                                   @Param("status") String status);


    // ✅ 3. Total pending fines amount
    @Query(value = """
        SELECT COALESCE(SUM(total_amount - paid_amount), 0)
        FROM fines
        WHERE status = 'PENDING'
           OR status = 'PARTIAL'
        """, nativeQuery = true)
    Double getTotalPendingFinesAmount();


    // ✅ 4. Monthly fine collection
    @Query(value = """
        SELECT EXTRACT(MONTH FROM paid_at) AS month,
               SUM(paid_amount) AS total
        FROM fines
        WHERE status = 'PAID'
          AND EXTRACT(YEAR FROM paid_at) = :year
        GROUP BY month
        ORDER BY month
        """, nativeQuery = true)
    List<Object[]> getMonthlyFineCollection(@Param("year") int year);


    // ✅ 5. Count fines by status
    @Query(value = "SELECT COUNT(*) FROM fines WHERE status = :status", nativeQuery = true)
    long countByStatus(@Param("status") String status);


    // ✅ 6. Total collected amount
    @Query(value = """
        SELECT COALESCE(SUM(paid_amount), 0)
        FROM fines
        WHERE status = 'PAID'
        """, nativeQuery = true)
    Double getTotalCollectedAmount();

    Page<Fine> findByStatus(FineStatus status, Pageable pageable);

    @Query(value = """
    SELECT 
        f.*
    FROM fine f
    INNER JOIN transaction t ON f.transaction_id = t.id
    INNER JOIN book b ON t.book_id = b.id
    INNER JOIN branch br ON b.branch_id = br.id
    WHERE br.id = :branchId
    """,
            countQuery = """
    SELECT COUNT(f.id)
    FROM fine f
    INNER JOIN transaction t ON f.transaction_id = t.id
    INNER JOIN book b ON t.book_id = b.id
    INNER JOIN branch br ON b.branch_id = br.id
    WHERE br.id = :branchId
    """,
            nativeQuery = true)
    Page<Fine> findByBranch(
            @Param("branchId") Long branchId,
            Pageable pageable);

    List<Fine> findByUserIdAndStatus(Long userId, FineStatus status);
}