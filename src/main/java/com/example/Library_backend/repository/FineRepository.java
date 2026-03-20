package com.example.Library_backend.repository;

import com.example.Library_backend.entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FineRepository
        extends JpaRepository<Fine, Long> {



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
}