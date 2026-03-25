package com.example.Library_backend.repository;

import com.example.Library_backend.entity.BookPurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
 
@Repository
public interface PurchaseRequestRepository
        extends JpaRepository<BookPurchaseRequest, Long> {

    // Faculty's own requests
    List<BookPurchaseRequest> findByRequestedById(Long userId);

    // All requests — for librarian/admin
    List<BookPurchaseRequest> findAllByOrderByCreatedAtDesc();
}