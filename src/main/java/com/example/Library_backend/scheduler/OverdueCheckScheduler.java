package com.example.Library_backend.scheduler;

import com.example.Library_backend.entity.BorrowTransaction;
import com.example.Library_backend.enums.TransactionStatus;
import com.example.Library_backend.repository.BorrowTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueCheckScheduler {

    private final BorrowTransactionRepository borrowRepo;

    // Runs every day at 6:00 AM
    // Cron format: second minute hour day month weekday
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void markOverdueBooks() {
        log.info("=== OverdueCheckScheduler started at 6:00 AM ===");

        // Find all books that are still BORROWED but due date has passed
        List<BorrowTransaction> overdue = borrowRepo
                .findByStatusAndDueDateBefore(
                        String.valueOf(TransactionStatus.BORROWED),
                        LocalDate.now()
                );

        int count = 0;
        for (BorrowTransaction tx : overdue) {
            tx.setStatus(TransactionStatus.OVERDUE);
            borrowRepo.save(tx);
            count++;
        }

        log.info("=== OverdueCheckScheduler done — marked {} books as OVERDUE ===", count);
    }
}