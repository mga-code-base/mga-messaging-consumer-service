package com.mygym.app.controller;

import static com.mygym.app.util.MessageConsumerConstants.CONSUMER_REST_CONTROLLER;
import static com.mygym.app.util.MessageConsumerConstants.SYNC_ENDPOINT;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mygym.app.models.Notification;
import com.mygym.app.store.NotificationFeedStore;

@RestController
@RequestMapping(CONSUMER_REST_CONTROLLER)
@CrossOrigin(origins = "*")
public class NotificationHistoryController {

    private final NotificationFeedStore feedStore;

    public NotificationHistoryController(NotificationFeedStore feedStore) {
        this.feedStore = feedStore;
    }

    @PostMapping(SYNC_ENDPOINT)
    public List<Notification> getUnreadAndMissedHistory(@RequestBody List<String> readNotificationIds) {
        Set<String> read = new HashSet<>(readNotificationIds);
        return feedStore.recent().stream()
                .filter(notice -> !read.contains(notice.notificationId()))
                .toList();
    }
}
