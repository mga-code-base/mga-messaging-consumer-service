package com.mygym.app.controller;

import static com.mygym.app.util.MessageConsumerConstants.CONSUMER_REST_CONTROLLER;
import static com.mygym.app.util.MessageConsumerConstants.SYNC_ENDPOINT;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mygym.app.models.Notification;

/**
 * REST controller for retrieving unread and missed broadcast notifications from the Caffeine memory cache.
 */
@RestController
@RequestMapping(CONSUMER_REST_CONTROLLER)
@CrossOrigin(origins = "*")
public class NotificationHistoryController {

    private final CacheManager cacheManager;

    public NotificationHistoryController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Filters active cached notifications against the client's provided list of read notification IDs.
     */
    @SuppressWarnings("unchecked")
    @PostMapping(SYNC_ENDPOINT)
    public List<Notification> getUnreadAndMissedHistory(@RequestBody List<String> readNotificationIds) {
        
        Cache cache = cacheManager.getCache("notificationsCache");
        if (cache == null) {
            return new ArrayList<>();
        }

        List<Notification> activeFeed = cache.get("all_cached_notices", List.class);
        if (activeFeed == null) {
            return new ArrayList<>();
        }

        return activeFeed.stream()
                .filter(notice -> !readNotificationIds.contains(notice.getNotificationId()))
                .collect(Collectors.toList());
    }
}