package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration
@PropertySource(value = {"vk.properties", "network.properties", "log4j.properties"})
@EnableScheduling
public class VkApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(VkApiApplication.class, args);
	}
}
