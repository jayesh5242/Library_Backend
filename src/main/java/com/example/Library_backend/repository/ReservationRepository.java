package com.example.Library_backend.repository;

import com.example.Library_backend.entity.Reservation;
import com.example.Library_backend.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    Page<Reservation> findAllByStatus(ReservationStatus status, Pageable pageable);

    @Query(
            value = """
        SELECT r.*
        FROM reservation r
        JOIN book b ON r.book_id = b.id
        WHERE b.branch_id = :branchId
        AND r.status = 'PENDING'
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM reservation r
        JOIN book b ON r.book_id = b.id
        WHERE b.branch_id = :branchId
        AND r.status = 'PENDING'
        """,
            nativeQuery = true
    )
    Page<Reservation> findPendingByBranch(@Param("branchId") Long branchId, Pageable pageable);

    Page<Reservation> findByBookId(Long bookId, Pageable pageable);


    @Query(value = "SELECT COUNT(*) FROM reservations WHERE status = :status", nativeQuery = true)
    long countByStatus(@Param("status") String status);
}
