package com.mygym.app.service;

import static com.mygym.app.util.MessageConsumerConstants.BROADCAST_TOPIC_CONSTANT;
import static com.mygym.app.util.MessageConsumerConstants.BROADCAST_GROUP_ID_CONSTANT;
import static com.mygym.app.util.MessageConsumerConstants.LIVE_TOPIC_ENDPOINT;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.mygym.app.models.BroadcastMessageDto;
import com.mygym.app.models.Notification;
import java.util.UUID;
import java.util.List;

/**
 * Event-driven consumer listening to Kafka broadcast topics, updating Caffeine cache, and fanning out to WebSockets.
 */
@Service
public class KafkaConsumerService {

    private final SimpMessagingTemplate messagingTemplate;
    private final CacheManager cacheManager;

    public KafkaConsumerService(SimpMessagingTemplate messagingTemplate, CacheManager cacheManager) {
        this.messagingTemplate = messagingTemplate;
        this.cacheManager = cacheManager;
    }

    /**
     * Consumes broadcast messages, stores them in the Caffeine cache, and dispatches them over STOMP WebSockets.
     */
    @SuppressWarnings("unchecked")
    @KafkaListener(topics = BROADCAST_TOPIC_CONSTANT, groupId = BROADCAST_GROUP_ID_CONSTANT)
    public void consumeBroadcastMessage(BroadcastMessageDto messageDto) {
        try {
            Notification notification = new Notification();
            notification.setNotificationId(UUID.randomUUID().toString());
            notification.setMessage(messageDto.getContent());
            notification.setImage(messageDto.getImage());
            notification.setGeneratingAdmin(messageDto.getSenderId());
            notification.setMessageType("BROADCAST");
            notification.setPublishStatusCheck(true);

            Cache cache = cacheManager.getCache("notificationsCache");
            if (cache != null) {
                List<Notification> activeFeed = cache.get("all_cached_notices", List.class);
                if (activeFeed == null) {
                    activeFeed = new java.util.concurrent.CopyOnWriteArrayList<>();
                }
                activeFeed.add(0, notification);
                cache.put("all_cached_notices", activeFeed);
            }

            messagingTemplate.convertAndSend(LIVE_TOPIC_ENDPOINT, notification);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}