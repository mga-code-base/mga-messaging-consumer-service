package com.mygym.app.config;

import static com.mygym.app.util.MessageConsumerConstants.LIVE_CHANNEL;
import static com.mygym.app.util.MessageConsumerConstants.LIVE_TOPIC_ENDPOINT;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.mygym.app.models.Notification;
import com.mygym.app.store.NotificationFeedStore;

@Configuration
public class RedisFanOutConfig {

    @Bean
    public MessageListener liveNotificationRelay(NotificationFeedStore store, SimpMessagingTemplate messagingTemplate) {
        return (message, pattern) -> {
            Notification notification = store.fromJson(new String(message.getBody(), StandardCharsets.UTF_8));
            if (Objects.nonNull(notification)) {
                messagingTemplate.convertAndSend(LIVE_TOPIC_ENDPOINT, notification);
            }
        };
    }

    @Bean
    public RedisMessageListenerContainer liveNotificationListenerContainer(RedisConnectionFactory connectionFactory,
            MessageListener liveNotificationRelay) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(liveNotificationRelay, new ChannelTopic(LIVE_CHANNEL));
        return container;
    }
}
