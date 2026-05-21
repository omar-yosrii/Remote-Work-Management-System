package com.rwms.notification.observer;

import com.rwms.notification.dto.NotificationEvent;

public interface NotificationObserver {
    void onNotification(NotificationEvent event);
}
