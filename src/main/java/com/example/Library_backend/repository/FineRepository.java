package com.example.Library_backend.repository;

import com.example.Library_backend.entity.Fine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {

    // ─────────────────────────────────────────────────────────
    // Used in: BorrowService — check unpaid fines before issuing
    // ─────────────────────────────────────────────────────────
    boolean existsByUserIdAndStatusIn(Long userId, List<String> statuses);

    // ─────────────────────────────────────────────────────────
    // Used in: GET /api/fines/my — user's own fines
    // ─────────────────────────────────────────────────────────
    List<Fine> findByUserId(Long userId);

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

    // ─────────────────────────────────────────────────────────
    // Used in: FineCalculationScheduler — find fine by transaction
    // ─────────────────────────────────────────────────────────
    Optional<Fine> findByTransactionId(Long transactionId);
}
