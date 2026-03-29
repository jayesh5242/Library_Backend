package com.example.Library_backend.service;

import com.example.Library_backend.entity.User;
import com.example.Library_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationTestHelper {

    private final NotificationService notificationService;
    private final UserRepository userRepository;


    // Call this method from Postman via a
    // temporary test endpoint to seed data
    public String createTestNotifications(String email) {

        Optional<User> userOpt =
                userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return "User not found!";
        }

        User user = userOpt.get();

        // Create sample notifications
        notificationService.createNotification(
                user,
                "📚 Book Ready for Pickup!",
                "Your reserved book 'Java Programming' "
                        + "is ready at CS Library. "
                        + "Please collect within 3 days.",
                "BOOK_READY"
        );

        notificationService.createNotification(
                user,
                "⏰ Book Due Tomorrow!",
                "Your book 'Clean Code' is due tomorrow "
                        + "(15 Jan 2024). Please return it on time "
                        + "to avoid fines.",
                "DUE_REMINDER"
        );

        notificationService.createNotification(
                user,
                "💰 Fine Added: ₹20",
                "A fine of ₹20 has been added to your "
                        + "account for 'Design Patterns' "
                        + "(10 days overdue).",
                "FINE_ALERT"
        );

        notificationService.createNotification(
                user,
                "✅ Book Returned Successfully",
                "You have successfully returned "
                        + "'Head First Java'. Thank you!",
                "RETURN_SUCCESS"
        );

        notificationService.createNotification(
                user,
                "📢 Library Holiday Notice",
                "The library will be closed on "
                        + "26 January 2024 for Republic Day. "
                        + "All due dates extended by 1 day.",
                "GENERAL"
        );

        return "5 test notifications created for "
                + user.getFullName() + "!";
    }
}