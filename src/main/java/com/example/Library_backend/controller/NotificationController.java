package com.example.Library_backend.controller;

import com.example.Library_backend.dto.request.notificationrequest.SendNotificationRequest;
import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.PagedResponse;
import com.example.Library_backend.dto.response.notificationresponse.NotificationResponse;
import com.example.Library_backend.service.NotificationService;
import com.example.Library_backend.service.NotificationTestHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private  final NotificationTestHelper notificationTestHelper;

    // ─── API 1: GET MY NOTIFICATIONS ──────────────────
    // GET /api/notifications/my?page=0&size=10
    @GetMapping("/my")
    public ResponseEntity<ApiResponse> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<NotificationResponse> notifications =
                notificationService
                        .getMyNotifications(page, size);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notifications fetched!", notifications));
    }

    // ─── API 2: MARK ONE AS READ ──────────────────────
    // PUT /api/notifications/{id}/read
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse> markAsRead(
            @PathVariable Long id) {

        NotificationResponse notification =
                notificationService.markAsRead(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notification marked as read!",
                        notification));
    }

    // ─── API 3: MARK ALL AS READ ──────────────────────
    // PUT /api/notifications/read-all
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse> markAllAsRead() {

        String message =
                notificationService.markAllAsRead();

        return ResponseEntity.ok(
                ApiResponse.success(message, null));
    }

    // ─── API 4: DELETE ONE NOTIFICATION ──────────────
    // DELETE /api/notifications/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteNotification(
            @PathVariable Long id) {

        String message =
                notificationService.deleteNotification(id);

        return ResponseEntity.ok(
                ApiResponse.success(message, null));
    }

    // ─── API 5: GET UNREAD COUNT ──────────────────────
    // GET /api/notifications/unread-count
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse> getUnreadCount() {

        long count = notificationService.getUnreadCount();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Unread count fetched!",
                        count));
    }

    // ─── API 6: BROADCAST TO ALL ──────────────────────
    // POST /api/notifications/broadcast
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> broadcast(
            @Valid @RequestBody
            SendNotificationRequest request) {

        String message =
                notificationService
                        .broadcastNotification(request);

        return ResponseEntity.ok(
                ApiResponse.success(message, null));
    }

    // ─── API 7: SEND TO SPECIFIC USER ─────────────────
    // POST /api/notifications/send
    @PostMapping("/send")
    @PreAuthorize(
            "hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> sendToUser(
            @Valid @RequestBody
            SendNotificationRequest request) {

        String message =
                notificationService.sendToUser(request);

        return ResponseEntity.ok(
                ApiResponse.success(message, null));
    }


    // ─── TEMP: Create test notifications ─────────────
// DELETE THIS AFTER TESTING!
    @PostMapping("/test-seed/{email}")
    public ResponseEntity<ApiResponse> seedTestData(
            @PathVariable String email) {

        String message =
                notificationTestHelper
                        .createTestNotifications(email);

        return ResponseEntity.ok(
                ApiResponse.success(message, null));
    }
}