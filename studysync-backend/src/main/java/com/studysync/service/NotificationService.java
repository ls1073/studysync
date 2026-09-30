package com.studysync.service;

import com.studysync.dto.DashboardDtos.NotificationResponse;
import com.studysync.entity.Notification;
import com.studysync.entity.NotificationType;
import com.studysync.entity.User;
import com.studysync.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Notification create(User user, NotificationType type, String message, Long relatedTaskId, Long relatedGoalId) {
        Notification n = Notification.builder()
                .user(user)
                .type(type)
                .message(message)
                .relatedTaskId(relatedTaskId)
                .relatedGoalId(relatedGoalId)
                .build();
        return notificationRepository.save(n);
    }

    public List<NotificationResponse> listForUser(User user) {
        List<NotificationResponse> result = new ArrayList<>();
        for (Notification n : notificationRepository.findByUserOrderByCreatedAtDesc(user)) {
            result.add(new NotificationResponse(n.getId(), n.getType().name(), n.getMessage(), n.getIsRead(),
                    n.getCreatedAt().format(FORMATTER)));
        }
        return result;
    }

    public void markRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    public long countUnread(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
}
