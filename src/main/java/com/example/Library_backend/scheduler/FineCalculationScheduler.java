package com.example.Library_backend.scheduler;

import com.example.Library_backend.entity.BorrowTransaction;
import com.example.Library_backend.entity.Fine;
import com.example.Library_backend.enums.FineStatus;
import com.example.Library_backend.enums.TransactionStatus;
import com.example.Library_backend.repository.BorrowTransactionRepository;
import com.example.Library_backend.repository.FineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class FineCalculationScheduler {

    private final BorrowTransactionRepository borrowRepo;
    private final FineRepository fineRepository;

    // Fine rate — Rs. 2 per day
    private static final double FINE_PER_DAY = 2.0;

    // Runs every day at 6:05 AM (5 minutes after overdue check)
    @Scheduled(cron = "0 5 6 * * *")
    @Transactional
    public void calculateFines() {
        log.info("=== FineCalculationScheduler started at 6:05 AM ===");

        // Get all OVERDUE transactions
        List<BorrowTransaction> overdueList = borrowRepo
                .findByStatus(String.valueOf(TransactionStatus.OVERDUE));

        int created = 0, updated = 0;

        for (BorrowTransaction tx : overdueList) {
            // Calculate how many days overdue
            long daysOverdue = LocalDate.now().toEpochDay()
                    - tx.getDueDate().toEpochDay();

            if (daysOverdue <= 0) continue;

            double totalAmount = daysOverdue * FINE_PER_DAY;

            // Check if a fine already exists for this transaction
            Optional<Fine> existingFine =
                    fineRepository.findByTransactionId(tx.getId());

            if (existingFine.isPresent()) {
                // Update existing fine amount
                Fine fine = existingFine.get();
                if (fine.getStatus() == FineStatus.PENDING) {
                    fine.setDaysOverdue((int) daysOverdue);
                    fine.setTotalAmount(totalAmount);
                    fineRepository.save(fine);
                    updated++;
                }
            } else {
                // Create new fine record
                Fine fine = new Fine();
                fine.setTransaction(tx);
                fine.setUser(tx.getUser());
                fine.setDaysOverdue((int) daysOverdue);
                fine.setAmountPerDay(FINE_PER_DAY);
                fine.setTotalAmount(totalAmount);
                fine.setPaidAmount(0.0);
                fine.setStatus(FineStatus.PENDING);
                fineRepository.save(fine);
                created++;
            }
        }

        log.info("=== FineCalculationScheduler done — created: {}, updated: {} fines ===",
                created, updated);
    }
}