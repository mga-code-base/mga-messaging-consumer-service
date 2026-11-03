package com.mygym.app.service;

import static com.mygym.app.util.MessageConsumerConstants.BROADCAST_GROUP_ID_CONSTANT;
import static com.mygym.app.util.MessageConsumerConstants.BROADCAST_MESSAGE_TYPE;
import static com.mygym.app.util.MessageConsumerConstants.BROADCAST_TOPIC_CONSTANT;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.mygym.app.models.BroadcastMessageDto;
import com.mygym.app.models.Notification;
import com.mygym.app.store.NotificationFeedStore;

@Service
public class KafkaConsumerService {

    private final NotificationFeedStore feedStore;

    public KafkaConsumerService(NotificationFeedStore feedStore) {
        this.feedStore = feedStore;
    }

    @KafkaListener(topics = BROADCAST_TOPIC_CONSTANT, groupId = BROADCAST_GROUP_ID_CONSTANT)
    public void consumeBroadcastMessage(BroadcastMessageDto messageDto) {
        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                BROADCAST_MESSAGE_TYPE,
                messageDto.content(),
                messageDto.image(),
                messageDto.senderId(),
                true);
        feedStore.append(notification);
        feedStore.publishLive(notification);
    }
}
