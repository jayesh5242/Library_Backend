package com.example.Library_backend.repository;

import com.example.Library_backend.entity.Book;
import com.example.Library_backend.entity.BookInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookInventoryRepository extends JpaRepository<BookInventory, Long> {

    // Get all inventory records for a book across all branches

    List<BookInventory> findByBookId(Long bookId);
}
