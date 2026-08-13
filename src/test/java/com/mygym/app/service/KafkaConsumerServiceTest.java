package com.mygym.app.service;

import com.mygym.app.models.BroadcastMessageDto;
import com.mygym.app.models.Notification;
import com.mygym.app.util.MessageConsumerConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    @Test
    @DisplayName("Should process message, update cache, and broadcast via WebSockets")
    void testConsumeBroadcastMessage_Success() {
        BroadcastMessageDto dto = new BroadcastMessageDto();
        dto.setSenderId("ADMIN-01");
        dto.setContent("Gym closed tomorrow.");
        dto.setImage("closure.png");

        CopyOnWriteArrayList<Notification> mockFeed = new CopyOnWriteArrayList<>();

        when(cacheManager.getCache("notificationsCache")).thenReturn(cache);
        when(cache.get("all_cached_notices", List.class)).thenReturn(mockFeed);

        kafkaConsumerService.consumeBroadcastMessage(dto);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(messagingTemplate, times(1)).convertAndSend(eq(MessageConsumerConstants.LIVE_TOPIC_ENDPOINT), notificationCaptor.capture());

        Notification captured = notificationCaptor.getValue();
        assertNotNull(captured.getNotificationId());
        assertEquals("Gym closed tomorrow.", captured.getMessage());
        assertEquals("closure.png", captured.getImage());
        assertEquals("ADMIN-01", captured.getGeneratingAdmin());
        assertEquals("BROADCAST", captured.getMessageType());

        verify(cache, times(1)).put("all_cached_notices", mockFeed);
        assertEquals(1, mockFeed.size());
    }
}