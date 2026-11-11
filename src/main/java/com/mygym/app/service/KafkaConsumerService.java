package com.mygym.app.service;

import static com.mygym.app.util.MessageConsumerConstants.BROADCAST_GROUP_ID_CONSTANT;
import static com.mygym.app.util.MessageConsumerConstants.BROADCAST_MESSAGE_TYPE;
import static com.mygym.app.util.MessageConsumerConstants.BROADCAST_TOPIC_CONSTANT;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.mygym.app.models.BroadcastMessageDto;
import com.mygym.app.models.Notification;
import com.mygym.app.store.NotificationFeedStore;
import com.mygym.app.store.ProcessedEventStore;

@Service
public class KafkaConsumerService {

    public static final String EVENT_ID_HEADER = "mga-event-id";
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final NotificationFeedStore feedStore;
    private final ProcessedEventStore processedEvents;

    public KafkaConsumerService(NotificationFeedStore feedStore, ProcessedEventStore processedEvents) {
        this.feedStore = feedStore;
        this.processedEvents = processedEvents;
    }

    @KafkaListener(topics = BROADCAST_TOPIC_CONSTANT, groupId = BROADCAST_GROUP_ID_CONSTANT)
    public void consumeBroadcastMessage(BroadcastMessageDto messageDto,
            @Header(name = EVENT_ID_HEADER, required = false) String eventId) {
        if (messageDto == null || messageDto.content() == null || messageDto.content().isBlank()) {
            throw new IllegalArgumentException("Broadcast has no content");
        }
        String notificationId = eventId != null ? eventId : UUID.randomUUID().toString();
        if (eventId != null && !processedEvents.markIfFirst(eventId)) {
            LOGGER.info("Skipping duplicate broadcast {}", eventId);
            return;
        }
        Notification notification = new Notification(
                notificationId,
                BROADCAST_MESSAGE_TYPE,
                messageDto.content(),
                messageDto.image(),
                messageDto.senderId(),
                true);
        try {
            feedStore.append(notification);
            feedStore.publishLive(notification);
        } catch (RuntimeException e) {
            if (eventId != null) {
                processedEvents.forget(eventId);
            }
            throw e;
        }
    }
}
