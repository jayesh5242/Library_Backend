package com.example.Library_backend.service;

import com.example.Library_backend.dto.request.notificationrequest.SendNotificationRequest;
import com.example.Library_backend.dto.response.authresponse.PagedResponse;
import com.example.Library_backend.dto.response.notificationresponse.NotificationResponse;
import com.example.Library_backend.entity.Notification;
import com.example.Library_backend.entity.User;
import com.example.Library_backend.exception.ResourceNotFoundException;
import com.example.Library_backend.repository.NotificationRepository;
import com.example.Library_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final EmailService emailService;

    // ═══════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════

    // ─── Convert entity to response DTO ──────────────
    private NotificationResponse toResponse(
            Notification notification) {

        NotificationResponse response =
                new NotificationResponse();

        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setIsRead(notification.getIsRead());
        response.setCreatedAt(notification.getCreatedAt());

        // Set human readable time
        // "Just now", "5 min ago", "2 hours ago" etc.
        response.setTimeAgo(
                getTimeAgo(notification.getCreatedAt()));

        // Set emoji icon based on type
        response.setIcon(
                getIconForType(notification.getType()));

        return response;
    }

    // ─── Calculate "time ago" text ────────────────────
    private String getTimeAgo(LocalDateTime createdAt) {
        if (createdAt == null) return "Just now";

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(
                createdAt, now);
        long hours   = ChronoUnit.HOURS.between(
                createdAt, now);
        long days    = ChronoUnit.DAYS.between(
                createdAt, now);

        if (minutes < 1)  return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (hours   < 24) return hours   + " hours ago";
        if (days    < 7)  return days    + " days ago";
        return createdAt.toLocalDate().toString();
    }

    // ─── Get icon emoji based on notification type ────
    private String getIconForType(String type) {
        if (type == null) return "🔔";
        switch (type) {
            case "DUE_REMINDER"   : return "⏰";
            case "FINE_ALERT"     : return "💰";
            case "BOOK_READY"     : return "📚";
            case "RETURN_SUCCESS" : return "✅";
            case "TRANSFER_UPDATE": return "🔄";
            case "PURCHASE_UPDATE": return "🛒";
            case "ACCOUNT_BLOCKED": return "🚫";
            case "GENERAL"        : return "📢";
            default               : return "🔔";
        }
    }

    public Notification createNotification(
            User user,
            String title,
            String message,
            String type) {

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);

        return notificationRepository.save(notification);
    }

    // ─── Create notification AND send email ───────────
    public void createNotificationWithEmail(
            User user,
            String title,
            String message,
            String type) {

        // 1. Save in-app notification
        createNotification(user, title, message, type);

        // 2. Also send email
        emailService.sendEmail(
                user.getEmail(),
                title + " - College Library",
                "Hello " + user.getFullName() + "!\n\n"
                        + message + "\n\n"
                        + "College Library Team"
        );
    }


    // ─── API 1: GET MY NOTIFICATIONS ──────────────────
    public PagedResponse<NotificationResponse>
    getMyNotifications(int page, int size) {

        // Get current logged in user
        User user = currentUserService.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size);

        // Get notifications newest first
        Page<Notification> notifPage =
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                user.getId(), pageable);

        List<NotificationResponse> notifications =
                notifPage.getContent()
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());

        return new PagedResponse<>(
                notifications,
                notifPage.getNumber(),
                notifPage.getSize(),
                notifPage.getTotalElements(),
                notifPage.getTotalPages(),
                notifPage.isLast(),
                "Notifications fetched successfully!"
        );
    }

    // ─── API 2: MARK ONE AS READ ──────────────────────
    public NotificationResponse markAsRead(Long id) {

        User user = currentUserService.getCurrentUser();

        // Find the notification
        Notification notification =
                notificationRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found!"));

        // Security: make sure this notification
        // belongs to the current user!
        if (!notification.getUser().getId()
                .equals(user.getId())) {
            throw new RuntimeException(
                    "You can only read your own notifications!");
        }

        // Mark as read
        notification.setIsRead(true);
        notificationRepository.save(notification);

        return toResponse(notification);
    }

    // ─── API 3: MARK ALL AS READ ──────────────────────
    public String markAllAsRead() {

        User user = currentUserService.getCurrentUser();

        // Count unread before marking
        long unreadCount = notificationRepository
                .countByUserAndIsReadNative(user.getId(), false);

        if (unreadCount == 0) {
            return "No unread notifications found!";
        }

        // Mark all as read in one DB query
        int updated = notificationRepository
                .markAllAsRead(user.getId());

        return updated + " notifications marked as read!";
    }

    // ─── API 4: DELETE ONE NOTIFICATION ──────────────
    public String deleteNotification(Long id) {

        User user = currentUserService.getCurrentUser();

        Notification notification =
                notificationRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found!"));

        // Security: only owner can delete
        if (!notification.getUser().getId()
                .equals(user.getId())) {
            throw new RuntimeException(
                    "You can only delete your own notifications!");
        }

        notificationRepository.delete(notification);

        return "Notification deleted successfully!";
    }

    // ─── API 5: GET UNREAD COUNT ──────────────────────
    public long getUnreadCount() {

        User user = currentUserService.getCurrentUser();

        return notificationRepository
                .countByUserAndIsReadNative(user.getId(), false);
    }

    // ─── API 6: BROADCAST TO ALL USERS ───────────────
    public String broadcastNotification(
            SendNotificationRequest request) {

        // Get all active users from DB
        List<User> allUsers = userRepository.findAll();

        int count = 0;
        for (User user : allUsers) {
            // Only send to active users
            if (user.getIsActive()) {
                createNotification(
                        user,
                        request.getTitle(),
                        request.getMessage(),
                        request.getType()
                );

                // Send email too if requested
                if (Boolean.TRUE.equals(
                        request.getSendEmail())) {
                    emailService.sendEmail(
                            user.getEmail(),
                            request.getTitle()
                                    + " - College Library",
                            "Hello " + user.getFullName()
                                    + "!\n\n"
                                    + request.getMessage()
                                    + "\n\nCollege Library Team"
                    );
                }
                count++;
            }
        }

        return "Notification broadcast to "
                + count + " users successfully!";
    }

    // ─── API 7: SEND TO SPECIFIC USER ─────────────────
    public String sendToUser(
            SendNotificationRequest request) {

        // Validate userId provided
        if (request.getUserId() == null) {
            throw new RuntimeException(
                    "userId is required!");
        }

        // Find the target user
        User targetUser = userRepository
                .findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: "
                                        + request.getUserId()));

        // Check user is active
        if (!targetUser.getIsActive()) {
            throw new RuntimeException(
                    "Cannot send notification to "
                            + "blocked user!");
        }

        // Create notification
        createNotification(
                targetUser,
                request.getTitle(),
                request.getMessage(),
                request.getType()
        );

        // Send email if requested
        if (Boolean.TRUE.equals(request.getSendEmail())) {
            emailService.sendEmail(
                    targetUser.getEmail(),
                    request.getTitle()
                            + " - College Library",
                    "Hello " + targetUser.getFullName()
                            + "!\n\n"
                            + request.getMessage()
                            + "\n\nCollege Library Team"
            );
        }

        return "Notification sent to "
                + targetUser.getFullName()
                + " successfully!";
    }
}