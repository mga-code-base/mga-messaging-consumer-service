package com.mygym.app;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.github.benmanes.caffeine.cache.Caffeine;

@SpringBootApplication
@ComponentScan(basePackages = "com.mygym.app")
public class MgaMessagingConsumerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MgaMessagingConsumerServiceApplication.class, args);
	}

    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("notificationsCache");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS) 
                .maximumSize(10000)); 
        return cacheManager;
    }
}
