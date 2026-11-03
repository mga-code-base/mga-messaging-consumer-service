package com.mygym.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mygym.app.models.Notification;
import com.mygym.app.store.NotificationFeedStore;

@ExtendWith(MockitoExtension.class)
class NotificationHistoryControllerTest {

    @Mock
    private NotificationFeedStore feedStore;

    @InjectMocks
    private NotificationHistoryController controller;

    @Test
    @DisplayName("Should return empty list when the feed is empty")
    void testGetUnreadAndMissedHistory_EmptyFeed_ReturnsEmptyList() {
        when(feedStore.recent()).thenReturn(List.of());

        assertTrue(controller.getUnreadAndMissedHistory(List.of("N-101")).isEmpty());
    }

    @Test
    @DisplayName("Should filter out read notifications and return only unread notifications")
    void testGetUnreadAndMissedHistory_FiltersReadNotifications() {
        Notification n1 = new Notification("N-101", "BROADCAST", "Notice 1", null, "A1", true);
        Notification n2 = new Notification("N-102", "BROADCAST", "Notice 2", null, "A1", true);
        when(feedStore.recent()).thenReturn(List.of(n2, n1));

        List<Notification> result = controller.getUnreadAndMissedHistory(List.of("N-101"));

        assertEquals(1, result.size());
        assertEquals("N-102", result.get(0).notificationId());
    }
}
