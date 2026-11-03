package com.mygym.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mygym.app.models.BroadcastMessageDto;
import com.mygym.app.models.Notification;
import com.mygym.app.store.NotificationFeedStore;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {

    @Mock
    private NotificationFeedStore feedStore;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    @Test
    @DisplayName("Should store the notification in Redis before publishing it to every instance")
    void testConsumeBroadcastMessage_StoresThenPublishes() {
        kafkaConsumerService.consumeBroadcastMessage(new BroadcastMessageDto("ADMIN-01", "Gym closed tomorrow.", "closure.png"));

        ArgumentCaptor<Notification> stored = ArgumentCaptor.forClass(Notification.class);
        InOrder order = inOrder(feedStore);
        order.verify(feedStore).append(stored.capture());
        order.verify(feedStore).publishLive(stored.getValue());

        Notification notification = stored.getValue();
        assertNotNull(notification.notificationId());
        assertEquals("Gym closed tomorrow.", notification.message());
        assertEquals("closure.png", notification.image());
        assertEquals("ADMIN-01", notification.generatingAdmin());
        assertEquals("BROADCAST", notification.messageType());
        assertTrue(notification.publishStatusCheck());
    }
}
