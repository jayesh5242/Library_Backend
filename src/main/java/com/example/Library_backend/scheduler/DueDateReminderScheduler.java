package com.example.Library_backend.scheduler;

import com.example.Library_backend.entity.BorrowTransaction;
import com.example.Library_backend.enums.TransactionStatus;
import com.example.Library_backend.repository.BorrowTransactionRepository;
import com.example.Library_backend.service.EmailService;
import com.example.Library_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DueDateReminderScheduler {

    private final BorrowTransactionRepository borrowRepo;
    private final EmailService emailService;
    private final NotificationService notificationService;

    // Runs every day at 9:00 AM
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDueDateReminders() {
        log.info("=== DueDateReminderScheduler started at 9:00 AM ===");

        // Find all books due in exactly 3 days
        LocalDate threeDaysFromNow = LocalDate.now().plusDays(3);

        List<BorrowTransaction> dueSoon = borrowRepo
                .findByStatusAndDueDate(
                        String.valueOf(TransactionStatus.BORROWED),
                        threeDaysFromNow
                );

        int remindersSent = 0;

        for (BorrowTransaction tx : dueSoon) {
            String studentEmail = tx.getUser().getEmail();
            String studentName  = tx.getUser().getFullName();
            String bookTitle    = tx.getBook().getTitle();
            String dueDate      = tx.getDueDate().toString();

            // 1. Send email reminder
            emailService.sendDueDateReminderEmail(
                    studentEmail, studentName, bookTitle, dueDate);

            // 2. Create in-app notification
            notificationService.createNotification(
                    tx.getUser(),
                    "⏰ Book Due in 3 Days!",
                    "Your book '" + bookTitle + "' is due on " + dueDate
                            + ". Please return on time to avoid a fine of Rs.2/day.",
                    "DUE_REMINDER"
            );

            remindersSent++;
        }

        log.info("=== DueDateReminderScheduler done — sent {} reminders ===",
                remindersSent);
    }
}