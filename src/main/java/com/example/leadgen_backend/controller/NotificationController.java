package com.example.leadgen_backend.controller;

import com.example.leadgen_backend.model.User;
import com.example.leadgen_backend.repository.UserRepository;
import com.example.leadgen_backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Operation(summary = "Get my notifications", description = "Retrieve all notifications for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.springframework.data.domain.Page.class)))
    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','BUSINESS','ADMIN')")
    public ResponseEntity<?> getMyNotifications(Authentication authentication, Pageable pageable) {
        User user = getUserFromAuth(authentication);
        return ResponseEntity.ok(notificationService.getUserNotificationsPaged(user.getId(), pageable));
    }

    @Operation(summary = "Get unread notifications", description = "Retrieve unread notifications")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved unread notifications",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = java.util.List.class)))
    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('CUSTOMER','BUSINESS','ADMIN')")
    public ResponseEntity<?> getUnreadNotifications(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        return ResponseEntity.ok(notificationService.getUnreadNotifications(user.getId()));
    }

    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved count",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('CUSTOMER','BUSINESS','ADMIN')")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        return ResponseEntity.ok(notificationService.getUnreadCount(user.getId()));
    }

    @Operation(summary = "Mark as read", description = "Mark a specific notification as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('CUSTOMER','BUSINESS','ADMIN')")
    public ResponseEntity<?> markAsRead(
            @Parameter(description = "Notification ID", required = true) @PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }

    @Operation(summary = "Mark all as read", description = "Mark all notifications as read")
    @ApiResponse(responseCode = "200", description = "All notifications marked as read")
    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('CUSTOMER','BUSINESS','ADMIN')")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok("All notifications marked as read");
    }

    @Operation(summary = "Delete notification", description = "Delete a notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification deleted"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','BUSINESS','ADMIN')")
    public ResponseEntity<?> deleteNotification(
            @Parameter(description = "Notification ID", required = true) @PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok("Notification deleted");
    }

    private User getUserFromAuth(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
