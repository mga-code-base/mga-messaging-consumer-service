package com.mygym.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.mygym.app")
public class MgaMessagingConsumerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MgaMessagingConsumerServiceApplication.class, args);
	}
}
