package com.mygym.app.store;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProcessedEventStore {

    private static final String KEY_PREFIX = "mga:notifications:processed:";

    private final StringRedisTemplate redis;
    private final Duration retention;

    public ProcessedEventStore(StringRedisTemplate redis,
            @Value("${mga.notifications.dedupe-retention:PT24H}") Duration retention) {
        this.redis = redis;
        this.retention = retention;
    }

    public boolean markIfFirst(String eventId) {
        Boolean first = redis.opsForValue().setIfAbsent(KEY_PREFIX + eventId, "1", retention);
        return Boolean.TRUE.equals(first);
    }

    public void forget(String eventId) {
        redis.delete(KEY_PREFIX + eventId);
    }
}
