package com.mygym.app.store;

import static com.mygym.app.util.MessageConsumerConstants.FEED_KEY;
import static com.mygym.app.util.MessageConsumerConstants.LIVE_CHANNEL;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygym.app.models.Notification;

@Component
public class NotificationFeedStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationFeedStore.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final Clock clock;
    private final Duration retention;
    private final long maxEntries;

    @Autowired
    public NotificationFeedStore(StringRedisTemplate redis,
            @Value("${mga.notifications.retention:PT24H}") Duration retention,
            @Value("${mga.notifications.max-entries:10000}") long maxEntries) {
        this(redis, new ObjectMapper(), Clock.systemUTC(), retention, maxEntries);
    }

    NotificationFeedStore(StringRedisTemplate redis, ObjectMapper mapper, Clock clock, Duration retention, long maxEntries) {
        this.redis = redis;
        this.mapper = mapper;
        this.clock = clock;
        this.retention = retention;
        this.maxEntries = maxEntries;
    }

    public void append(Notification notification) {
        String json = toJson(notification);
        long now = clock.millis();
        redis.opsForZSet().add(FEED_KEY, json, now);
        redis.opsForZSet().removeRangeByScore(FEED_KEY, Double.NEGATIVE_INFINITY, now - retention.toMillis());
        redis.opsForZSet().removeRange(FEED_KEY, 0, -(maxEntries + 1));
        redis.expire(FEED_KEY, retention);
    }

    public void publishLive(Notification notification) {
        redis.convertAndSend(LIVE_CHANNEL, toJson(notification));
    }

    public List<Notification> recent() {
        long cutoff = clock.millis() - retention.toMillis();
        Set<String> entries = redis.opsForZSet().reverseRangeByScore(FEED_KEY, cutoff, Double.POSITIVE_INFINITY);
        if (entries == null) {
            return List.of();
        }
        return entries.stream().map(this::fromJson).filter(Objects::nonNull).toList();
    }

    public Notification fromJson(String json) {
        try {
            return mapper.readValue(json, Notification.class);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Skipping unreadable notification entry", e);
            return null;
        }
    }

    private String toJson(Notification notification) {
        try {
            return mapper.writeValueAsString(notification);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialise notification " + notification.notificationId(), e);
        }
    }
}
