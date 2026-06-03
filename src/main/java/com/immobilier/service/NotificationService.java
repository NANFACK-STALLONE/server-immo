package com.immobilier.service;

import com.immobilier.entity.AppNotification;
import com.immobilier.entity.User;
import com.immobilier.exception.ResourceNotFoundException;
import com.immobilier.repository.AppNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final AppNotificationRepository notificationRepository;

    public List<AppNotification> getUserNotifications(User user) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId());
    }

    public long countUnread(User user) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
    }

    public AppNotification markAsRead(String notificationId, User user) {
        AppNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvee"));
        if (!notification.getRecipientId().equals(user.getId())) {
            throw new IllegalArgumentException("Vous n'avez pas acces a cette notification");
        }
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }
}
