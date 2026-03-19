package com.example.Library_backend.repository;

import com.example.Library_backend.entity.Notification;
import com.example.Library_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    // Get all notifications for a user
    // (newest first)
    Page<Notification> findByUserOrderByCreatedAtDesc(
            User user, Pageable pageable);

    // Count unread notifications for a user
    long countByUserAndIsRead(User user, Boolean isRead);

    // Mark ALL notifications as read for a user
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true " +
            "WHERE n.user = :user AND n.isRead = false")
    int markAllAsRead(@Param("user") User user);

    // Delete all read notifications for a user
    // (cleanup old notifications)
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n " +
            "WHERE n.user = :user AND n.isRead = true")
    int deleteReadNotifications(@Param("user") User user);
}