package com.mygym.app.controller;

import com.mygym.app.models.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationHistoryControllerTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private NotificationHistoryController controller;

    @Test
    @DisplayName("Should return empty list when cache instance is null")
    void testGetUnreadAndMissedHistory_NullCache_ReturnsEmptyList() {
        when(cacheManager.getCache("notificationsCache")).thenReturn(null);

        List<Notification> result = controller.getUnreadAndMissedHistory(List.of("N-101"));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when cached active feed is null")
    void testGetUnreadAndMissedHistory_NullActiveFeed_ReturnsEmptyList() {
        when(cacheManager.getCache("notificationsCache")).thenReturn(cache);
        when(cache.get("all_cached_notices", List.class)).thenReturn(null);

        List<Notification> result = controller.getUnreadAndMissedHistory(List.of("N-101"));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should filter out read notifications and return only unread notifications")
    void testGetUnreadAndMissedHistory_FiltersReadNotifications() {
        Notification n1 = new Notification();
        n1.setNotificationId("N-101");
        n1.setMessage("Notice 1");

        Notification n2 = new Notification();
        n2.setNotificationId("N-102");
        n2.setMessage("Notice 2");

        when(cacheManager.getCache("notificationsCache")).thenReturn(cache);
        when(cache.get("all_cached_notices", List.class)).thenReturn(List.of(n1, n2));

        List<Notification> result = controller.getUnreadAndMissedHistory(List.of("N-101"));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("N-102", result.get(0).getNotificationId());
    }
}