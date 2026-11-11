package com.mygym.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import com.mygym.app.store.ProcessedEventStore;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {

    @Mock
    private NotificationFeedStore feedStore;

    @Mock
    private ProcessedEventStore processedEvents;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    @Test
    @DisplayName("Should store the notification in Redis before publishing it to every instance")
    void testConsumeBroadcastMessage_StoresThenPublishes() {
        when(processedEvents.markIfFirst("evt-1")).thenReturn(true);

        kafkaConsumerService.consumeBroadcastMessage(new BroadcastMessageDto("ADMIN-01", "Gym closed tomorrow.", "closure.png"), "evt-1");

        ArgumentCaptor<Notification> stored = ArgumentCaptor.forClass(Notification.class);
        InOrder order = inOrder(feedStore);
        order.verify(feedStore).append(stored.capture());
        order.verify(feedStore).publishLive(stored.getValue());

        Notification notification = stored.getValue();
        assertEquals("evt-1", notification.notificationId());
        assertEquals("Gym closed tomorrow.", notification.message());
        assertEquals("closure.png", notification.image());
        assertEquals("ADMIN-01", notification.generatingAdmin());
        assertEquals("BROADCAST", notification.messageType());
        assertTrue(notification.publishStatusCheck());
    }

    @Test
    @DisplayName("Should ignore a redelivered broadcast with an event id it has already processed")
    void testConsumeBroadcastMessage_SkipsDuplicate() {
        when(processedEvents.markIfFirst("evt-1")).thenReturn(false);

        kafkaConsumerService.consumeBroadcastMessage(new BroadcastMessageDto("ADMIN-01", "Gym closed tomorrow.", null), "evt-1");

        verifyNoInteractions(feedStore);
    }

    @Test
    @DisplayName("Should still process legacy messages without an event id")
    void testConsumeBroadcastMessage_WithoutEventId() {
        kafkaConsumerService.consumeBroadcastMessage(new BroadcastMessageDto("ADMIN-01", "Hello", null), null);

        ArgumentCaptor<Notification> stored = ArgumentCaptor.forClass(Notification.class);
        verify(feedStore).append(stored.capture());
        assertNotNull(stored.getValue().notificationId());
        verify(processedEvents, never()).markIfFirst(any());
    }

    @Test
    @DisplayName("Should release the dedupe marker when storing fails so the retry is not skipped")
    void testConsumeBroadcastMessage_ReleasesMarkerOnFailure() {
        when(processedEvents.markIfFirst("evt-2")).thenReturn(true);
        doThrow(new IllegalStateException("redis down")).when(feedStore).append(any());

        assertThrows(IllegalStateException.class, () -> kafkaConsumerService
                .consumeBroadcastMessage(new BroadcastMessageDto("ADMIN-01", "Hello", null), "evt-2"));

        verify(processedEvents).forget("evt-2");
    }

    @Test
    @DisplayName("Should reject an empty broadcast so it goes to the dead letter topic")
    void testConsumeBroadcastMessage_RejectsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> kafkaConsumerService
                .consumeBroadcastMessage(new BroadcastMessageDto("ADMIN-01", " ", null), "evt-3"));
        verifyNoInteractions(feedStore);
    }
}
