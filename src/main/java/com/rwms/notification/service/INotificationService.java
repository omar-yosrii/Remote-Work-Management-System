package com.rwms.notification.service;

import com.rwms.notification.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface INotificationService {
    Page<NotificationResponse> getUserNotifications(String email, Pageable pageable);
    long getUnreadCount(String email);
    void markAsRead(Long notificationId, String email);
    void markAllAsRead(String email);
}
