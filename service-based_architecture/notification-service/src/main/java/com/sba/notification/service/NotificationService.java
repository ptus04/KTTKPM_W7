package com.sba.notification.service;

import com.sba.common.dto.NotificationRequest;
import com.sba.notification.entity.NotificationLog;
import com.sba.notification.repository.NotificationLogRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;

    public NotificationService(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    public void notify(NotificationRequest request) {
        NotificationLog log = new NotificationLog();
        log.setUserId(request.userId());
        log.setOrderId(request.orderId());
        log.setMessage(request.message());
        notificationLogRepository.save(log);

        System.out.println("Notification: User " + request.userId() + " da dat don #" + request.orderId() + " thanh cong");
    }
}
