package com.example.Library_backend.repository;

import com.example.Library_backend.entity.Reservation;
import com.example.Library_backend.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    Page<Reservation> findAllByStatus(ReservationStatus status, Pageable pageable);

    @Query("""
        SELECT r FROM Reservation r 
        WHERE r.book.branch.id = :branchId 
        AND r.status = 'PENDING'
    """)
    Page<Reservation> findPendingByBranch(Long branchId, Pageable pageable);

    Page<Reservation> findByBookId(Long bookId, Pageable pageable);
}
