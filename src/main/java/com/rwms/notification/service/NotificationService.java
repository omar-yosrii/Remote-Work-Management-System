package com.rwms.notification.service;

import com.rwms.common.exception.ResourceNotFoundException;
import com.rwms.notification.dto.NotificationEvent;
import com.rwms.notification.dto.NotificationResponse;
import com.rwms.notification.entity.Notification;
import com.rwms.notification.observer.NotificationObserver;
import com.rwms.notification.observer.NotificationSubject;
import com.rwms.notification.repository.NotificationRepository;
import com.rwms.user.entity.User;
import com.rwms.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService implements INotificationService, NotificationSubject, NotificationObserver {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    private final List<NotificationObserver> observers = new ArrayList<>();

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.registerObserver(this); // Register itself to handle persistence
    }

    // --- Subject Methods ---
    @Override
    public void registerObserver(NotificationObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(NotificationEvent event) {
        for (NotificationObserver observer : observers) {
            observer.onNotification(event);
        }
    }

    // This method is called by other services to publish an event via Spring's mechanism
    public void publishNotification(NotificationEvent event) {
        eventPublisher.publishEvent(event);
    }

    // Spring Event Listener acts as the bridge to our Observer implementation
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        notifyObservers(event);
    }

    // --- Observer Methods ---
    @Override
    @Transactional
    public void onNotification(NotificationEvent event) {
        Notification notification = Notification.builder()
                .recipient(event.getRecipient())
                .title(event.getTitle())
                .message(event.getMessage())
                .type(event.getType())
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    // --- API Methods ---
    @Override
    public Page<NotificationResponse> getUserNotifications(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::toResponse);
    }

    @Override
    public long getUnreadCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
                
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Notification does not belong to user");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        List<Notification> unread = notificationRepository.findByRecipientIdAndIsReadFalse(user.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
    
    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
