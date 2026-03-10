package com.example.leadgen_backend.controller;

import com.example.leadgen_backend.model.User;
import com.example.leadgen_backend.repository.UserRepository;
import com.example.leadgen_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','BUSINESS','ADMIN')")
    public ResponseEntity<?> getMyNotifications(Authentication authentication, Pageable pageable) {
        User user = getUserFromAuth(authentication);
        return ResponseEntity.ok(notificationService.getUserNotificationsPaged(user.getId(), pageable));
    }

    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('CUSTOMER','BUSINESS','ADMIN')")
    public ResponseEntity<?> getUnreadNotifications(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        return ResponseEntity.ok(notificationService.getUnreadNotifications(user.getId()));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('CUSTOMER','BUSINESS','ADMIN')")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        return ResponseEntity.ok(notificationService.getUnreadCount(user.getId()));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('CUSTOMER','BUSINESS','ADMIN')")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('CUSTOMER','BUSINESS','ADMIN')")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok("All notifications marked as read");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','BUSINESS','ADMIN')")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok("Notification deleted");
    }

    private User getUserFromAuth(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
