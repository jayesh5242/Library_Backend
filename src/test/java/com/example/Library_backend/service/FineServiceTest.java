package com.example.Library_backend.service;

import com.example.Library_backend.entity.BorrowTransaction;
import com.example.Library_backend.entity.Fine;
import com.example.Library_backend.entity.User;
import com.example.Library_backend.enums.FineStatus;
import com.example.Library_backend.enums.TransactionStatus;
import com.example.Library_backend.repository.FineRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FineServiceTest {
//
//    @Mock private FineRepository fineRepository;
//    @Mock private NotificationService notificationService;
//
//    @InjectMocks private FineService fineService;
//
//    // ─── TEST 1: Fine Calculation is Correct ──────────
//    @Test
//    void calculateFine_For10DaysOverdue_ShouldBeRs20() {
//        // ARRANGE: Book was due 10 days ago
//        BorrowTransaction transaction = new BorrowTransaction();
//        User user = new User();
//        user.setId(1L);
//        user.setEmail("student@test.com");
//        user.setFullName("Test Student");
//        transaction.setUser(user);
//        transaction.setDueDate(
//                LocalDate.now().minusDays(10)); // 10 days late
//        transaction.setStatus(TransactionStatus.OVERDUE);
//
//        when(fineRepository.findByTransactionId(any()))
//                .thenReturn(Optional.empty()); // no fine yet
//        when(fineRepository.save(any(Fine.class)))
//                .thenAnswer(i -> i.getArguments()[0]);
//
//        // ACT
//        Fine fine = fineService.calculateFine(transaction);
//
//        // ASSERT: 10 days × Rs.2 = Rs.20
//        assertNotNull(fine);
//        assertEquals(10, fine.getDaysOverdue());
//        assertEquals(20.0, fine.getTotalAmount());
//        assertEquals(FineStatus.PENDING, fine.getStatus());
//    }
//
//    // ─── TEST 2: No Fine for On-Time Return ───────────
//    @Test
//    void calculateFine_ForOnTimeReturn_ShouldBeZero() {
//        BorrowTransaction transaction = new BorrowTransaction();
//        User user = new User();
//        user.setId(1L);
//        transaction.setUser(user);
//        transaction.setDueDate(
//                LocalDate.now().plusDays(5)); // still 5 days left
//        transaction.setStatus(TransactionStatus.BORROWED);
//
//        // ACT: Not overdue, fine should not be created
//        // Method should return null or throw no exception
//        assertDoesNotThrow(
//                () -> fineService.calculateFine(transaction)
//        );
//    }
}