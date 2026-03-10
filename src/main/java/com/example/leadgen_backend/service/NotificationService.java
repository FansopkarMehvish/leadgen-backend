package com.example.leadgen_backend.service;

import com.example.leadgen_backend.model.Notification;
import com.example.leadgen_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification createNotification(Long userId, String type, String title, String message,
                                            Long relatedLeadId, Long relatedAssignmentId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .relatedLeadId(relatedLeadId)
                .relatedAssignmentId(relatedAssignmentId)
                .isRead(false)
                .channel("IN_APP")
                .sentAt(Instant.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Created notification for user {}: {}", userId, title);

        return saved;
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setIsRead(true);
        notification.setReadAt(Instant.now());
        notificationRepository.save(notification);
        log.info("Marked notification {} as read", notificationId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
        unread.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(Instant.now());
        });
        notificationRepository.saveAll(unread);
        log.info("Marked all notifications as read for user {}", userId);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId);
    }

    public Page<Notification> getUserNotificationsPaged(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId, pageable);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void deleteOldNotifications(int daysOld) {
        Instant cutoff = Instant.now().minusSeconds(daysOld * 24L * 60 * 60);
        List<Notification> old = notificationRepository.findAll()
                .stream()
                .filter(n -> n.getSentAt().isBefore(cutoff))
                .toList();
        notificationRepository.deleteAll(old);
        log.info("Deleted {} old notifications", old.size());
    }
}
