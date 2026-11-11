package com.mygym.app.store;

import static com.mygym.app.util.MessageConsumerConstants.FEED_KEY;
import static com.mygym.app.util.MessageConsumerConstants.LIVE_CHANNEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import com.mygym.app.models.Notification;

import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class NotificationFeedStoreTest {

    private static final long NOW = 1_800_000_000_000L;
    private static final Duration RETENTION = Duration.ofHours(24);

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ZSetOperations<String, String> zSet;

    private final JsonMapper mapper = JsonMapper.builder().build();
    private NotificationFeedStore store;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.ofEpochMilli(NOW), ZoneOffset.UTC);
        store = new NotificationFeedStore(redis, mapper, clock, RETENTION, 100);
    }

    @Test
    @DisplayName("Should add the notification scored by time, drop expired entries and cap the feed size")
    void testAppend() {
        when(redis.opsForZSet()).thenReturn(zSet);

        store.append(new Notification("N-1", "BROADCAST", "Hello", null, "A1", true));

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(zSet).add(eq(FEED_KEY), json.capture(), eq((double) NOW));
        assertTrue(json.getValue().contains("\"notificationId\":\"N-1\""));
        verify(zSet).removeRangeByScore(FEED_KEY, Double.NEGATIVE_INFINITY, NOW - RETENTION.toMillis());
        verify(zSet).removeRange(FEED_KEY, 0, -101);
        verify(redis).expire(FEED_KEY, RETENTION);
    }

    @Test
    @DisplayName("Should publish live notifications on the shared Redis channel")
    void testPublishLive() {
        store.publishLive(new Notification("N-1", "BROADCAST", "Hello", null, "A1", true));

        verify(redis).convertAndSend(eq(LIVE_CHANNEL), anyString());
    }

    @Test
    @DisplayName("Should return only entries inside the retention window, newest first, skipping unreadable ones")
    void testRecent() throws Exception {
        when(redis.opsForZSet()).thenReturn(zSet);
        LinkedHashSet<String> stored = new LinkedHashSet<>(List.of(
                mapper.writeValueAsString(new Notification("N-2", "BROADCAST", "Newer", null, "A1", true)),
                "not-json",
                mapper.writeValueAsString(new Notification("N-1", "BROADCAST", "Older", null, "A1", true))));
        when(zSet.reverseRangeByScore(FEED_KEY, NOW - RETENTION.toMillis(), Double.POSITIVE_INFINITY)).thenReturn(stored);

        List<Notification> recent = store.recent();

        assertEquals(List.of("N-2", "N-1"), recent.stream().map(Notification::notificationId).toList());
    }

    @Test
    @DisplayName("Should return null for unreadable payloads instead of failing")
    void testFromJson_Invalid() {
        assertNull(store.fromJson("{broken"));
    }
}
