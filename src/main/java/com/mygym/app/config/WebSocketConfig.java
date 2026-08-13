package com.mygym.app.config;

import static com.mygym.app.util.MessageConsumerConstants.NOTIFICATIONS_ENDPOINT;
import static com.mygym.app.util.MessageConsumerConstants.TOPIC_ENDPOINT;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
	    registry.addEndpoint(NOTIFICATIONS_ENDPOINT)
	            .setAllowedOriginPatterns("*"); 
	}

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(TOPIC_ENDPOINT);
    }
}
