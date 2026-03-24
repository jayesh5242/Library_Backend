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

    @Query(
            value = """
        SELECT *
        FROM notifications
        WHERE user_id = :userId
        ORDER BY created_at DESC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM notifications
        WHERE user_id = :userId
        """,
            nativeQuery = true
    )
    Page<Notification> findByUserIdOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query(
            value = """
        SELECT COUNT(*)
        FROM notifications
        WHERE user_id = :userId
          AND is_read = :isRead
        """,
            nativeQuery = true
    )
    long countByUserAndIsReadNative(
            @Param("userId") Long userId,
            @Param("isRead") Boolean isRead
    );

    // Mark ALL notifications as read for a user
    @Modifying
    @Transactional
    @Query(
            value = """
        UPDATE notifications
        SET is_read = true
        WHERE user_id = :userId
          AND is_read = false
        """,
            nativeQuery = true
    )
    int markAllAsRead(@Param("userId") Long userId);
    // Delete all read notifications for a user
    // (cleanup old notifications)
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n " +
            "WHERE n.user = :user AND n.isRead = true")
    int deleteReadNotifications(@Param("user") User user);
}