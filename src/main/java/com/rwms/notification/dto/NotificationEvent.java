package com.rwms.notification.dto;

import com.rwms.notification.entity.NotificationType;
import com.rwms.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private User recipient;
    private String title;
    private String message;
    private NotificationType type;
}
