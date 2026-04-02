package com.example.Library_backend.scheduler;

import com.example.Library_backend.entity.Reservation;
import com.example.Library_backend.enums.ReservationStatus;
import com.example.Library_backend.repository.ReservationRepository;
import com.example.Library_backend.service.NotificationService;
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
public class ReservationExpiryScheduler {

    private final ReservationRepository reservationRepo;
    private final NotificationService notificationService;

    // Runs every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void expireOldReservations() {
        log.info("=== ReservationExpiryScheduler started at 8:00 AM ===");

        // Find READY reservations where readyDate was more than 3 days ago
        LocalDate expiryCutoff = LocalDate.now().minusDays(3);

        List<Reservation> toExpire = reservationRepo
                .findByStatusAndReadyDateBefore(
                        String.valueOf(ReservationStatus.READY),
                        expiryCutoff
                );

        int expired = 0;

        for (Reservation r : toExpire) {
            r.setStatus(ReservationStatus.EXPIRED);
            reservationRepo.save(r);

            // Notify student their reservation expired
            notificationService.createNotification(
                    r.getUser(),
                    "📋 Reservation Expired",
                    "Your reservation for '" + r.getBook().getTitle()
                            + "' has expired as it was not collected within 3 days.",
                    "GENERAL"
            );

            expired++;
        }

        log.info("=== ReservationExpiryScheduler done — expired {} reservations ===",
                expired);
    }
}