package com.rwms.notification.observer;

public interface NotificationSubject {
    void registerObserver(NotificationObserver observer);
    void removeObserver(NotificationObserver observer);
    void notifyObservers(com.rwms.notification.dto.NotificationEvent event);
}
